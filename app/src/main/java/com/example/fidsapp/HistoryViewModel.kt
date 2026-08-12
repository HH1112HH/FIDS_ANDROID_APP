package com.example.fidsapp

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- Models ---
data class SurveyRecord(val score: Int, val maxScore: Int, val date: String, val evaluation: String, val type: String)

data class DiaryRecord(
    val id: String? = null,
    val taskTitle: String, // 'content' trong DB (Mô tả nhiệm vụ)
    val userText: String,  // 'descript' trong DB (Nội dung người dùng viết)
    val date: String,
    val day: Int,
    val category: String
)

data class Question(val id: Int, val text: String, val category: String, val isReverse: Boolean = false)

data class RoadmapTask(
    val id: Int,
    val day: Int,
    val category: String,
    val description: String,
    val week: Int,
    val projectName: String,
    val hasDiary: Boolean = true
)

class HistoryViewModel : ViewModel() {
    val surveyHistory = mutableStateListOf<SurveyRecord>()
    val diaryHistory = mutableStateListOf<DiaryRecord>()
    
    // THEO DÕI TRẠNG THÁI HOÀN THÀNH CỦA CÁC DỰ ÁN
    val finishedProjects = mutableStateMapOf<String, Boolean>()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // --- Lộ trình 56 ngày (8 tuần) ---
    private fun generate56DaysTasks(): List<RoadmapTask> {
        val tasks = mutableListOf<RoadmapTask>()
        for (i in 1..56) {
            val week = ((i - 1) / 7) + 1
            val dayInCycle = (i - 1) % 14 + 1
            val projectNum = ((i - 1) / 14) + 1
            val projectName = "Project $projectNum"
            val category = when {
                dayInCycle <= 3 -> "FEEL"
                dayInCycle <= 5 -> "IMAGINE"
                dayInCycle <= 10 -> "DO"
                else -> "SHARE"
            }
            tasks.add(RoadmapTask(
                id = i,
                day = i,
                category = category,
                description = getTaskDescription(i),
                week = week,
                projectName = projectName,
                hasDiary = !(dayInCycle == 7 || dayInCycle == 9 || dayInCycle == 10)
            ))
        }
        return tasks
    }

