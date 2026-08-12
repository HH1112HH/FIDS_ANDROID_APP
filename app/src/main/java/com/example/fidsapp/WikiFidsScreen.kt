package com.example.fidsapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiFidsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wiki FIDS", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back", 
                            tint = Color.White
                        )
                    }
                },
                // Chuyển nền TopAppBar sang màu Vàng đồng bộ
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFC107))
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WikiCard(
                title = "FEEL (Cảm nhận)",
                description = "Lắng nghe bản thân và thấu cảm với những vấn đề xung quanh bạn.",
                icon = Icons.Default.Psychology,
                iconColor = Color(0xFFE91E63)
            )
            WikiCard(
                title = "IMAGINE (Hình dung)",
                description = "Sáng tạo các giải pháp mới lạ để thay đổi tích cực cho vấn đề.",
                icon = Icons.Default.Psychology,
                iconColor = Color(0xFFFF9800)
            )
            WikiCard(
                title = "DO (Thực hiện)",
                description = "Bắt tay vào hành động thực tế để hiện thực hóa giải pháp của bạn.",
                icon = Icons.Default.Psychology,
                iconColor = Color(0xFF4CAF50)
            )
            WikiCard(
                title = "SHARE (Chia sẻ)",
                description = "Lan tỏa câu chuyện, truyền cảm hứng và rút ra bài học cho cộng đồng.",
                icon = Icons.Default.Psychology,
                iconColor = Color(0xFF9C27B0)
            )
        }
    }
}

@Composable
fun WikiCard(title: String, description: String, icon: ImageVector, iconColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
