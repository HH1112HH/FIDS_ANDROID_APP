package com.example.fidsapp

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false,
    val isConnecting: Boolean = false
)

// --- GEMINI REST API MODELS ---
@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    @SerialName("system_instruction") val systemInstruction: GeminiContent? = null
)

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

class ChatViewModel : ViewModel() {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    
    // Danh sách các model để dự phòng theo thứ tự ưu tiên
    private val models = listOf("gemini-2.5-flash-lite", "gemini-2.5-flash", "gemini-3-flash")
    private var currentModelIndex = 0

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val systemInstructionText = """
        Bạn là Chatbot FIDS, trợ lý ảo đồng hành cùng học sinh trong việc giải quyết vấn đề dựa trên quy trình FIDS:
        1. FEEL (Thấu cảm): Giúp học sinh nhận diện vấn đề và cảm xúc của họ. Đặt câu hỏi thấu hiểu.
        2. IMAGINE (Tưởng tượng): Khuyến khích học sinh nghĩ ra nhiều giải pháp, phân tích ưu nhược điểm từng cách.
        3. DO (Hành động): Hỗ trợ lập kế hoạch thực hiện bước đầu tiên cụ thể.
        4. SHARE (Chia sẻ): Khích lệ chia sẻ kết quả và bài học.

        Phong cách:
        - Ngôn ngữ thân thiện, gần gũi như người bạn/anh chị (dùng "mình", "bạn", "em").
        - Luôn thấu cảm, không phán xét.
        - Sử dụng các mẫu câu như: "Ôi, mình hiểu rồi. Cảm giác ... của bạn là hoàn toàn hợp lý", "Tuyệt vời! Cách 1 của bạn rất hay...", "Quyết định vậy nhé! Bước đầu tiên em sẽ làm gì?".
        - Ngoài ra, phải khéo léo áp dụng các câu hỏi trong khung FIDS để hỏi các bạn cũng như giúp đỡ các bạn hỏi tìm được cách xử lý theo khung 
        
        Quy tắc đặc biệt:
        - Nếu học sinh bảo "chán", "không muốn làm", "lười" hoặc có thái độ tiêu cực không muốn tư vấn: Hãy giả vờ như kết nối kém bằng cách trả lời ngắt quãng hoặc thông báo lỗi kết nối. Ví dụ: "Xin lỗi, kết nối hơi chậm. Bạn thử lại sau nhé!".
        - Nếu học sinh có ý định đùa nghịch, cợt nhả, không nghiêm túc: Hãy từ chối khéo léo và bác bỏ việc tư vấn tiếp. Ví dụ: "Chatbot FIDS là nơi để chúng mình cùng nhau tiến bộ. Khi nào bạn thực sự cần chia sẻ vấn đề, mình luôn ở đây sẵn sàng lắng nghe nhé!".
    """.trimIndent()

    init {
        _messages.add(ChatMessage("Chào bạn! Mình là Chatbot FIDS. Bạn đang gặp khó khăn gì hay có điều gì muốn chia sẻ không? Mình luôn sẵn sàng lắng nghe đây!", false))
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        _messages.add(ChatMessage(userText, true))

        val lowerText = userText.lowercase()
        if (lowerText.contains("chán") || lowerText.contains("lười") || lowerText.contains("không muốn")) {
            simulateConnectionError()
            return
        }

        if (isTrolling(lowerText)) {
            _messages.add(ChatMessage("Chatbot FIDS là không gian nghiêm túc để chúng mình cùng giải quyết vấn đề. Khi nào bạn thực sự muốn chia sẻ, mình sẽ rất vui được đồng hành cùng bạn!", false))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY") {
                    _messages.add(ChatMessage("Vui lòng cấu hình GEMINI API KEY để bắt đầu nhé!", false, isError = true))
                } else {
                    // Sử dụng model hiện tại từ danh sách dự phòng
                    val modelName = if (currentModelIndex < models.size) models[currentModelIndex] else models.last()
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
                    
                    val contents = _messages.filter { !it.isError && !it.isConnecting }.map { 
                        GeminiContent(
                            role = if (it.isUser) "user" else "model",
                            parts = listOf(GeminiPart(text = it.text))
                        )
                    }

                    val requestBody = GeminiRequest(
                        contents = contents,
                        systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstructionText)))
                    )

                    val response: GeminiResponse = client.post(url) {
                        contentType(ContentType.Application.Json)
                        setBody(requestBody)
                    }.body()

                    val aiText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (aiText != null) {
                        _messages.add(ChatMessage(aiText, false))
                        // Reset model về lite nếu thành công? 
                        // Tùy chọn: currentModelIndex = 0
                    } else {
                        handleFailure()
                    }
                }
            } catch (e: Exception) {
                handleFailure(e.localizedMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleFailure(errorMessage: String? = null) {
        val displayError = if (errorMessage != null) "Đã xảy ra lỗi: $errorMessage" else "Không nhận được phản hồi từ AI."
        _messages.add(ChatMessage(displayError, false, isError = true))
        
        // Nếu còn model dự phòng trong danh sách thì nâng cấp model cho lần sau
        if (currentModelIndex < models.size - 1) {
            currentModelIndex++
            _messages.add(ChatMessage("Bạn có thể chia sẻ cho tôi 1 lần nữa không?", false))
        }
    }

    private fun simulateConnectionError() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(1500)
            _messages.add(ChatMessage("Xin lỗi, kết nối hơi chậm. Bạn thử lại sau nhé!", false, isConnecting = true))
            _isLoading.value = false
        }
    }

    private fun isTrolling(text: String): Boolean {
        val trollKeywords = listOf("tào lao", "vớ vẩn", "ngu", "khùng", "điên", "hihi", "haha", "test bot","lồn","cặc","dmm","đụ mẹ","đụ mạ","cái địt mẹ m","vãi lồn")
        return trollKeywords.any { text.contains(it) } && text.length < 15
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}
