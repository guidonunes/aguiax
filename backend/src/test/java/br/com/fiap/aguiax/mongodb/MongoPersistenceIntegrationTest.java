package br.com.fiap.aguiax.mongodb;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoPersistenceIntegrationTest {

    private static final String TEST_DATABASE =
            "aguiax_task_2_2_" + UUID.randomUUID().toString().replace("-", "");

    @Autowired
    private MongoTemplate mongoTemplate;

    @DynamicPropertySource
    static void configureMongoDB(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.mongodb.uri",
                () -> System.getProperty(
                        "mongodb.test.uri",
                        "mongodb://localhost:27017"
                )
        );
        registry.add("spring.mongodb.database", () -> TEST_DATABASE);
    }

    @AfterAll
    void cleanUp() {
        mongoTemplate.getDb().drop();
    }

    @Test
    void shouldWriteAndReadMongoDocument() {
        PersistenceProbe expected = new PersistenceProbe(
                UUID.randomUUID().toString(),
                "Task 2.2 persistence test"
        );

        mongoTemplate.save(expected);

        PersistenceProbe persisted = mongoTemplate.findById(
                expected.id(),
                PersistenceProbe.class
        );

        assertThat(persisted).isNotNull();
        assertThat(persisted.id()).isEqualTo(expected.id());
        assertThat(persisted.message()).isEqualTo(expected.message());
    }

    @Document(collection = "task_2_2_persistence_probe")
    private record PersistenceProbe(
            @Id String id,
            String message
    ) {
    }
}
