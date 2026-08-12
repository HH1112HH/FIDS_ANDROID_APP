package com.example.fidsapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapScreen(
    viewModel: HistoryViewModel,
    onBack: () -> Unit,
    onNavigateToProject: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lộ trình 2 tháng", fontWeight = FontWeight.Bold, color = Color.White) },
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Lộ trình rèn luyện 8 tuần", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LOGIC TRẠNG THÁI VÀ MỞ KHÓA
            val p1Finished = viewModel.finishedProjects["Project 1"] ?: false
            val p2Finished = viewModel.finishedProjects["Project 2"] ?: false
            val p3Finished = viewModel.finishedProjects["Project 3"] ?: false

            ProjectCard(
                title = "Dự án 1: Cải thiện Lớp học",
                week = "Tuần 1 - Tuần 2",
                status = if (p1Finished) "Hoàn thành" else "Sẵn sàng",
                color = Color(0xFF4CAF50),
                isUnlocked = true,
                onClick = { onNavigateToProject("Project 1") }
            )

            ProjectCard(
                title = "Dự án 2: Kết nối Gia đình",
                week = "Tuần 3 - Tuần 4",
                status = if (p2Finished) "Hoàn thành" else if (p1Finished) "Sẵn sàng" else "Chưa mở",
                color = Color(0xFF81C784),
                isUnlocked = p1Finished,
                onClick = { onNavigateToProject("Project 2") }
            )

            ProjectCard(
                title = "Dự án 3: Cai nghiện Điện thoại",
                week = "Tuần 5 - Tuần 6",
                status = if (p3Finished) "Hoàn thành" else if (p2Finished) "Sẵn sàng" else "Chưa mở",
                color = Color(0xFFAED581),
                isUnlocked = p2Finished,
                onClick = { onNavigateToProject("Project 3") }
            )

            ProjectCard(
                title = "Dự án 4: An toàn & Cộng đồng",
                week = "Tuần 7 - Tuần 8",
                status = if (p3Finished) "Sẵn sàng" else "Chưa mở",
                color = Color(0xFFC5E1A5),
                isUnlocked = p3Finished,
                onClick = { onNavigateToProject("Project 4") }
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProjectCard(title: String, week: String, status: String, color: Color, isUnlocked: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(enabled = isUnlocked) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isUnlocked) Color.White else Color(0xFFF0F0F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(week, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isUnlocked) color else Color.Gray)
                Text(status, fontSize = 12.sp, color = if (status == "Hoàn thành") Color(0xFF2E7D32) else Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isUnlocked) Color.Black else Color.Gray)
        }
    }
}
