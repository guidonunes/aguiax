package br.com.fiap.aguiaexpress.repository;

import io.github.cdimascio.dotenv.Dotenv;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.data.jpa.repository.Query;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/** Opt-in: requires MySQL and CREATE DATABASE permission; never writes to the app schema. */
@EnabledIfSystemProperty(named = "mysql.test.enabled", matches = "true")
class MilestoneBadgeMySqlIntegrationTest {
    @Test
    void migrationAndConcurrentRuntimeAwardsPreserveExistingDataAndRollback() throws Exception {
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        String configuredUrl = env.get("DB_URL");
        assertNotNull(configuredUrl, "Configure DB_URL for this opt-in MySQL test.");
        assertTrue(configuredUrl.startsWith("jdbc:mysql://"));
        int databaseStart = configuredUrl.indexOf('/', "jdbc:mysql://".length());
        assertTrue(databaseStart >= 0);
        int queryStart = configuredUrl.indexOf('?', databaseStart);
        String suffix = queryStart < 0 ? "" : configuredUrl.substring(queryStart);
        String server = configuredUrl.substring(0, databaseStart + 1);
        String schema = "aguiaexpress_badges_test_" + UUID.randomUUID().toString().replace("-", "");
        String testUrl = server + schema + suffix;
        String user = env.get("DB_USER");
        String password = env.get("DB_PASSWORD");

        try (Connection admin = DriverManager.getConnection(server + suffix, user, password)) {
            try (Statement statement = admin.createStatement()) {
                statement.execute("CREATE DATABASE `" + schema + "`");
            }
            try {
                Flyway.configure().dataSource(testUrl, user, password).target("12").load().migrate();
                try (Connection connection = DriverManager.getConnection(testUrl, user, password)) {
                    seed(connection);
                    Flyway.configure().dataSource(testUrl, user, password).load().migrate();
                    assertEquals(1, count(connection, 101));
                    assertEquals(2, count(connection, 102));
                    assertEquals(3, count(connection, 103));
                    assertEquals(1, count(connection, 104));
                    assertEquals(0, count(connection, 105));
                    assertEquals(0, count(connection, 106));
                    assertEquals("2025-01-01 10:00:00", firstAwardDate(connection, 102));

                    // Re-running the backfill does not duplicate rows or reset old dates.
                    try (var resource = getClass().getResourceAsStream(
                            "/db/migration/V13__backfill_operational_milestone_badges.sql")) {
                        execute(connection, new String(assertNotNullResource(resource).readAllBytes(), StandardCharsets.UTF_8));
                    }
                    assertEquals(3, count(connection, 103));
                    assertEquals("2025-01-01 10:00:00", firstAwardDate(connection, 102));

                    award(connection, 101, "PRIMEIRA_IDEIA");
                    award(connection, 105, "PRIMEIRA_IDEIA");
                    award(connection, 106, "PRIMEIRA_IDEIA");
                    award(connection, 101, "UNKNOWN");
                    assertEquals(1, count(connection, 101));
                    assertEquals(0, count(connection, 105));
                    assertEquals(0, count(connection, 106));

                    execute(connection, "UPDATE t_badges SET st_status = 0 WHERE nm_badge = 'IDEIA_APROVADA'");
                    award(connection, 101, "IDEIA_APROVADA");
                    assertEquals(1, count(connection, 101));
                    execute(connection, "UPDATE t_badges SET st_status = 1 WHERE nm_badge = 'IDEIA_APROVADA'");

                    connection.setAutoCommit(false);
                    execute(connection, "UPDATE t_ideias SET id_status_ideia = 4 WHERE id_usuario = 101");
                    award(connection, 101, "IDEIA_APROVADA");
                    assertEquals(2, count(connection, 101));
                    connection.rollback();
                    connection.setAutoCommit(true);
                    assertEquals(1, count(connection, 101));
                    try (Statement statement = connection.createStatement();
                         var result = statement.executeQuery("SELECT id_status_ideia FROM t_ideias WHERE id_usuario = 101")) {
                        assertTrue(result.next());
                        assertEquals(1, result.getInt(1));
                    }

                    execute(connection, "INSERT INTO t_usuarios (id_usuario,id_perfil,nm_nome,ds_email,ds_senha) VALUES (107,1,'Autor','107@test.invalid','unused')");
                    execute(connection, "INSERT INTO t_ideias (id_usuario,id_categoria,id_status_ideia,id_orientacao,ds_titulo,ds_descricao) SELECT 107,MIN(id_categoria),1,201,'Nova ideia','Descrição' FROM t_categorias");
                    try (var executor = Executors.newFixedThreadPool(2)) {
                        CountDownLatch gate = new CountDownLatch(1);
                        var first = executor.submit(() -> concurrentAward(testUrl, user, password, gate));
                        var second = executor.submit(() -> concurrentAward(testUrl, user, password, gate));
                        gate.countDown();
                        first.get();
                        second.get();
                    }
                    assertEquals(1, count(connection, 107));
                }
            } finally {
                // Only this test's newly created, UUID-scoped database is removed.
                try (Statement statement = admin.createStatement()) {
                    statement.execute("DROP DATABASE `" + schema + "`");
                }
            }
        }
    }

