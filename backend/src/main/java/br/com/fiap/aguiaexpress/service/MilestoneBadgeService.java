package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.NomeStatusIdeia;
import br.com.fiap.aguiaexpress.repository.UsuarioBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Automatic, server-owned milestones; manual grant permissions remain unchanged. */
@Service
@RequiredArgsConstructor
public class MilestoneBadgeService {
    private final UsuarioBadgeRepository usuarioBadgeRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void awardForIdea(Ideia ideia) {
        Long authorId = ideia.getUsuario().getId();
        usuarioBadgeRepository.concederMarcoSeAusente(authorId, "PRIMEIRA_IDEIA");

        NomeStatusIdeia status = ideia.getStatusIdeia().getStatus();
        if (status == NomeStatusIdeia.APROVADA || status == NomeStatusIdeia.CONVERTIDA_EM_PROJETO) {
            usuarioBadgeRepository.concederMarcoSeAusente(authorId, "IDEIA_APROVADA");
        }
        if (status == NomeStatusIdeia.CONVERTIDA_EM_PROJETO) {
            usuarioBadgeRepository.concederMarcoSeAusente(authorId, "PROJETO_IMPLEMENTADO");
        }
    }
}
