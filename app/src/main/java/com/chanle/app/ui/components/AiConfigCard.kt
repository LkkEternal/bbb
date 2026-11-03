package com.chanle.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.chanle.app.AppViewModel
import com.chanle.app.TestState
import androidx.compose.runtime.collectAsState

@Composable
fun AiConfigCard(
    vm: AppViewModel,
    initiallyExpanded: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val config by vm.aiConfig.collectAsState()
    val testState by vm.testState.collectAsState()
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded || !config.isConfigured) }
    var endpoint by rememberSaveable { mutableStateOf(config.endpointUrl) }
    var model by rememberSaveable { mutableStateOf(config.modelName) }
    var apiKey by rememberSaveable { mutableStateOf("") }
    var limit by rememberSaveable { mutableIntStateOf(config.dailyLimit) }
    var message by remember { mutableStateOf<String?>(null) }
    val masked = remember(config.hasKey) { vm.maskedKey() }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "AI 接口设置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (config.isConfigured) {
                            "${config.modelName} · 今日 ${config.usedToday}/${config.dailyLimit} 次"
                        } else {
                            "填好地址、模型和 Key 才能生成菜谱"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "收起" else "修改")
                }
            }

            if (!expanded) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (config.isConfigured) {
                        "累计 ${config.totalTokens} tokens · Key 已加密保存在本机"
                    } else {
                        "未配置时，浏览、收藏和份量换算都不受影响"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("接口地址") },
                    placeholder = { Text("https://api.deepseek.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("模型名称") },
                    placeholder = { Text("deepseek-chat") },
                    singleLine = true,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("API Key") },
                    placeholder = {
                        Text(
                            if (config.hasKey && masked != null) {
                                "已保存 $masked，留空表示不修改"
                            } else {
                                "sk-..."
                            },
                        )
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("每日上限", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { limit = (limit - 5).coerceAtLeast(1) }) {
                            Text("−")
                        }
                        Text(
                            text = "$limit 次",
                            modifier = Modifier.padding(horizontal = 14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        OutlinedButton(onClick = { limit = (limit + 5).coerceAtMost(100) }) {
                            Text("+")
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val error = vm.saveAiConfig(endpoint, model, apiKey, limit)
                            message = error ?: "设置已保存"
                            if (error == null) apiKey = ""
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("保存")
                    }
                    OutlinedButton(
                        onClick = {
                            message = null
                            vm.saveAndTest(endpoint, model, apiKey, limit)
                            apiKey = ""
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("保存并测试")
                    }
                }

                if (config.hasKey) {
                    TextButton(onClick = {
                        vm.clearAiKey()
                        message = "API Key 已清除"
                    }) {
                        Text("清除已保存的 Key", color = MaterialTheme.colorScheme.error)
                    }
                }

                when (val state = testState) {
                    is TestState.Testing -> Row(
                        modifier = Modifier.padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(Modifier.width(16.dp).height(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("正在测试连接…", style = MaterialTheme.typography.bodySmall)
                    }
                    is TestState.Success -> Text(
                        text = state.message,
                        modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    is TestState.Error -> Text(
                        text = state.message,
                        modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    TestState.Idle -> Unit
                }

                message?.let {
                    Text(
                        text = it,
                        modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "只认 OpenAI 兼容接口（POST /chat/completions），只填域名也会自动补全；仅支持 HTTPS。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "菜名会直接发送到你填写的服务商；本 App 没有服务器。Key 加密保存在本机，不进备份、不写日志。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
