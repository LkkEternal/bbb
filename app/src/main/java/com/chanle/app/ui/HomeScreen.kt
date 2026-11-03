package com.chanle.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chanle.app.AppViewModel
import com.chanle.app.data.DishCategory
import com.chanle.app.ui.components.CategoryVisual
import com.chanle.app.ui.components.DishCard
import com.chanle.app.ui.components.EmptyState
import com.chanle.app.ui.components.SectionHeader
import kotlin.random.Random

@Composable
fun HomeScreen(
    vm: AppViewModel,
    onOpenDish: (String) -> Unit,
    onOpenCategory: (String) -> Unit,
) {
    val curated by vm.curated.collectAsState()
    val mine by vm.mine.collectAsState()
    val loading by vm.loading.collectAsState()
    val loadError by vm.loadError.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }

    val results = remember(query, curated, mine) { vm.search(query) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text(
                    text = "馋了",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "今天想吃点什么？",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜菜名、食材，比如“排骨”") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "清空")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
        }

        if (loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (loadError != null) {
            item {
                EmptyState(
                    title = "菜谱没加载出来",
                    message = loadError.orEmpty(),
                )
            }
        } else if (query.isNotBlank()) {
            item {
                Text(
                    text = "搜索“${query.trim()}” · ${results.size} 个结果",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (results.isEmpty()) {
                item {
                    EmptyState(
                        title = "没找到这道菜",
                        message = "换个关键词试试，或者去「识菜」让 AI 生成一道。",
                    )
                }
            } else {
                items(results, key = { it.id }) { dish ->
                    DishCard(dish = dish, onClick = { onOpenDish(dish.id) })
                }
            }
        } else {
            item {
                SectionHeader(title = "分类")
            }
            items(DishCategory.entries.chunked(2)) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { category ->
                        val count = curated.count { it.category == category.label }
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenCategory(category.label) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CategoryVisual(
                                    category = category.label,
                                    modifier = Modifier.size(44.dp),
                                    corner = 14.dp,
                                    symbolSize = 20,
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = category.label,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = "$count 道",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(
                            text = "选择困难？",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "从精选菜谱里随机挑一道，今晚就它了。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (curated.isNotEmpty()) {
                                    onOpenDish(curated[Random.nextInt(curated.size)].id)
                                }
                            },
                        ) {
                            Text("随机来一道")
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "精选菜谱",
                    actionText = "${curated.size} 道",
                )
            }
            items(curated.take(8), key = { it.id }) { dish ->
                DishCard(dish = dish, onClick = { onOpenDish(dish.id) })
            }
        }
    }
}
