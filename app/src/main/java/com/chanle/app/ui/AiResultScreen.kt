package com.chanle.app.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.chanle.app.AppViewModel
import com.chanle.app.ui.components.EmptyState

@Composable
fun AiResultScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val pending by vm.pendingDish.collectAsState()
    val dish = pending
    var showConflict by remember { mutableStateOf(false) }

    if (dish == null) {
        EmptyState(
            title = "还没有生成结果",
            message = "回到「识菜」输入菜名，生成后再来保存。",
            actionLabel = "返回",
            onAction = onBack,
        )
        return
    }

    DetailScreen(
        dish = dish,
        isFavorite = false,
        isPreview = true,
        onBack = onBack,
        onToggleFavorite = {},
        onSave = {
            if (vm.findMineByName(dish.name) != null) {
                showConflict = true
            } else {
                vm.saveAiDish(dish) { id -> onSaved(id) }
            }
        },
        onDiscard = onBack,
    )

    if (showConflict) {
        AlertDialog(
            onDismissRequest = { showConflict = false },
            title = { Text("已经有同名菜谱") },
            text = { Text("「我的菜谱」里已经有一道「${dish.name}」，保存会覆盖旧的那一份。") },
            confirmButton = {
                TextButton(onClick = {
                    showConflict = false
                    vm.saveAiDish(dish) { id -> onSaved(id) }
                }) {
                    Text("覆盖保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConflict = false }) {
                    Text("取消")
                }
            },
        )
    }
}
