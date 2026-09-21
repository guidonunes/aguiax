package br.com.fiap.aguiax.viewmodel

import br.com.fiap.aguiax.domain.model.Ideia
import java.time.LocalDateTime

internal fun ordenarIdeiasMaisRecentes(ideias: List<Ideia>): List<Ideia> =
    ideias.sortedWith(
        compareByDescending<Ideia> { ideia ->
            ideia.dataCriacao?.let { timestamp ->
                runCatching { LocalDateTime.parse(timestamp) }.getOrNull()
            }
        }.thenByDescending { it.id }
    )
