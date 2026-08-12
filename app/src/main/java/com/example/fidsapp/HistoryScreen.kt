package com.example.fidsapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Khảo sát", "Nhật ký")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử hoạt động", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF4CAF50)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium) }
                    )
                }
            }

            when (selectedTab) {
                0 -> SurveyHistoryList(viewModel.surveyHistory)
                1 -> DiaryHistoryList(viewModel.diaryHistory)
            }
        }
    }
}

@Composable
fun DiaryHistoryList(history: List<DiaryRecord>) {
    if (history.isEmpty()) {
        EmptyState("Chưa có nhật ký nào")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history) { record ->
                HistoryCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Book, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ngày ${record.day} - ${record.category}", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 13.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            // CHỈ HIỂN THỊ NGÀY (10 ký tự đầu)
                            Text(record.date.take(10), fontSize = 11.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = record.taskTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF1F8E9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = record.userText,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurveyHistoryList(history: List<SurveyRecord>) {
    if (history.isEmpty()) {
        EmptyState("Chưa có kết quả khảo sát nào")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(history) { record ->
                HistoryCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFF4CAF50)), contentAlignment = Alignment.Center) {
                            Text(text = record.score.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(record.type, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), fontSize = 15.sp)
                            // CHỈ HIỂN THỊ NGÀY (10 ký tự đầu)
                            Text("Ngày: ${record.date.take(10)}", fontSize = 13.sp)
                            Text("Đánh giá: ${record.evaluation}", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(content: @Composable () -> Unit) {
     Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Box(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}
