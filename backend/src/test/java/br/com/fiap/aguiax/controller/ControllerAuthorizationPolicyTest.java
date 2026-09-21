package br.com.fiap.aguiax.controller;

import br.com.fiap.aguiax.config.security.SecurityConfig;
import br.com.fiap.aguiax.dto.ideia.AtualizarAvaliacaoIdeiaDTO;
import br.com.fiap.aguiax.dto.ideia.AtualizarStatusIdeiaDTO;
import br.com.fiap.aguiax.dto.projeto.AtualizarStatusProjetoDTO;
import br.com.fiap.aguiax.dto.ideia.RegistrarIdeiaDTO;
import br.com.fiap.aguiax.dto.orientacao.RegistrarOrientacaoDTO;
import br.com.fiap.aguiax.dto.projeto.RegistrarProjetoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ControllerAuthorizationPolicyTest {

    @Test
    void methodSecurityIsEnabled() {
        EnableMethodSecurity annotation =
                SecurityConfig.class.getAnnotation(EnableMethodSecurity.class);

        assertNotNull(annotation);
    }

    @Test
    void dashboardControllerRequiresLeaderAtTheControllerBoundary() {
        PreAuthorize annotation = DashboardController.class.getAnnotation(PreAuthorize.class);

        assertNotNull(annotation);
        assertEquals("hasRole('LIDER')", annotation.value());
    }

    @Test
    void roleRestrictedOperationsMatchTheAuthorizationMatrix() throws Exception {
        List<ExpectedPolicy> policies = List.of(
                new ExpectedPolicy(
                        IdeiaController.class,
                        "criarIdeia",
                        new Class<?>[]{RegistrarIdeiaDTO.class},
                        "hasRole('OPERACIONAL')"
                ),
                new ExpectedPolicy(
                        IdeiaController.class,
                        "atualizarIdeia",
                        new Class<?>[]{Long.class, RegistrarIdeiaDTO.class},
                        "hasRole('OPERACIONAL')"
                ),
                new ExpectedPolicy(
                        IdeiaController.class,
                        "deletarIdeia",
                        new Class<?>[]{Long.class},
                        "hasRole('OPERACIONAL')"
                ),
                new ExpectedPolicy(
                        IdeiaController.class,
                        "atualizarStatusIdeia",
                        new Class<?>[]{Long.class, AtualizarStatusIdeiaDTO.class},
                        "hasRole('GESTOR')"
                ),
                new ExpectedPolicy(
                        IdeiaController.class,
                        "atualizarAvaliacaoIdeia",
                        new Class<?>[]{Long.class, AtualizarAvaliacaoIdeiaDTO.class},
                        "hasRole('GESTOR')"
                ),
                new ExpectedPolicy(
                        ProjetoController.class,
                        "criarProjeto",
                        new Class<?>[]{RegistrarProjetoDTO.class},
                        "hasRole('GESTOR')"
                ),
                new ExpectedPolicy(
                        ProjetoController.class,
                        "atualizarProjeto",
                        new Class<?>[]{Long.class, RegistrarProjetoDTO.class},
                        "hasRole('GESTOR')"
                ),
                new ExpectedPolicy(
                        ProjetoController.class,
                        "deletarProjeto",
                        new Class<?>[]{Long.class},
                        "hasRole('GESTOR')"
                ),
                new ExpectedPolicy(
                        ProjetoController.class,
                        "atualizarStatusProjeto",
                        new Class<?>[]{Long.class, AtualizarStatusProjetoDTO.class},
                        "hasRole('GESTOR')"
                ),
                new ExpectedPolicy(
                        OrientacaoEstrategicaController.class,
                        "criarOrientacao",
                        new Class<?>[]{RegistrarOrientacaoDTO.class},
                        "hasRole('LIDER')"
                ),
                new ExpectedPolicy(
                        OrientacaoEstrategicaController.class,
                        "atualizarOrientacao",
                        new Class<?>[]{Long.class, RegistrarOrientacaoDTO.class},
                        "hasRole('LIDER')"
                ),
                new ExpectedPolicy(
                        OrientacaoEstrategicaController.class,
                        "deletarOrientacao",
                        new Class<?>[]{Long.class},
                        "hasRole('LIDER')"
                )
        );

        for (ExpectedPolicy policy : policies) {
            Method method = policy.controller().getDeclaredMethod(
                    policy.methodName(),
                    policy.parameterTypes()
            );
            PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

            assertNotNull(
                    annotation,
                    () -> policy.controller().getSimpleName()
                            + "." + policy.methodName()
                            + " must declare @PreAuthorize"
            );
            assertEquals(
                    policy.expression(),
                    annotation.value(),
                    () -> "Unexpected policy on "
                            + policy.controller().getSimpleName()
                            + "." + policy.methodName()
            );
        }
    }

    private record ExpectedPolicy(
            Class<?> controller,
            String methodName,
            Class<?>[] parameterTypes,
            String expression
    ) {
    }
}
