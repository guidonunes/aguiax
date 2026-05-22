package br.com.fiap.aguiaexpress.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.fiap.aguiaexpress.ui.screens.IdeiasScreen
import br.com.fiap.aguiaexpress.ui.screens.MedalhasScreen
import br.com.fiap.aguiaexpress.ui.screens.categorias.CategoriasScreen
import br.com.fiap.aguiaexpress.ui.screens.home.HomeScreen
import br.com.fiap.aguiaexpress.ui.screens.ideias.CriarIdeiaScreen
import br.com.fiap.aguiaexpress.ui.screens.orientacoes.OrientacoesScreen
import br.com.fiap.aguiaexpress.ui.screens.projetos.ProjetosScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onCategoriasClick = {
                    navController.navigate(Routes.CATEGORIAS) },
                onOrientacoesClick = { navController.navigate(Routes.ORIENTACOES) },
                onIdeiasClick = { navController.navigate(Routes.IDEIAS) },
                onProjetosClick = { navController.navigate(Routes.PROJETOS) },
                onMedalhasClick = { navController.navigate(Routes.MEDALHAS) }
            )
        }

        composable(Routes.CATEGORIAS) {
            CategoriasScreen()
        }

        composable(Routes.ORIENTACOES) {
            OrientacoesScreen()
        }

        composable(Routes.IDEIAS) {
            IdeiasScreen(
                onCriarIdeiaClick = {
                    navController.navigate(Routes.CRIAR_IDEIA)
                }
            )
        }

        composable(Routes.CRIAR_IDEIA) {
            CriarIdeiaScreen()
        }

        composable(Routes.PROJETOS) {
            ProjetosScreen()
        }

        composable(Routes.MEDALHAS) {
            MedalhasScreen()
        }

    }
}