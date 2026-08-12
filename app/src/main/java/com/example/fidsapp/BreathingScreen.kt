package com.example.fidsapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(onBack: () -> Unit) {
    var timeLeft by remember { mutableIntStateOf(15) }
    
    // Animation điều khiển kích thước bong bóng (từ 1.0 về 0.4 trong 15 giây)
    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Bắt đầu thu nhỏ bong bóng trong 15 giây
        scale.animateTo(
            targetValue = 0.4f,
            animationSpec = tween(durationMillis = 15000, easing = LinearEasing)
        )
    }

    LaunchedEffect(Unit) {
        // Bộ đếm ngược 15 giây
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SOS: Điều hòa hơi thở", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFC62828))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FF)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            Text(
                text = "Hít vào thật sâu và thở ra chậm rãi...",
                fontSize = 18.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Bong bóng màu xanh (Blue 400)
                Surface(
                    modifier = Modifier
                        .size(300.dp)
                        .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
                        .shadow(elevation = 30.dp, shape = CircleShape, spotColor = Color(0xFF42A5F5)),
                    shape = CircleShape,
                    color = Color(0xFF42A5F5)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Số đếm ngược hiển thị bên trong bong bóng
                        Text(
                            text = timeLeft.toString(),
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            if (timeLeft == 0) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.padding(bottom = 60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Tôi đã thấy ổn hơn", fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
