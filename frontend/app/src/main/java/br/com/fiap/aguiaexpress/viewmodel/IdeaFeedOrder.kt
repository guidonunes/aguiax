package br.com.fiap.aguiaexpress.viewmodel

import br.com.fiap.aguiaexpress.domain.model.Ideia
import java.time.LocalDateTime

internal fun ordenarIdeiasMaisRecentes(ideias: List<Ideia>): List<Ideia> =
    ideias.sortedWith(
        compareByDescending<Ideia> { ideia ->
            ideia.dataCriacao?.let { timestamp ->
                runCatching { LocalDateTime.parse(timestamp) }.getOrNull()
            }
        }.thenByDescending { it.id }
    )
