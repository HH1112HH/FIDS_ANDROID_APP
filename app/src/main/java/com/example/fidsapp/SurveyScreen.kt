package com.example.fidsapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(
    type: String,
    questions: List<Question>,
    onComplete: (Map<Int, Int>) -> Unit,
    onBack: () -> Unit
) {
    val displayQuestions = if (type.contains("Test")) questions.take(5) else questions
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateMapOf<Int, Int>() }
    
    val currentQuestion = displayQuestions[currentQuestionIndex]
    val progress = (currentQuestionIndex + 1).toFloat() / displayQuestions.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(type, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Thanh tiến trình - Sửa lại sử dụng lambda
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = Color(0xFF7E57C2),
                trackColor = Color(0xFFE0E0E0)
            )
            
            Text(
                text = "Câu ${currentQuestionIndex + 1} / ${displayQuestions.size}",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Nội dung câu hỏi và 6 ô lựa chọn
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = currentQuestion.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = Color(0xFF1A237E),
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                
                // Lưới 6 lựa chọn (2 cột x 3 hàng)
                val options = listOf(
                    "Hoàn toàn không đúng", "Khá không đúng", "Hơi không đúng",
                    "Hơi đúng", "Khá đúng", "Rất đúng"
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.chunked(2).forEachIndexed { rowIndex, pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEachIndexed { colIndex, text ->
                                val value = rowIndex * 2 + colIndex + 1
                                OptionGridItem(
                                    value = value,
                                    text = text,
                                    isSelected = (answers[currentQuestion.id] == value),
                                    onSelect = { answers[currentQuestion.id] = value },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Nút điều hướng
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentQuestionIndex > 0) {
                    OutlinedButton(
                        onClick = { currentQuestionIndex-- },
                        modifier = Modifier.height(48.dp).weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Quay lại")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                val isLastQuestion = currentQuestionIndex == displayQuestions.size - 1
                val canContinue = answers.containsKey(currentQuestion.id)

                Button(
                    onClick = {
                        if (isLastQuestion) {
                            onComplete(answers.toMap())
                        } else {
                            currentQuestionIndex++
                        }
                    },
                    modifier = Modifier.height(48.dp).weight(1f),
                    enabled = canContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isLastQuestion) "HOÀN THÀNH" else "Tiếp theo")
                }
            }
        }
    }
}

@Composable
fun OptionGridItem(
    value: Int, 
    text: String, 
    isSelected: Boolean, 
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onSelect,
        modifier = modifier.height(85.dp), // Chiều cao hợp lý để hiện đủ 6 ô
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFFF3E5F5) else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF7E57C2) else Color(0xFFE0E0E0)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$value. $text",
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF7E57C2) else Color.DarkGray,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}