    private fun getTaskDescription(day: Int): String {
        val cycleDay = (day - 1) % 56 + 1
        return when (cycleDay) {
            1 -> "Quan sát và ghi lại 1 điều khiến bạn trăn trở hôm nay."
            2 -> "Trò chuyện và lắng nghe cảm nhận của những người quanh bạn."
            3 -> "Đặt mình vào vị trí của người khác để thấu hiểu sâu sắc hơn."
            4 -> "Động não và liệt kê 3 ý tưởng giải quyết vấn đề sáng tạo."
            5 -> "Lựa chọn giải pháp tối ưu nhất và phân tích các mặt Lợi/Hại."
            6 -> "Xây dựng kế hoạch hành động chi tiết: Làm gì? Khi nào? Ai giúp?"
            7 -> "Bắt đầu thực hiện bước đi nhỏ đầu tiên trong kế hoạch."
            8 -> "Bạn hãy kể ra những việc mình đã làm qua nhật ký nhé"
            9 -> "Kiên trì thực hiện nhiệm vụ và vượt qua những khó khăn ban đầu."
            10 -> "Lan tỏa tinh thần và mời thêm bạn bè cùng tham gia hành động."
            11 -> "Chia sẻ câu chuyện thay đổi và trải nghiệm của bạn với mọi người."
            12 -> "Viết nhật ký tổng kết về bài học quý giá nhất bạn rút ra được."
            13 -> "Tự nhìn nhận và đánh giá những điểm mạnh, điểm cần cải thiện."
            14 -> "Hoàn thành giai đoạn rèn luyện và chuẩn bị cho thử thách mới."
            15 -> "Quan sát trong gia đình và ghi lại 1 điều khiến bạn cảm thấy khoảng cách với người thân."
            16 -> "Trò chuyện với bố mẹ hoặc người thân để lắng nghe cảm xúc của họ."
            17 -> "Đặt mình vào vị trí của người thân để hiểu suy nghĩ và áp lực của họ."
            18 -> "Liệt kê 3 cách giúp bạn kết nối tốt hơn với gia đình."
            19 -> "Chọn cách phù hợp nhất và phân tích các mặt Lợi/Hại."
            20 -> "Lập kế hoạch hành động cụ thể để cải thiện mối quan hệ gia đình."
            21 -> "Thực hiện một hành động nhỏ thể hiện sự quan tâm với gia đình."
            22 -> "Hãy ghi lại trong nhật ký những điều bạn đã làm cho gia đình hôm nay."
            23 -> "Kiên trì duy trì hành động tích cực với gia đình."
            24 -> "Rủ thêm một thành viên trong gia đình cùng tham gia."
            25 -> "Chia sẻ câu chuyện thay đổi trong mối quan hệ gia đình."
            26 -> "Viết nhật ký về điều bạn hiểu hơn về gia đình mình."
            27 -> "Tự đánh giá những điều bạn đã làm tốt và điều cần cải thiện."
            28 -> "Hoàn thành giai đoạn kết nối gia đình và rút ra bài học cho bản thân."
            29 -> "Quan sát thói quen sử dụng điện thoại của bạn trong một ngày."
            30 -> "Trò chuyện với bạn bè hoặc người thân về việc sử dụng điện thoại của mình."
            31 -> "Đặt mình vào góc nhìn của người khác khi thấy bạn dùng điện thoại quá nhiều."
            32 -> "Liệt kê 3 cách giúp giảm thời gian sử dụng điện thoại."
            33 -> "Chọn giải pháp phù hợp nhất và phân tích các mặt Lợi/Hại."
            34 -> "Lập kế hoạch cụ thể để kiểm soát việc sử dụng điện thoại."
            35 -> "Thực hiện bước đầu tiên trong kế hoạch giảm sử dụng điện thoại."
            36 -> "Ghi lại trong nhật ký những thay đổi khi bạn giảm dùng điện thoại."
            37 -> "Kiên trì thực hiện kế hoạch dù gặp khó khăn."
            38 -> "Rủ thêm một người bạn cùng thực hiện thử thách giảm dùng điện thoại."
            39 -> "Chia sẻ trải nghiệm của bạn khi giảm thời gian sử dụng điện thoại."
            40 -> "Viết nhật ký về cảm xúc và thay đổi của bản thân."
            41 -> "Tự đánh giá những tiến bộ và điều cần tiếp tục cải thiện."
            42 -> "Hoàn thành thử thách và rút ra bài học về việc sử dụng công nghệ."
            43 -> "Quan sát khu vực xung quanh và ghi lại một vấn đề về môi trường hoặc an toàn."
            44 -> "Trò chuyện với người xung quanh để lắng nghe ý kiến của họ về vấn đề này."
            45 -> "Đặt mình vào vị trí của người bị ảnh hưởng bởi vấn đề đó."
            46 -> "Liệt kê 3 ý tưởng để cải thiện tình hình."
            47 -> "Chọn giải pháp khả thi nhất và phân tích các mặt Lợi/Hại."
            48 -> "Lập kế hoạch hành động để thực hiện giải pháp đã chọn."
            49 -> "Thực hiện hành động nhỏ đầu tiên vì môi trường hoặc cộng đồng."
            50 -> "Ghi lại những việc bạn đã làm để cải thiện môi trường xung quanh."
            51 -> "Kiên trì thực hiện hành động dù gặp khó khăn."
            52 -> "Mời thêm bạn bè hoặc người thân cùng tham gia hành động."
            53 -> "Chia sẻ câu chuyện hành động vì cộng đồng của bạn."
            54 -> "Viết nhật ký về điều ý nghĩa nhất bạn nhận được."
            55 -> "Tự đánh giá những đóng góp của bản thân cho cộng đồng."
            56 -> "Hoàn thành hành trình rèn luyện và tổng kết những thay đổi của bạn."
            else -> "Nhiệm vụ rèn luyện kỹ năng FIDS ngày thứ $day."
        }
    }

