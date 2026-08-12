package com.example.fidsapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillDetailScreen(skillId: String, onBack: () -> Unit) {
    val (title, color, content) = when (skillId) {
        "FEEL" -> Triple(
            "FEEL (Thấu cảm)", 
            Color(0xFFE91E63),
            "Bước này bắt đầu bằng việc yêu cầu bạn chậm lại và thấu hiểu tình huống trước khi vội vàng tìm cách giải quyết.\n\n" +
            "Tại sao điều này quan trọng?\n" +
            "Bởi vì nó giúp phát triển sự đồng cảm.\n\n" +
            "Khi bạn muốn thiết kế một giải pháp tốt hơn, bạn cần chuyển từ những giả định chủ quan sang sự thấu hiểu thực sự. Điều này chỉ xảy ra khi bạn tương tác với người sử dụng và cùng họ xây dựng giải pháp, thay vì áp đặt giải pháp cho họ."
        )
        "IMAGINE" -> Triple(
            "IMAGINE (Tưởng tượng)", 
            Color(0xFFFFB300),
            "Bước này yêu cầu bạn động não và hình dung ra những giải pháp mới nhằm cải thiện, làm phong phú hoặc thay đổi trải nghiệm của người dùng.\n\n" +
            "Tại sao điều này quan trọng?\n" +
            "Bởi vì nó giúp phát triển ý thức đạo đức và trách nhiệm.\n\n" +
            "Khi bạn lựa chọn đưa ra một giải pháp để thay đổi thực trạng, điều đó đồng nghĩa với việc bạn chấp nhận chịu trách nhiệm cho sự thay đổi đó. Tư duy này giúp bạn tin rằng mình không bất lực trước vấn đề, rằng sự thay đổi luôn có thể xảy ra và bạn có khả năng góp phần dẫn dắt sự thay đổi đó."
        )
        "DO" -> Triple(
            "DO (Hành động)", 
            Color(0xFF4CAF50),
            "Bước này nhấn mạnh khả năng sáng tạo và hành động kịp thời để hiện thực hóa ý tưởng.\n\n" +
            "Tại sao điều này quan trọng?\n" +
            "Bởi vì nó giúp phát triển tinh thần hướng tới sự xuất sắc.\n\n" +
            "Những hành động xuất phát từ ý định rõ ràng sẽ tạo ra kết quả như mong muốn. Đồng thời, việc chú ý đến từng chi tiết trong quá trình thực hiện giúp nâng cao chất lượng hành động, từ đó hình thành thói quen làm việc cẩn trọng và hướng tới sự hoàn thiện."
        )
        "SHARE" -> Triple(
            "SHARE (Chia sẻ)", 
            Color(0xFF9C27B0),
            "Bước cuối cùng là “Chia sẻ”, nhằm nuôi dưỡng tư duy về sự lan tỏa và phát triển chung.\n\n" +
            "Tại sao điều này quan trọng?\n" +
            "Bởi vì nó giúp phát triển tinh thần vươn cao và truyền cảm hứng cho người khác.\n\n" +
            "Vươn cao không chỉ là cạnh tranh để vượt qua người khác, mà còn là giúp người khác cùng tiến bộ và hoàn thiện. Cốt lõi của bước này là niềm tin rằng tinh thần “Tôi có thể” sẽ tạo ra hy vọng và truyền cảm hứng cho những thay đổi tích cực tiếp theo.\n\n" +
            "Hãy mạnh dạn chia sẻ câu chuyện của mình và truyền cảm hứng cho người khác.\n" +
            "Hãy trở thành sự thay đổi mà bạn mong muốn nhìn thấy!"
        )
        else -> Triple("Kỹ năng", Color.Gray, "Đang cập nhật nội dung...")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = color)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FF))
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = content,
                    modifier = Modifier.padding(28.dp),
                    fontSize = 18.sp, // Tăng kích thước chữ nội dung
                    lineHeight = 30.sp, // Tăng khoảng cách dòng để dễ đọc hơn
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF263238)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
