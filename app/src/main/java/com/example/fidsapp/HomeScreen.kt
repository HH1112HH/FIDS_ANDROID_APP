package com.example.fidsapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    user: User?,
    viewModel: HistoryViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSurvey: (String) -> Unit,
    onNavigateToWiki: () -> Unit,
    onNavigateToSkill: (String) -> Unit,
    onNavigateToSOS: () -> Unit,
    onNavigateToBreathing: () -> Unit,
    onNavigateToRoadmap: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val userName = remember(user) { user?.name ?: "Bạn" }

    Scaffold(
        bottomBar = bottomBar
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FF))
        ) {
            HeaderBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                HomeHeader(userName)
                EmotionCard()

                QuickActionsSection(
                    onSurveyClick = { onNavigateToSurvey("Khảo sát") },
                    onHistoryClick = onNavigateToHistory,
                    onSOSClick = onNavigateToSOS
                )

                FidsSkillsSection(onSkillClick = onNavigateToSkill)
                
                StudentUtilitiesSection(
                    onWikiClick = onNavigateToWiki,
                    onBreathingClick = onNavigateToBreathing,
                    onTestInputClick = { onNavigateToSurvey("Test đầu vào") },
                    onTestOutputClick = { onNavigateToSurvey("Test đầu ra") },
                    onCommunityClick = onNavigateToCommunity,
                    onRoadmapClick = onNavigateToRoadmap
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun HeaderBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(
                color = Color(0xFF3F51B5),
                shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
            )
    )
}

@Composable
fun HomeHeader(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFE0E0FF)))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Chào mừng bạn đã trở lại,", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            Text(userName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun EmotionCard() {
    Card(
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Cảm xúc hôm nay của em?", fontWeight = FontWeight.Bold, color = Color(0xFF1A237E), fontSize = 17.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("😔", "😐", "🙂", "😁", "🤩").forEach { emoji ->
                    AnimatedEmojiItem(emoji)
                }
            }
        }
    }
}

@Composable
fun AnimatedEmojiItem(emoji: String) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    Text(
        text = emoji,
        fontSize = 36.sp,
        modifier = Modifier
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                scope.launch {
                    scale.animateTo(
                        targetValue = 1.4f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                    )
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                    )
                }
            }
    )
}

@Composable
fun QuickActionsSection(onSurveyClick: () -> Unit, onHistoryClick: () -> Unit, onSOSClick: () -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionItem("Khảo sát", Icons.Default.AccountBox, Color(0xFF673AB7), Modifier.weight(1f).clickable { onSurveyClick() })
        QuickActionItem("Lịch sử", Icons.Default.History, Color(0xFF009688), Modifier.weight(1f).clickable { onHistoryClick() })
        QuickActionItem("Hỗ trợ SOS", Icons.Default.Warning, Color(0xFFE53935), Modifier.weight(1f).clickable { onSOSClick() })
    }
}

@Composable
fun FidsSkillsSection(onSkillClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("Rèn luyện Kỹ năng FIDS", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1A237E))
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SkillCardItem("FEEL", "Đồng cảm", Icons.Default.Favorite, Color(0xFFE91E63), Modifier.weight(1f).clickable { onSkillClick("FEEL") })
            SkillCardItem("IMAGINE", "Hình dung", Icons.Default.Lightbulb, Color(0xFFFFB300), Modifier.weight(1f).clickable { onSkillClick("IMAGINE") })
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SkillCardItem("DO", "Thực hiện", Icons.AutoMirrored.Filled.DirectionsRun, Color(0xFF4CAF50), Modifier.weight(1f).clickable { onSkillClick("DO") })
            SkillCardItem("SHARE", "Chia sẻ", Icons.Default.RecordVoiceOver, Color(0xFF9C27B0), Modifier.weight(1f).clickable { onSkillClick("SHARE") })
        }
    }
}

@Composable
fun StudentUtilitiesSection(
    onWikiClick: () -> Unit, 
    onBreathingClick: () -> Unit,
    onTestInputClick: () -> Unit,
    onTestOutputClick: () -> Unit,
    onCommunityClick: () -> Unit,
    onRoadmapClick: () -> Unit
) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Box(modifier = Modifier.fillMaxWidth().background(color = Color(0xFF3F51B5), shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).padding(horizontal = 24.dp, vertical = 18.dp)) {
            Text("Tiện ích Học sinh", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
        }
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    UtilityGridItem("SOS Bọt biển", Icons.Default.Air, Color(0xFFE53935), Modifier.weight(1f).clickable { onBreathingClick() })
                    UtilityGridItem("Test Đầu vào", Icons.AutoMirrored.Filled.Login, Color(0xFF2196F3), Modifier.weight(1f).clickable { onTestInputClick() })
                    UtilityGridItem("Wiki FIDS", Icons.Default.MenuBook, Color(0xFFFFC107), Modifier.weight(1f).clickable { onWikiClick() })
                }
                Spacer(modifier = Modifier.height(28.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    UtilityGridItem("Lộ trình 2 tháng", Icons.Default.AutoGraph, Color(0xFF4CAF50), Modifier.weight(1f).clickable { onRoadmapClick() })
                    UtilityGridItem("Test Đầu ra", Icons.AutoMirrored.Filled.Logout, Color(0xFF9C27B0), Modifier.weight(1f).clickable { onTestOutputClick() })
                    UtilityGridItem("Cộng đồng", Icons.Default.Forum, Color(0xFF009688), Modifier.weight(1f).clickable { onCommunityClick() })
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SkillCardItem(title: String, desc: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(modifier = modifier.height(95.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(12.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = color, fontSize = 15.sp)
                Text(desc, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun UtilityGridItem(name: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Card(modifier = Modifier.size(68.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(30.dp))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, textAlign = TextAlign.Center)
    }
}
