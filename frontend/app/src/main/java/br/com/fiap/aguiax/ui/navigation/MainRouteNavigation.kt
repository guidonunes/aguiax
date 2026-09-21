package br.com.fiap.aguiax.ui.navigation

import androidx.navigation.NavHostController

internal fun NavHostController.navigateToMainRoute(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        // Home is the retained anchor, not a saved tab. Restoring it can reopen
        // the child stack just saved by popUpTo instead of showing Home.
        restoreState = route != Routes.HOME
    }
}
