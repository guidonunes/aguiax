package br.com.fiap.aguiaexpress.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.fiap.aguiaexpress.ui.screens.IdeiasScreen
import br.com.fiap.aguiaexpress.ui.screens.categorias.CategoriasScreen
import br.com.fiap.aguiaexpress.ui.screens.ideias.CriarIdeiaScreen
import br.com.fiap.aguiaexpress.ui.screens.medalhas.MedalhasScreen
import br.com.fiap.aguiaexpress.ui.screens.orientacoes.OrientacoesScreen
import br.com.fiap.aguiaexpress.ui.screens.projetos.ProjetosScreen
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme
import androidx.navigation.NavType
import androidx.navigation.navArgument
import br.com.fiap.aguiaexpress.ui.screens.ideias.EditarIdeiaScreen
import br.com.fiap.aguiaexpress.ui.screens.orientacoes.CriarOrientacaoScreen
import br.com.fiap.aguiaexpress.ui.screens.orientacoes.EditarOrientacaoScreen
import br.com.fiap.aguiaexpress.core.session.SessionManager
import br.com.fiap.aguiaexpress.ui.screens.dashboard.DashboardScreen
import br.com.fiap.aguiaexpress.ui.screens.projetos.CriarProjetoScreen
import br.com.fiap.aguiaexpress.ui.screens.projetos.EditarProjetoScreen


private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val bottomNavItems = buildList {
        add(
            BottomNavItem(
                route = Routes.ORIENTACOES,
                label = "Orientações",
                icon = Icons.Default.Flag
            )
        )

        add(
            BottomNavItem(
                route = Routes.IDEIAS,
                label = "Ideias",
                icon = Icons.Default.Lightbulb
            )
        )

        add(
            BottomNavItem(
                route = Routes.PROJETOS,
                label = "Projetos",
                icon = Icons.Default.Work
            )
        )

        if (SessionManager.podeAcessarDashboard()) {
            add(
                BottomNavItem(
                    route = Routes.DASHBOARD,
                    label = "Dashboard",
                    icon = Icons.Default.Dashboard
                )
            )
        }

        add(
            BottomNavItem(
                route = Routes.MEDALHAS,
                label = "Medalhas",
                icon = Icons.Default.EmojiEvents
            )
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.IDEIAS) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(text = item.label)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,

                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (currentRoute) {
                Routes.IDEIAS -> {
                    if (SessionManager.podeCadastrarIdeia()) {
                        FloatingActionButton(
                            onClick = {
                                navController.navigate(Routes.CRIAR_IDEIA)
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Cadastrar nova ideia"
                            )
                        }
                    }
                }

                Routes.ORIENTACOES -> {
                    if (SessionManager.podeGerenciarOrientacoes()) {
                        FloatingActionButton(
                            onClick = {
                                navController.navigate(Routes.CRIAR_ORIENTACAO)
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Cadastrar orientação"
                            )
                        }
                    }
                }
                Routes.PROJETOS -> {
                    if (SessionManager.podeGerenciarProjetos()) {
                        FloatingActionButton(
                            onClick = {
                                navController.navigate(Routes.CRIAR_PROJETO)
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Cadastrar projeto"
                            )
                        }
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.IDEIAS,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.CATEGORIAS) {
                CategoriasScreen()
            }

            composable(Routes.ORIENTACOES) {
                OrientacoesScreen(
                    podeGerenciarOrientacoes = SessionManager.podeGerenciarOrientacoes(),
                    onCriarOrientacaoClick = {
                        navController.navigate(Routes.CRIAR_ORIENTACAO)
                    },
                    onEditarOrientacaoClick = { orientacaoId ->
                        navController.navigate(Routes.editarOrientacaoRoute(orientacaoId))
                    }
                )
            }

            composable(Routes.IDEIAS) {
                IdeiasScreen(
                    podeVotarEmIdeia = SessionManager.podeVotarEmIdeia(),
                    podeGerenciarIdeias = SessionManager.podeGerenciarIdeias(),
                    onCriarIdeiaClick = {
                        navController.navigate(Routes.CRIAR_IDEIA)
                    },
                    onEditarClick = { ideiaId ->
                        navController.navigate(Routes.editarIdeiaRoute(ideiaId))
                    }
                )
            }

            composable(Routes.CRIAR_IDEIA) {
                CriarIdeiaScreen(
                    onVoltarClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.PROJETOS) {
                ProjetosScreen(
                    podeGerenciarProjetos = SessionManager.podeGerenciarProjetos(),
                    onEditarProjetoClick = { projetoId ->
                        navController.navigate(Routes.editarProjetoRoute(projetoId))
                    }
                )
            }

            composable(Routes.MEDALHAS) {
                MedalhasScreen()
            }

            composable(Routes.CRIAR_ORIENTACAO) {
                CriarOrientacaoScreen(
                    onVoltarClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "${Routes.EDITAR_ORIENTACAO}/{orientacaoId}",
                arguments = listOf(
                    navArgument("orientacaoId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val orientacaoId = backStackEntry.arguments?.getLong("orientacaoId") ?: return@composable

                EditarOrientacaoScreen(
                    orientacaoId = orientacaoId,
                    onVoltarClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "${Routes.EDITAR_IDEIA}/{ideiaId}",
                arguments = listOf(
                    navArgument("ideiaId") {
                        type = NavType.LongType
                    }
                )

            ) { backStackEntry ->
                val ideiaId = backStackEntry.arguments?.getLong("ideiaId") ?: return@composable

                EditarIdeiaScreen(
                    ideiaId = ideiaId,
                    onVoltarClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.CRIAR_PROJETO) {
                CriarProjetoScreen(
                    onVoltarClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "${Routes.EDITAR_PROJETO}/{projetoId}",
                arguments = listOf(
                    navArgument("projetoId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val projetoId = backStackEntry.arguments?.getLong("projetoId") ?: return@composable

                EditarProjetoScreen(
                    projetoId = projetoId,
                    onVoltarClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.DASHBOARD) {
                if (SessionManager.podeAcessarDashboard()) {
                    DashboardScreen()
                } else {
                    Text(text = "Acesso restrito ao perfil Líder.")
                }
            }
        }
    }
}



@Preview(showBackground = false)
@Composable
private fun AppNavigationPreview() {
    AguiaExpressTheme() {
        AppNavigation()
    }
}
