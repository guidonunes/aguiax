package br.com.fiap.aguiaexpress.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.fiap.aguiaexpress.core.session.RoleUiPolicy
import br.com.fiap.aguiaexpress.core.session.SessionManager
import br.com.fiap.aguiaexpress.ui.screens.IdeiasScreen
import br.com.fiap.aguiaexpress.ui.screens.auth.LoginScreen
import br.com.fiap.aguiaexpress.ui.screens.categorias.CategoriasScreen
import br.com.fiap.aguiaexpress.ui.screens.dashboard.DashboardScreen
import br.com.fiap.aguiaexpress.ui.screens.ideias.CriarIdeiaScreen
import br.com.fiap.aguiaexpress.ui.screens.ideias.EditarIdeiaScreen
import br.com.fiap.aguiaexpress.ui.screens.medalhas.MedalhasScreen
import br.com.fiap.aguiaexpress.ui.screens.orientacoes.CriarOrientacaoScreen
import br.com.fiap.aguiaexpress.ui.screens.orientacoes.EditarOrientacaoScreen
import br.com.fiap.aguiaexpress.ui.screens.orientacoes.OrientacoesScreen
import br.com.fiap.aguiaexpress.ui.screens.projetos.CriarProjetoScreen
import br.com.fiap.aguiaexpress.ui.screens.projetos.EditarProjetoScreen
import br.com.fiap.aguiaexpress.ui.screens.projetos.ProjetosScreen
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme


private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private fun bottomNavItem(route: String): BottomNavItem {
    return when (route) {
        Routes.ORIENTACOES -> BottomNavItem(route, "Orientações", Icons.Default.Flag)
        Routes.IDEIAS -> BottomNavItem(route, "Ideias", Icons.Default.Lightbulb)
        Routes.PROJETOS -> BottomNavItem(route, "Projetos", Icons.Default.Work)
        Routes.DASHBOARD -> BottomNavItem(route, "Dashboard", Icons.Default.Dashboard)
        Routes.MEDALHAS -> BottomNavItem(route, "Medalhas", Icons.Default.EmojiEvents)
        else -> error("Unsupported bottom navigation route: $route")
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val rolePolicy = SessionManager.uiPolicy()
    val bottomNavItems = visibleMainRoutes(rolePolicy).map(::bottomNavItem)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        topBar = {
            if (shouldShowBottomBar) {
                AppTopBar(
                    onLogoutClick = {
                        SessionManager.limparSessao()

                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        },
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
                    if (rolePolicy.canCreateIdeas) {
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
                    if (rolePolicy.canManageOrientations) {
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
                    if (rolePolicy.canManageProjects) {
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
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = { perfil ->
                        val startRoute = startRouteForRole(perfil)

                        navController.navigate(startRoute) {
                            popUpTo(Routes.LOGIN) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(Routes.CATEGORIAS) {
                RoleProtectedContent(
                    allowed = canAccessRoute(Routes.CATEGORIAS, rolePolicy),
                    onBack = navController::popBackStack
                ) {
                    CategoriasScreen()
                }
            }

            composable(Routes.ORIENTACOES) {
                RoleProtectedContent(
                    allowed = canAccessRoute(Routes.ORIENTACOES, rolePolicy),
                    onBack = navController::popBackStack
                ) {
                    OrientacoesScreen(
                        podeGerenciarOrientacoes = rolePolicy.canManageOrientations,
                        onCriarOrientacaoClick = {
                            navController.navigate(Routes.CRIAR_ORIENTACAO)
                        },
                        onEditarOrientacaoClick = { orientacaoId ->
                            navController.navigate(
                                Routes.editarOrientacaoRoute(orientacaoId)
                            )
                        }
                    )
                }
            }

            composable(Routes.IDEIAS) {
                RoleProtectedContent(
                    allowed = canAccessRoute(Routes.IDEIAS, rolePolicy),
                    onBack = navController::popBackStack
                ) {
                    IdeiasScreen(
                        podeEditarExcluirIdeia = rolePolicy.canEditOwnIdeas,
                        podeAtualizarStatusIdeia = rolePolicy.canReviewIdeas,
                        podeAvaliarIdeia = rolePolicy.canReviewIdeas,
                        podeSolicitarAvaliacaoIa =
                            rolePolicy.canRequestAiEvaluation,
                        podeVotarEmIdeia = rolePolicy.canVote,
                        usuarioIdLogado = SessionManager.usuarioId,
                        onCriarIdeiaClick = {
                            navController.navigate(Routes.CRIAR_IDEIA)
                        },
                        onEditarIdeiaClick = { ideiaId ->
                            navController.navigate(
                                Routes.editarIdeiaRoute(ideiaId)
                            )
                        }
                    )
                }
            }

            composable(Routes.CRIAR_IDEIA) {
                RoleProtectedContent(
                    allowed = canAccessRoute(Routes.CRIAR_IDEIA, rolePolicy),
                    onBack = navController::popBackStack
                ) {
                    CriarIdeiaScreen(onVoltarClick = navController::popBackStack)
                }
            }

            composable(Routes.PROJETOS) {
                RoleProtectedContent(
                    allowed = canAccessRoute(Routes.PROJETOS, rolePolicy),
                    onBack = navController::popBackStack
                ) {
                    ProjetosScreen(
                        podeGerenciarProjetos = rolePolicy.canManageProjects,
                        onEditarProjetoClick = { projetoId ->
                            navController.navigate(
                                Routes.editarProjetoRoute(projetoId)
                            )
                        }
                    )
                }
            }

            composable(Routes.MEDALHAS) {
                RoleProtectedContent(
                    allowed = canAccessRoute(Routes.MEDALHAS, rolePolicy),
                    onBack = navController::popBackStack
                ) {
                    MedalhasScreen()
                }
            }

            composable(Routes.CRIAR_ORIENTACAO) {
                RoleProtectedContent(
                    allowed = canAccessRoute(Routes.CRIAR_ORIENTACAO, rolePolicy),
                    onBack = navController::popBackStack
                ) {
                    CriarOrientacaoScreen(
                        onVoltarClick = navController::popBackStack
                    )
                }
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

                RoleProtectedContent(
                    allowed = canAccessRoute(Routes.EDITAR_ORIENTACAO, rolePolicy),
                    onBack = navController::popBackStack
                ) {
                    EditarOrientacaoScreen(
                        orientacaoId = orientacaoId,
                        onVoltarClick = navController::popBackStack
                    )
                }
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

                RoleProtectedContent(
                    allowed = canAccessRoute(Routes.EDITAR_IDEIA, rolePolicy),
                    onBack = navController::popBackStack
                ) {
                    EditarIdeiaScreen(
                        ideiaId = ideiaId,
                        onVoltarClick = navController::popBackStack
                    )
                }
            }

            composable(Routes.CRIAR_PROJETO) {
                RoleProtectedContent(
                    allowed = canAccessRoute(Routes.CRIAR_PROJETO, rolePolicy),
                    onBack = navController::popBackStack
                ) {
                    CriarProjetoScreen(onVoltarClick = navController::popBackStack)
                }
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

                RoleProtectedContent(
                    allowed = canAccessRoute(Routes.EDITAR_PROJETO, rolePolicy),
                    onBack = navController::popBackStack
                ) {
                    EditarProjetoScreen(
                        projetoId = projetoId,
                        onVoltarClick = navController::popBackStack
                    )
                }
            }

            composable(Routes.DASHBOARD) {
                RoleProtectedContent(
                    allowed = canAccessRoute(Routes.DASHBOARD, rolePolicy),
                    onBack = navController::popBackStack
                ) {
                    DashboardScreen()
                }
            }
        }
    }
}

@Composable
private fun RoleProtectedContent(
    allowed: Boolean,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    if (allowed) {
        content()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Acesso restrito",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = "Seu perfil não possui permissão para acessar esta funcionalidade.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        TextButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Voltar")
        }
    }
}

@Composable
private fun AppTopBar(
    onLogoutClick: () -> Unit
) {
    val nomeUsuario = SessionManager.nome ?: "usuário"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 28.dp,
                bottom = 12.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Bem-vindo,",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = nomeUsuario,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(
            onClick = onLogoutClick
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Sair",
                tint = MaterialTheme.colorScheme.primary
            )
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