    private static void seed(Connection connection) throws Exception {
        for (int id = 101; id <= 106; id++) {
            int role = id == 105 ? 2 : id == 106 ? 3 : 1;
            execute(connection, "INSERT INTO t_usuarios (id_usuario,id_perfil,nm_nome,ds_email,ds_senha) VALUES ("
                    + id + "," + role + ",'Teste','" + id + "@test.invalid','unused')");
        }
        execute(connection, "INSERT INTO t_orientacoes_estrategicas (id_orientacao,id_usuario,ds_titulo,ds_descricao) VALUES (201,106,'Estratégia','Descrição')");
        int[] statuses = {1, 4, 6, 2, 6};
        for (int index = 0; index < statuses.length; index++) {
            execute(connection, "INSERT INTO t_ideias (id_usuario,id_categoria,id_status_ideia,id_orientacao,ds_titulo,ds_descricao) SELECT "
                    + (101 + index) + ",MIN(id_categoria)," + statuses[index] + ",201,'Ideia " + index + "','Descrição' FROM t_categorias");
        }
        execute(connection, "INSERT INTO t_usuario_badges (id_usuario,id_badge,dt_conquista) SELECT 102,id_badge,'2025-01-01 10:00:00' FROM t_badges WHERE nm_badge = 'PRIMEIRA_IDEIA'");
    }

    private static Void concurrentAward(String url, String user, String password, CountDownLatch gate) throws Exception {
        gate.await();
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            award(connection, 107, "PRIMEIRA_IDEIA");
        }
        return null;
    }

    private static void award(Connection connection, long userId, String badge) throws Exception {
        String query = UsuarioBadgeRepository.class.getMethod("concederMarcoSeAusente", Long.class, String.class)
                .getAnnotation(Query.class).value().replace(":nomeBadge", "?").replace(":usuarioId", "?");
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, badge);
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    private static int count(Connection connection, long userId) throws Exception {
        try (Statement statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COUNT(*) FROM t_usuario_badges WHERE id_usuario = " + userId)) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }

    private static String firstAwardDate(Connection connection, long userId) throws Exception {
        try (Statement statement = connection.createStatement();
             var result = statement.executeQuery("SELECT DATE_FORMAT(dt_conquista,'%Y-%m-%d %H:%i:%s') FROM t_usuario_badges ub JOIN t_badges b ON b.id_badge=ub.id_badge WHERE ub.id_usuario=" + userId + " AND b.nm_badge='PRIMEIRA_IDEIA'")) {
            assertTrue(result.next());
            return result.getString(1);
        }
    }

    private static java.io.InputStream assertNotNullResource(java.io.InputStream resource) {
        assertNotNull(resource);
        return resource;
    }

    private static void execute(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
