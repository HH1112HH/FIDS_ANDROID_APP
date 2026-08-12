package com.example.fidsapp

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSScreen(
    onBack: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToBreathing: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trung tâm SOS", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFC62828),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FF))
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Text(
                text = "Bạn đang gặp khó khăn?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE53935)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Đừng lo lắng, chúng tôi luôn ở đây để hỗ trợ bạn 24/7.",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Thẻ Tổng đài 111
            EmergencyCard(
                title = "Tổng đài Quốc gia",
                number = "111",
                description = "Bảo vệ Trẻ em & Tư vấn Tâm lý",
                icon = Icons.Default.Call,
                iconBgColor = Color(0xFF2196F3),
                btnColor = Color(0xFF2196F3),
                onCall = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:111"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Thẻ Cảnh sát 113
            EmergencyCard(
                title = "Cảnh sát Phản ứng nhanh",
                number = "113",
                description = "Hỗ trợ khẩn cấp an ninh",
                icon = Icons.Default.Shield,
                iconBgColor = Color(0xFFF44336),
                btnColor = Color(0xFFF44336),
                onCall = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:113"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Công cụ hỗ trợ tức thì",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ToolButton(
                    modifier = Modifier.weight(1f),
                    title = "Hít thở",
                    icon = Icons.Default.Air,
                    containerColor = Color(0xFFE0F2F1),
                    iconColor = Color(0xFF00897B),
                    onClick = onNavigateToBreathing
                )
                Spacer(modifier = Modifier.width(16.dp))
                ToolButton(
                    modifier = Modifier.weight(1f),
                    title = "Chat khẩn",
                    icon = Icons.Default.FlashOn,
                    containerColor = Color(0xFFFFF3E0),
                    iconColor = Color(0xFFFB8C00),
                    onClick = onNavigateToChat
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Lưu ý bảo mật
            Surface(
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD32F2F))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Lưu ý: Mọi thông tin của bạn đều được bảo mật tuyệt đối. Nếu bạn cảm thấy không an toàn, hãy gọi ngay cho 111.",
                        fontSize = 14.sp,
                        color = Color(0xFFD32F2F),
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun EmergencyCard(
    title: String,
    number: String,
    description: String,
    icon: ImageVector,
    iconBgColor: Color,
    btnColor: Color,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconBgColor,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(text = number, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text(text = description, fontSize = 12.sp, color = Color.Gray)
            }
            Button(
                onClick = onCall,
                colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                Text("GỌI", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun ToolButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, color = iconColor)
        }
    }
}
