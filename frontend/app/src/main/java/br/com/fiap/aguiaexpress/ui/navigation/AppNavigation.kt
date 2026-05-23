package br.com.fiap.aguiaexpress.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
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

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem(
            route = Routes.ORIENTACOES,
            label = "Orientações",
            icon = Icons.Default.Flag
        ),
        BottomNavItem(
            route = Routes.IDEIAS,
            label = "Ideias",
            icon = Icons.Default.Lightbulb
        ),
        BottomNavItem(
            route = Routes.PROJETOS,
            label = "Projetos",
            icon = Icons.Default.Work
        ),
        BottomNavItem(
            route = Routes.MEDALHAS,
            label = "Medalhas",
            icon = Icons.Default.EmojiEvents
        )
    )

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
            if (currentRoute == Routes.IDEIAS) {
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
}

@Preview(showBackground = false)
@Composable
private fun AppNavigationPreview() {
    AguiaExpressTheme() {
        AppNavigation()
    }
}