    val allRoadmapTasks = generate56DaysTasks()
    val project1Tasks = allRoadmapTasks.filter { it.day in 1..14 }
    val project2Tasks = allRoadmapTasks.filter { it.day in 15..28 }
    val project3Tasks = allRoadmapTasks.filter { it.day in 29..42 }
    val project4Tasks = allRoadmapTasks.filter { it.day in 43..56 }

    fun markProjectAsFinished(projectId: String) {
        finishedProjects[projectId] = true
    }

    fun setUserData(user: User?) {
        if (user == null) return
        viewModelScope.launch {
            surveyHistory.clear()
            user.surveys.forEach { s ->
                surveyHistory.add(SurveyRecord(
                    score = s.score ?: 0,
                    maxScore = if (s.content?.contains("Test") == true) 30 else 138,
                    date = s.created_at ?: "N/A",
                    evaluation = s.result ?: "Không có nhận xét",
                    type = s.content ?: "Khảo sát"
                ))
            }
            diaryHistory.clear()
            finishedProjects.clear()
            user.diaries.forEach { d ->
                val record = DiaryRecord(
                    id = d.id,
                    taskTitle = d.content ?: "",
                    userText = d.descript ?: "",
                    date = d.created_at ?: "N/A",
                    day = d.Day ?: 0,
                    category = d.Category ?: ""
                )
                diaryHistory.add(record)

                // Nếu là ngày 14, 28, 42, 56 và đã có nội dung, đánh dấu project xong
                if (d.Day != null && d.Day % 14 == 0 && !d.descript.isNullOrEmpty()) {
                    val pNum = d.Day / 14
                    finishedProjects["Project $pNum"] = true
                }
            }
            surveyHistory.sortByDescending { it.date }
            diaryHistory.sortByDescending { it.date }
        }
    }

    fun upsertDiary(username: String, userWrittenText: String, task: RoadmapTask) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val existing = SupabaseClient.client.from("diaries").select {
                    filter {
                        eq("username", username)
                        eq("Day", task.day)
                    }
                }.decodeSingleOrNull<Diary>()

