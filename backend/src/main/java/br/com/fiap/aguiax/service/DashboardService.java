package br.com.fiap.aguiax.service;

import br.com.fiap.aguiax.dto.dashboard.DashboardSummaryResponse;
import br.com.fiap.aguiax.exception.ResourceNotFoundException;
import br.com.fiap.aguiax.model.NomeStatusProjeto;
import br.com.fiap.aguiax.model.Projeto;
import br.com.fiap.aguiax.repository.OrientacaoEstrategicaRepository;
import br.com.fiap.aguiax.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int AVERAGE_SCALE = 2;

    private final ProjetoRepository projetoRepository;
    private final OrientacaoEstrategicaRepository orientacaoEstrategicaRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse buscarResumo(Long orientacaoId) {
        if (orientacaoId == null) {
            return calcularResumo(projetoRepository.findAll());
        }

        if (!orientacaoEstrategicaRepository.existsById(orientacaoId)) {
            throw new ResourceNotFoundException("Orientação estratégica não encontrada.");
        }

        return calcularResumo(
                projetoRepository.findByIdeia_OrientacaoEstrategica_Id(orientacaoId)
        );
    }

    private DashboardSummaryResponse calcularResumo(List<Projeto> projetos) {
        EnumMap<NomeStatusProjeto, Long> projetosPorStatus =
                new EnumMap<>(NomeStatusProjeto.class);

        BigDecimal investimentoTotal = BigDecimal.ZERO;
        BigDecimal retornoFinanceiroTotal = BigDecimal.ZERO;
        BigDecimal reducaoCustosTotal = BigDecimal.ZERO;
        BigDecimal somaRoi = BigDecimal.ZERO;
        BigDecimal somaGanhoProdutividade = BigDecimal.ZERO;

        for (Projeto projeto : projetos) {
            NomeStatusProjeto status = projeto.getStatusProjeto().getStatus();
            projetosPorStatus.merge(status, 1L, Long::sum);

            investimentoTotal = investimentoTotal.add(valorOuZero(projeto.getInvestimento()));
            retornoFinanceiroTotal = retornoFinanceiroTotal.add(
                    valorOuZero(projeto.getRetornoFinanceiro())
            );
            reducaoCustosTotal = reducaoCustosTotal.add(valorOuZero(projeto.getReducaoCustos()));
            somaRoi = somaRoi.add(valorOuZero(projeto.getRoi()));
            somaGanhoProdutividade = somaGanhoProdutividade.add(
                    valorOuZero(projeto.getGanhoProdutividade())
            );
        }

        BigDecimal resultadoEstimado = retornoFinanceiroTotal
                .add(reducaoCustosTotal)
                .subtract(investimentoTotal);

        return new DashboardSummaryResponse(
                projetos.size(),
                projetosPorStatus,
                investimentoTotal,
                retornoFinanceiroTotal,
                reducaoCustosTotal,
                resultadoEstimado,
                calcularMedia(somaRoi, projetos.size()),
                calcularMedia(somaGanhoProdutividade, projetos.size())
        );
    }

    private BigDecimal calcularMedia(BigDecimal soma, int quantidade) {
        if (quantidade == 0) {
            return BigDecimal.ZERO;
        }

        return soma.divide(
                BigDecimal.valueOf(quantidade),
                AVERAGE_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}
