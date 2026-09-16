package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.NomeStatusIdeia;
import br.com.fiap.aguiaexpress.model.StatusIdeia;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.repository.UsuarioBadgeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class MilestoneBadgeServiceTest {
    @ParameterizedTest
    @EnumSource(NomeStatusIdeia.class)
    void awardsOnlyTheDefinedMilestonesToTheIdeaAuthor(NomeStatusIdeia status) {
        UsuarioBadgeRepository repository = mock(UsuarioBadgeRepository.class);
        Usuario author = new Usuario();
        author.setId(42L);
        Ideia idea = new Ideia();
        idea.setUsuario(author);
        idea.setStatusIdeia(new StatusIdeia(1L, status, "Status"));

        new MilestoneBadgeService(repository).awardForIdea(idea);

        verify(repository).concederMarcoSeAusente(42L, "PRIMEIRA_IDEIA");
        if (status == NomeStatusIdeia.APROVADA || status == NomeStatusIdeia.CONVERTIDA_EM_PROJETO) {
            verify(repository).concederMarcoSeAusente(42L, "IDEIA_APROVADA");
        }
        if (status == NomeStatusIdeia.CONVERTIDA_EM_PROJETO) {
            verify(repository).concederMarcoSeAusente(42L, "PROJETO_IMPLEMENTADO");
        }
        verifyNoMoreInteractions(repository);
    }

    @Test
    void requiresTheOwningIdeaOrProjectTransaction() throws Exception {
        Transactional award = MilestoneBadgeService.class.getMethod("awardForIdea", Ideia.class)
                .getAnnotation(Transactional.class);
        assertEquals(Propagation.MANDATORY, award.propagation());
        assertNotNull(IdeiaService.class.getMethod("criarIdeia",
                br.com.fiap.aguiaexpress.dto.ideia.RegistrarIdeiaDTO.class).getAnnotation(Transactional.class));
        assertNotNull(IdeiaService.class.getMethod("atualizarStatusIdeia", Long.class,
                br.com.fiap.aguiaexpress.dto.ideia.AtualizarStatusIdeiaDTO.class).getAnnotation(Transactional.class));
    }
}
