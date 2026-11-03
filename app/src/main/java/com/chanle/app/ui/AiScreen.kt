package com.chanle.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.chanle.app.AppViewModel
import com.chanle.app.ui.components.DishCard
import com.chanle.app.ui.components.SectionHeader

@Composable
fun AiScreen(
    vm: AppViewModel,
    onOpenDish: (String) -> Unit,
    onOpenResult: () -> Unit,
    onOpenFavorites: () -> Unit,
) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("按食材搜索", "拍照识别")
    
    var ingredientText by rememberSaveable { mutableStateOf("") }
    var searchResults by rememberSaveable { mutableStateOf<List<com.chanle.app.data.Dish>>(emptyList()) }
    var hasSearched by rememberSaveable { mutableStateOf(false) }
    
    var recognizedIngredients by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var imageSearchResults by rememberSaveable { mutableStateOf<List<com.chanle.app.data.Dish>>(emptyList()) }
    var hasRecognized by rememberSaveable { mutableStateOf(false) }
    var isProcessing by rememberSaveable { mutableStateOf(false) }
    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var recognitionError by rememberSaveable { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && selectedImageUri != null) {
            isProcessing = true
            recognitionError = null
            vm.recognizeIngredientsFromImage(selectedImageUri!!) { result ->
                isProcessing = false
                result.onSuccess { ingredients ->
                    recognizedIngredients = ingredients
                    hasRecognized = true
                    if (ingredients.isNotEmpty()) {
                        imageSearchResults = vm.searchByIngredients(ingredients)
                    } else {
                        imageSearchResults = emptyList()
                    }
                }.onFailure { error ->
                    recognitionError = error.message ?: "识别失败"
                    hasRecognized = true
                    recognizedIngredients = emptyList()
                    imageSearchResults = emptyList()
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "temp_image_${System.currentTimeMillis()}.jpg")
                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            selectedImageUri = context.contentResolver.insert(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            selectedImageUri?.let { cameraLauncher.launch(it) }
        } else {
            Toast.makeText(context, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            isProcessing = true
            recognitionError = null
            vm.recognizeIngredientsFromImage(it) { result ->
                isProcessing = false
                result.onSuccess { ingredients ->
                    recognizedIngredients = ingredients
                    hasRecognized = true
                    if (ingredients.isNotEmpty()) {
                        imageSearchResults = vm.searchByIngredients(ingredients)
                    } else {
                        imageSearchResults = emptyList()
                    }
                }.onFailure { error ->
                    recognitionError = error.message ?: "识别失败"
                    hasRecognized = true
                    recognizedIngredients = emptyList()
                    imageSearchResults = emptyList()
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text(
                    text = "智识",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "智能识别食材和菜品，为你推荐合适的菜谱",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text(
                                text = "输入食材",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "输入你有的食材，多个食材用逗号或空格分隔",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = ingredientText,
                                onValueChange = { ingredientText = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("例如：西红柿，鸡蛋，葱") },
                                placeholder = { Text("输入食材名称，多个食材用逗号分隔") },
                                singleLine = false,
                                maxLines = 3,
                                shape = RoundedCornerShape(14.dp),
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val ingredients = ingredientText
                                        .split("[，,]".toRegex())
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }

                                    if (ingredients.isNotEmpty()) {
                                        searchResults = vm.searchByIngredients(ingredients)
                                        hasSearched = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = ingredientText.isNotBlank(),
                            ) {
                                Text("搜索菜谱")
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "支持多个食材组合搜索，系统会找出包含任意一个或多个食材的菜谱",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (hasSearched) {
                    if (searchResults.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "未找到相关菜谱",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "试试其他食材组合，或者换个说法",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            SectionHeader(
                                title = "找到 ${searchResults.size} 个菜谱",
                                actionText = null,
                                onAction = {},
                            )
                        }
                        itemsIndexed(searchResults) { index, dish ->
                            DishCard(dish = dish, onClick = { onOpenDish(dish.id) })
                        }
                    }
                }
            }
            1 -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text(
                                text = "拍照识别菜品",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "拍摄或选择菜品图片，AI 识别食材后为你推荐菜谱",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val permission = Manifest.permission.CAMERA
                                        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                                            val contentValues = android.content.ContentValues().apply {
                                                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "temp_image_${System.currentTimeMillis()}.jpg")
                                                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                            }
                                            selectedImageUri = context.contentResolver.insert(
                                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                contentValues
                                            )
                                            selectedImageUri?.let { cameraLauncher.launch(it) }
                                        } else {
                                            cameraPermissionLauncher.launch(permission)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "拍照",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("拍照")
                                }
                                OutlinedButton(
                                    onClick = {
                                        galleryLauncher.launch("image/*")
                                    },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "相册",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("相册")
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "拍摄时请尽量包含菜品全景，避免遮挡",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (isProcessing) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "正在识别食材...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

                if (hasRecognized && !isProcessing) {
                    recognitionError?.let { error ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                ),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "识别失败",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }

                    if (recognitionError == null && recognizedIngredients.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "未能识别出食材",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "请尝试拍摄更清晰的菜品图片",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        text = "识别到的食材",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = recognizedIngredients.joinToString("、"),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }

                        item { Spacer(Modifier.height(4.dp)) }

                        if (imageSearchResults.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "未找到相关菜谱",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = "数据库中暂无包含这些食材的菜谱",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        } else {
                            item {
                                SectionHeader(
                                    title = "为你推荐 ${imageSearchResults.size} 个菜谱",
                                    actionText = null,
                                    onAction = {},
                                )
                            }
                            itemsIndexed(imageSearchResults) { index, dish ->
                                DishCard(dish = dish, onClick = { onOpenDish(dish.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}