                if (existing != null) {
                    SupabaseClient.client.from("diaries").update({
                        set("descript", userWrittenText)
                    }) { filter { eq("id", existing.id ?: "") } }
                } else {
                    val newDiary = Diary(
                        username = username,
                        content = task.description,
                        descript = userWrittenText,
                        Day = task.day,
                        Category = task.category
                    )
                    SupabaseClient.client.from("diaries").insert(newDiary)
                }
                refreshUserData(username)
            } catch (e: Exception) {
                Log.e("FIDS_DEBUG", "Lỗi: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markTaskAsCompleted(username: String, task: RoadmapTask) {
        upsertDiary(username, "Completed", task)
    }

    private fun refreshUserData(username: String) {
        viewModelScope.launch {
            try {
                val user = SupabaseClient.client.from("users")
                    .select(columns = Columns.raw("*, surveys(*), diaries(*)")) {
                        filter { eq("username", username) }
                    }.decodeSingleOrNull<User>()
                setUserData(user)
            } catch (e: Exception) { Log.e("FIDS_DEBUG", "Lỗi đồng bộ: ${e.message}") }
        }
    }

    // --- Survey Logic & Questions ---
    val questions = listOf(
        Question(1, "Khi đưa ra quyết định, bạn cân nhắc liệu hành động của bạn có gây ra hậu quả xấu cho người khác hay tập thể không.", "Responsibility"),
        Question(2, "Bạn cân nhắc đến lợi ích của người khác trước khi đưa ra một đề nghị.", "Responsibility"),
        Question(3, "Bạn xem xét thuận lợi và khó khăn mà mình đang gặp phải trước khi đưa ra một đề nghị.", "Responsibility"),
        Question(4, "Bạn nỗ lực để lựa chọn những hành động đem lại nhiều kết quả tích cực cho tập thể.", "Responsibility"),
        Question(5, "Bạn cân nhắc những điểm mạnh và điểm yếu của một biện pháp giải quyết vấn đề trước khi quyết định sử dụng nó.", "Responsibility"),
        Question(6, "Khi gặp một vấn đề, em cố gắng quan sát và lắng nghe để hiểu cảm xúc của những người liên quan.", "FEEL"),
        Question(7, "Em có thể nhận ra điều gì đang khiến người khác cảm thấy khó chịu, bất công hoặc buồn phiền.", "FEEL"),
        Question(8, "Em thường đưa ra quyết định mà không quan tâm nhiều đến cảm xúc hay khó khăn của người khác.", "FEEL", true),
        Question(9, "Khi chứng kiến một vấn đề xảy ra, bạn thường không cảm thấy mình có liên quan.", "FEEL", true),
        Question(10, "Bạn cố gắng nghĩ ra nhiều ý tưởng khác nhau, kể cả ý tưởng mới lạ.", "IMAGINE"),
        Question(11, "Bạn thích trao đổi, thảo luận với người khác để cùng nghĩ ra giải pháp.", "IMAGINE"),
        Question(12, "Bạn cho rằng chỉ cần giải pháp quen thuộc là đủ, không cần nghĩ cách làm mới.", "IMAGINE", true),
        Question(13, "Khi đã có ý tưởng, bạn thường không muốn nghe thêm ý kiến khác.", "IMAGINE", true),
        Question(14, "Bạn hình dung được viễn cảnh tích cực hơn nếu vấn đề được giải quyết.", "IMAGINE"),
        Question(15, "Trước khi quyết định, bạn suy nghĩ xem mỗi giải pháp ảnh hưởng thế nào đến người khác.", "IMAGINE"),
        Question(16, "Khi đã chọn được giải pháp, bạn sẵn sàng bắt tay vào hành động dù gặp khó khăn.", "DO"),
        Question(17, "Bạn thường cùng người khác lập kế hoạch cụ thể để thực hiện.", "DO"),
        Question(18, "Bạn thường ngại hành động vì sợ thất bại hoặc nghĩ mình không đủ khả năng.", "DO", true),
        Question(19, "Bạn có ý tưởng nhưng hiếm khi biến thành hành động cụ thể.", "DO", true),
        Question(20, "Sau khi làm, bạn thích chia sẻ lại trải nghiệm và kết quả với người khác.", "SHARE"),
        Question(21, "Bạn rút ra bài học và sẵn sàng chia sẻ để truyền cảm hứng.", "SHARE"),
        Question(22, "Bạn hiếm khi kể cho người khác nghe về những việc mình đã làm.", "SHARE", true),
        Question(23, "Khi không thành công, bạn thường tránh nói về nó và không muốn nhìn lại.", "SHARE", true)
    )

    fun calculateSurveyResult(answers: Map<Int, Int>, currentUser: User?): SurveyRecord {
        var totalScore = 0
        val fidsScores = mutableMapOf("FEEL" to 0, "IMAGINE" to 0, "DO" to 0, "SHARE" to 0)
        questions.forEach { q ->
            val rawAns = answers[q.id] ?: 1
            val finalAns = if (q.isReverse) 7 - rawAns else rawAns
            totalScore += finalAns
            if (fidsScores.containsKey(q.category)) {
                fidsScores[q.category] = fidsScores[q.category]!! + finalAns
            }
        }

        // Logic nhận xét FIDS
        val mainEvaluation = when {
            totalScore in 116..138 -> "Bạn có sự thấu cảm sâu sắc, tư duy sáng tạo cao, dám nghĩ dám làm và không ngại đối diện với thất bại."
            totalScore in 93..115 -> {
                val lowestCategory = fidsScores.filterKeys { it != "Responsibility" }.minByOrNull { it.value }?.key ?: ""
                "Bạn có nền tảng trách nhiệm tốt. Nhưng mà bạn có thiếu sót trong bước $lowestCategory."
            }
            totalScore in 70..92 -> {
                val sortedFids = fidsScores.filterKeys { it != "Responsibility" }.toList().sortedBy { it.second }
                val low1 = sortedFids.getOrNull(0)?.first ?: ""
                val low2 = sortedFids.getOrNull(1)?.first ?: ""
                "Có nền tảng trách nhiệm cơ bản nhưng quy trình FIDS chưa liền mạch. Bạn đang bị thiếu sót ở $low1 và $low2."
            }
            totalScore in 47..69 -> "Bạn có nền tảng trách nhiệm cơ bản nhưng quy trình FIDS chưa liền mạch."
            else -> "Báo động ! Thiếu hụt nghiêm trọng năng lực cảm xúc - xã hội, bạn cần được hỗ trợ ngay!"
        }

        // Nhận xét bổ sung cho ngưỡng 50%
        val alertComments = mutableListOf<String>()
        if ((fidsScores["FEEL"] ?: 0) <= 12) alertComments.add("Bạn cần lắng nghe người khác nhiều hơn trước khi hành động.")
        if ((fidsScores["IMAGINE"] ?: 0) <= 18) alertComments.add("Bạn cần tư duy thoát ra khỏi lối mòn, nghĩ thêm giải pháp mới.")
        if ((fidsScores["DO"] ?: 0) <= 12) alertComments.add("Bạn có ý tưởng tốt nhưng cần mạnh dạn hành động, đừng sợ thất bại nữa.")
        if ((fidsScores["SHARE"] ?: 0) <= 12) alertComments.add("Bạn làm tốt đấy nhưng cần biết cách rút kinh nghiệm cho bản thân cũng như mở lòng ra chia sẻ với người khác đi chứ.")

        val finalEvaluation = if (alertComments.isNotEmpty()) {
            "$mainEvaluation\n${alertComments.joinToString("\n")}"
        } else mainEvaluation

        val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val record = SurveyRecord(totalScore, 138, now, finalEvaluation, "Khảo sát")
        surveyHistory.add(0, record)
        currentUser?.username?.let { saveSurveyToCloud(it, totalScore, finalEvaluation, "Khảo sát") }
        return record
    }

    fun calculateTestResult(answers: Map<Int, Int>, type: String, currentUser: User?): SurveyRecord {
        var score = 0
        for (i in 1..5) score += answers[i] ?: 1
        
        val evaluation = when {
            score in 25..30 -> "Bạn có trách nhiệm cao. Bạn là người sẽ luôn chủ động phân tích thiệt hơn, đặt lợi ích tập thể và người khác lên trên/ngang bằng lợi ích cá nhân trước khi quyết định."
            score in 19..24 -> "Bạn có trách nhiệm khá. Bạn là người có ý thức về hệ quả hành động, nhưng đôi khi vẫn đưa ra quyết định thiên về thuận lợi cá nhân nhiều hơn."
            score in 12..18 -> "Bạn có trách nhiệm trung bình. Bạn là người nhận thức về trách nhiệm chưa rõ ràng, thường chỉ suy nghĩ thấu đáo khi có người khác nhắc nhở."
            else -> "Bạn rất thiếu trách nhiệm. Bạn là người thường ra quyết định bốc đồng, bỏ qua điểm mạnh/yếu của vấn đề và ít quan tâm đến việc hành động của mình ảnh hưởng tới ai."
        }

        val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val record = SurveyRecord(score, 30, now, evaluation, type)
        surveyHistory.add(0, record)
        currentUser?.username?.let { saveSurveyToCloud(it, score, evaluation, type) }
        return record
    }

    private fun saveSurveyToCloud(username: String, score: Int, result: String, content: String) {
        viewModelScope.launch {
            try {
                val surveyData = UserSurvey(username = username, content = content, score = score, result = result)
                SupabaseClient.client.from("surveys").insert(surveyData)
                refreshUserData(username)
            } catch (e: Exception) { Log.e("FIDS_DEBUG", "Lỗi lưu: ${e.message}") }
        }
    }
}
