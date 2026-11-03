package com.chanle.app.ui

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chanle.app.AppViewModel
import com.chanle.app.UpdateState
import com.chanle.app.ui.components.EmptyState
import com.chanle.app.ui.update.UpdateDialog

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun ChanLeApp(vm: AppViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val items = remember {
        listOf(
            BottomItem("home", "首页", Icons.Filled.Home),
            BottomItem("ai", "智识", Icons.Filled.Search),
            BottomItem("favorites", "收藏", Icons.Filled.Favorite),
            BottomItem("profile", "我的", Icons.Filled.Person),
        )
    }
    val showBottomBar = items.any { it.route == currentRoute }

    val updateState by vm.updateState.collectAsState()

    if (updateState is UpdateState.Available) {
        val available = updateState as UpdateState.Available
        UpdateDialog(
            updateInfo = available.updateInfo,
            currentVersion = available.currentVersion,
            onDismiss = { vm.dismissUpdate() }
        )
    }

    fun goRoot(target: String) {
        navController.navigate(target) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openDish(id: String) {
        navController.navigate("detail/$id")
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = { goRoot(item.route) },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable("home") {
                HomeScreen(
                    vm = vm,
                    onOpenDish = { openDish(it) },
                    onOpenCategory = { category ->
                        navController.navigate("category/${Uri.encode(category)}")
                    },
                )
            }

            composable("ai") {
                AiScreen(
                    vm = vm,
                    onOpenDish = { openDish(it) },
                    onOpenResult = { navController.navigate("aiResult") },
                    onOpenFavorites = {
                        goRoot("favorites")
                    },
                )
            }

            composable("favorites") {
                FavoritesScreen(
                    vm = vm,
                    onOpenDish = { openDish(it) },
                    onGoHome = { goRoot("home") },
                    onGoAi = { goRoot("ai") },
                )
            }

            composable("profile") {
                ProfileScreen(vm = vm)
            }

            composable("category/{category}") { entry ->
                val label = Uri.decode(entry.arguments?.getString("category").orEmpty())
                CategoryScreen(
                    vm = vm,
                    category = label,
                    onBack = { navController.popBackStack() },
                    onOpenDish = { openDish(it) },
                )
            }

            composable("detail/{id}") { entry ->
                val id = entry.arguments?.getString("id")
                val curated by vm.curated.collectAsState()
                val mine by vm.mine.collectAsState()
                val favorites by vm.favorites.collectAsState()
                val dish = remember(id, curated, mine) { vm.findDish(id) }

                if (dish == null) {
                    EmptyState(
                        title = "这道菜找不到了",
                        message = "它可能已经被删除，回上一页再看看。",
                        actionLabel = "返回",
                        onAction = { navController.popBackStack() },
                    )
                } else {
                    DetailScreen(
                        dish = dish,
                        isFavorite = dish.id in favorites,
                        isPreview = false,
                        onBack = { navController.popBackStack() },
                        onToggleFavorite = { vm.toggleFavorite(dish.id) },
                        onDelete = if (dish.isAi) {
                            {
                                vm.deleteMyDish(dish.id)
                                navController.popBackStack()
                            }
                        } else {
                            null
                        },
                    )
                }
            }

            composable("aiResult") {
                AiResultScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onSaved = { id ->
                        navController.navigate("detail/$id") {
                            popUpTo("aiResult") { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}