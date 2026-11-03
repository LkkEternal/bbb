package com.chanle.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chanle.app.AppViewModel
import com.chanle.app.ui.components.DishCard
import com.chanle.app.ui.components.EmptyState

@Composable
fun FavoritesScreen(
    vm: AppViewModel,
    onOpenDish: (String) -> Unit,
    onGoHome: () -> Unit,
    onGoAi: () -> Unit,
) {
    val curated by vm.curated.collectAsState()
    val mine by vm.mine.collectAsState()
    val favorites by vm.favorites.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }

    val favoriteDishes = (curated + mine).filter { it.id in favorites }
    val aiDishes = mine.sortedByDescending { it.createdAt }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 10.dp)) {
            Text(
                text = "收藏",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "留着想做的菜，和 AI 生成过的菜谱。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        TabRow(selectedTabIndex = tab) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                text = { Text("收藏 ${favoriteDishes.size}") },
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                text = { Text("我的菜谱 ${aiDishes.size}") },
            )
        }

        if (tab == 0) {
            if (favoriteDishes.isEmpty()) {
                EmptyState(
                    title = "还没有收藏",
                    message = "在菜谱详情页点右上角的心形，就能把菜留在这里。",
                    actionLabel = "去首页看看",
                    onAction = onGoHome,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(favoriteDishes, key = { it.id }) { dish ->
                        DishCard(dish = dish, onClick = { onOpenDish(dish.id) })
                    }
                }
            }
        } else {
            if (aiDishes.isEmpty()) {
                EmptyState(
                    title = "还没有 AI 菜谱",
                    message = "去「识菜」输入菜名生成一道，满意的就保存到这里。",
                    actionLabel = "去识菜",
                    onAction = onGoAi,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(aiDishes, key = { it.id }) { dish ->
                        DishCard(dish = dish, onClick = { onOpenDish(dish.id) })
                    }
                }
            }
        }
    }
}
