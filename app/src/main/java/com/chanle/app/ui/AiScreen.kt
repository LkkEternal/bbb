package com.chanle.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chanle.app.AppViewModel
import com.chanle.app.GenerationState
import com.chanle.app.ui.components.AiBadge
import com.chanle.app.ui.components.AiConfigCard
import com.chanle.app.ui.components.DishCard
import com.chanle.app.ui.components.SectionHeader

private val exampleDishes = listOf("红烧肉", "番茄牛腩", "冬阴功汤", "提拉米苏", "葱油饼")

@Composable
fun AiScreen(
    vm: AppViewModel,
    onOpenDish: (String) -> Unit,
    onOpenResult: () -> Unit,
    onOpenFavorites: () -> Unit,
) {
    val config by vm.aiConfig.collectAsState()
    val generation by vm.generation.collectAsState()
    val mine by vm.mine.collectAsState()
    var name by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(generation) {
        if (generation is GenerationState.Success) {
            onOpenResult()
            vm.consumeGenerationSuccess()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "识菜",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    AiBadge()
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "输入菜名，AI 给你一份完整的用量、步骤和营养。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            AiConfigCard(vm = vm, initiallyExpanded = !config.isConfigured)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 30) name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("想吃什么？") },
                        placeholder = { Text("比如：糖醋排骨") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        exampleDishes.forEach { example ->
                            AssistChip(
                                onClick = { name = example },
                                label = { Text(example) },
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val local = vm.matchLocal(name)
                            if (local != null) {
                                onOpenDish(local.id)
                            } else {
                                vm.generate(name)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = name.isNotBlank() && config.isConfigured && config.canCall && generation !is GenerationState.Loading,
                    ) {
                        Text("生成菜谱")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = when {
                            !config.isConfigured -> "先在下面填好接口地址、模型名和 API Key。"
                            config.remainingToday == 0 -> "今日次数已用完，可在设置里调高上限。"
                            else -> "本地已有的菜会直接打开，不消耗 AI 次数。今日还可生成 ${config.remainingToday} 次。"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        when (val state = generation) {
            is GenerationState.Loading -> item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.padding(horizontal = 7.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "正在生成「${name.trim()}」…",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "通常需要 10–40 秒，请稍等。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = { vm.cancelGeneration() }) {
                            Text("取消")
                        }
                    }
                }
            }

            is GenerationState.Error -> item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "没生成成功",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(Modifier.height(10.dp))
                        Row {
                            Button(
                                onClick = { vm.generate(name) },
                                enabled = name.isNotBlank(),
                            ) {
                                Text("重试")
                            }
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            TextButton(onClick = { vm.clearGenerationError() }) {
                                Text("关闭")
                            }
                        }
                    }
                }
            }

            else -> Unit
        }

        if (mine.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "我的 AI 菜谱",
                    actionText = "查看全部",
                    onAction = onOpenFavorites,
                )
            }
            items(mine.sortedByDescending { it.createdAt }.take(5), key = { it.id }) { dish ->
                DishCard(dish = dish, onClick = { onOpenDish(dish.id) })
            }
        }
    }
}
