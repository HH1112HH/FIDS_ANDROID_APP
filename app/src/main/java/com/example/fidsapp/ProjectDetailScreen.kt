package com.example.fidsapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectName: String,
    tasks: List<RoadmapTask>,
    currentUser: User?,
    viewModel: HistoryViewModel,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<RoadmapTask?>(null) }
    var diaryContent by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(projectName, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FF))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tasks.forEach { task ->
                val existingDiary = viewModel.diaryHistory.find { it.day == task.day }
                
                TaskItem(
                    task = task, 
                    isDone = existingDiary != null,
                    onAction = {
                        if (task.hasDiary) {
                            selectedTask = task
                            diaryContent = existingDiary?.userText ?: ""
                            showDialog = true
                        } else {
                            // Đánh dấu hoàn thành cho các ngày không có nhật ký (7, 9, 10)
                            currentUser?.username?.let { uname ->
                                viewModel.markTaskAsCompleted(uname, task)
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút hoàn thành lộ trình ở cuối trang
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("XÁC NHẬN HOÀN THÀNH GIAI ĐOẠN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (diaryContent.isNotEmpty()) "Sửa nhật ký - Ngày ${selectedTask?.day}" else "Viết nhật ký - Ngày ${selectedTask?.day}") },
                text = {
                    OutlinedTextField(
                        value = diaryContent,
                        onValueChange = { diaryContent = it },
                        placeholder = { Text("Hôm nay bạn cảm thấy thế nào?...") },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        scope.launch {
                            val username = currentUser?.username ?: return@launch
                            viewModel.upsertDiary(
                                username = username,
                                userWrittenText = diaryContent,
                                task = selectedTask!!
                            )
                            showDialog = false
                            diaryContent = ""
                        }
                    }) { Text("Lưu") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Hủy") } }
            )
        }
    }
}

@Composable
fun TaskItem(task: RoadmapTask, isDone: Boolean, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDone) Color(0xFFE8F5E9) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Ngày ${task.day} – ${task.category}", fontWeight = FontWeight.Bold, color = if (isDone) Color(0xFF2E7D32) else Color(0xFF4CAF50))
                Spacer(modifier = Modifier.height(4.dp))
                Text(task.description, fontSize = 14.sp, color = Color.DarkGray)
            }
            
            if (task.hasDiary) {
                // Icon viết nhật ký
                IconButton(onClick = onAction) {
                    Icon(
                        imageVector = Icons.Default.EditNote, 
                        contentDescription = "Action",
                        tint = if (isDone) Color(0xFF2E7D32) else Color(0xFF4CAF50)
                    )
                }
            } else {
                // Nút đánh dấu hoàn thành cho ngày 7, 9, 10
                Button(
                    onClick = onAction,
                    enabled = !isDone,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDone) Color(0xFF2E7D32) else Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    if (isDone) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Đã làm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
