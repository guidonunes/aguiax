package br.com.fiap.aguiaexpress.ui.screens.home

import br.com.fiap.aguiaexpress.domain.model.DashboardSummary
import br.com.fiap.aguiaexpress.domain.model.Ideia
import br.com.fiap.aguiaexpress.domain.model.Orientacao
import br.com.fiap.aguiaexpress.domain.model.Projeto
import br.com.fiap.aguiaexpress.domain.model.UsuarioBadge
import java.math.BigDecimal

internal fun homeIdea(id: Long, userId: Long = 7, status: String = "NOVA", votes: Int? = 0,
    time: String? = "2026-09-15T12:00:00Z") = Ideia(
    id, "Ideia $id", "Descrição", "Problema", "Solução", null, null, BigDecimal.ZERO,
    status, userId, 1, 1, "Pessoa", "Categoria", "Orientação", time, time, votes
)

internal fun homeBadge(id: Long, userId: Long = 7, badgeId: Long = id, time: String = "2026-09-15T13:00:00Z") = UsuarioBadge(
    id, userId, "Pessoa", "pessoa@example.test", badgeId, "Medalha $badgeId", "Descrição", "Critério", "", time
)

internal fun homeProject(id: Long, ideaId: Long = 1, status: String = "EM_ANDAMENTO",
    time: String? = "2026-09-15T14:00:00Z") = Projeto(
    id, ideaId, "Ideia $ideaId", 8, "Gestor", 1, status, "Projeto $id", "Descrição", "Objetivo",
    999999.0, 999999.0, 999999.0, 999999.0, 999999.0,
    "2026-09-01", "2026-12-01", null, time, time
)

internal fun homeOrientation(id: Long, title: String = "Orientação $id") = Orientacao(
    id, title, "Descrição", "Estratégia", "Líder", "LIDER", "2026-09-10T12:00:00Z", "2026-09-15T12:00:00Z"
)

internal fun homeSummary(total: Long = 8) = DashboardSummary(
    total, mapOf("PLANEJADO" to 2, "EM_ANDAMENTO" to 3, "CONCLUIDO" to 2, "CANCELADO" to 1),
    BigDecimal("1234.56"), BigDecimal("2468.91"), BigDecimal("100.25"), BigDecimal("1334.60"),
    BigDecimal("12.34"), BigDecimal("5.67")
)
