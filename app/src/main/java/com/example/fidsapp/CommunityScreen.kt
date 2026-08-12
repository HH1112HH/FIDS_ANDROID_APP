package com.example.fidsapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

// --- MODELS ---

@Serializable
data class Topic(
    val topic_id: Long? = null,
    val username: String? = null, // Khóa ngoại -> users.username
    val title: String? = null,
    val content: String? = null,
    val is_locked: Boolean = false,
    val created_at: String? = null,
    @SerialName("author") val users: UserMinimal? = null // Join alias 'author'
)

@Serializable
data class UserMinimal(val name: String? = null)

@Serializable
data class Comment(
    val comment_id: String? = null,
    val topic_id: Long? = null,
    val username: String? = null, // Khóa ngoại -> users.username
    val content: String? = null,
    val created_at: String? = null,
    val deleted_at: String? = null,
    @SerialName("commenter") val users: UserMinimal? = null // Join alias 'commenter'
)

@Serializable
data class BannedWord(
    val word_id: String? = null,
    val word: String? = null
)

// --- VIEWMODEL ---

class CommunityViewModel : ViewModel() {
    private val _topics = MutableStateFlow<List<Topic>>(emptyList())
    val topics = _topics.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _bannedWords = mutableListOf<String>()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchTopics()
        fetchBannedWords()
    }

    fun fetchTopics() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Join bảng users thông qua username để lấy name của tác giả
                val result = SupabaseClient.client.from("topics")
                    .select(columns = Columns.raw("*, author:users(name)"))
                    .decodeList<Topic>()
                _topics.value = result.sortedByDescending { it.created_at }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchBannedWords() {
        viewModelScope.launch {
            try {
                val words = SupabaseClient.client.from("banned_words").select().decodeList<BannedWord>()
                _bannedWords.clear()
                _bannedWords.addAll(words.mapNotNull { it.word?.lowercase() })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchComments(topicId: Long) {
        viewModelScope.launch {
            try {
                // Join bảng users lấy name người bình luận và lọc comment chưa bị xóa
                val result = SupabaseClient.client.from("comments")
                    .select(columns = Columns.raw("*, commenter:users(name)")) {
                        filter { 
                            eq("topic_id", topicId)
                            filter("deleted_at", FilterOperator.IS, null)
                        }
                    }.decodeList<Comment>()
                _comments.value = result.sortedBy { it.created_at }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createTopic(username: String, title: String, content: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val newTopic = Topic(username = username, title = title, content = content)
                SupabaseClient.client.from("topics").insert(newTopic)
                fetchTopics()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun postComment(topicId: Long, username: String, content: String, onBlocked: (String) -> Unit) {
        val lowerContent = content.lowercase()
        // LOGIC CHẶN CỨNG (Hard Block)
        if (_bannedWords.any { lowerContent.contains(it) }) {
            onBlocked("Bình luận của em chứa từ ngữ không phù hợp!")
            return
        }

        viewModelScope.launch {
            try {
                val newComment = Comment(topic_id = topicId, username = username, content = content)
                SupabaseClient.client.from("comments").insert(newComment)
                fetchComments(topicId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteComment(commentId: String, topicId: Long) {
        viewModelScope.launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
                val now = sdf.format(Date())
                // Soft-delete: cập nhật cột deleted_at
                SupabaseClient.client.from("comments").update({
                    set("deleted_at", now)
                }) { filter { eq("comment_id", commentId) } }
                fetchComments(topicId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleLockTopic(topicId: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.from("topics").update({
                    set("is_locked", !currentStatus)
                }) { filter { eq("topic_id", topicId) } }
                fetchTopics()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

// --- UI COMPONENTS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    user: User?,
    onBack: () -> Unit
) {
    val viewModel: CommunityViewModel = viewModel()
    val topics by viewModel.topics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var selectedTopic by remember { mutableStateOf<Topic?>(null) }
    var showCreateTopic by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cộng đồng FIDS", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = if (selectedTopic != null) { { selectedTopic = null } } else onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    // Nút tạo bài viết chỉ hiện cho giáo viên
                    if (user?.role == "teacher" && selectedTopic == null) {
                        IconButton(onClick = { showCreateTopic = true }) {
                            Icon(Icons.Default.AddCircle, null, tint = Color(0xFF009688))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FF))) {
            if (selectedTopic == null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(topics) { topic ->
                        TopicItemCard(topic, onClick = { 
                            selectedTopic = topic
                            viewModel.fetchComments(topic.topic_id!!)
                        })
                    }
                }
            } else {
                TopicDetailView(
                    topic = selectedTopic!!,
                    user = user,
                    viewModel = viewModel,
                    onUpdateTopic = { selectedTopic = it }
                )
            }

            if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (showCreateTopic) {
            CreateTopicDialog(
                onDismiss = { showCreateTopic = false },
                onConfirm = { title, content ->
                    viewModel.createTopic(user?.username ?: "", title, content)
                    showCreateTopic = false
                }
            )
        }
    }
}

@Composable
fun TopicItemCard(topic: Topic, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF009688)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.School, null, modifier = Modifier.size(18.dp), tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Hiển thị tên thật (name) của giáo viên
                Text(text = topic.users?.name ?: "Giáo viên", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (topic.is_locked) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = topic.title ?: "", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF263238))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = topic.content ?: "", maxLines = 2, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)
        }
    }
}

@Composable
fun TopicDetailView(
    topic: Topic,
    user: User?,
    viewModel: CommunityViewModel,
    onUpdateTopic: (Topic) -> Unit
) {
    val comments by viewModel.comments.collectAsState()
    var commentText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { p ->
        Column(modifier = Modifier.fillMaxSize().padding(p)) {
            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = topic.title ?: "", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = topic.content ?: "", fontSize = 16.sp, lineHeight = 24.sp)
                    
                    if (user?.role == "teacher") {
                        Button(
                            onClick = { 
                                val newStatus = !topic.is_locked
                                viewModel.toggleLockTopic(topic.topic_id!!, topic.is_locked)
                                onUpdateTopic(topic.copy(is_locked = newStatus))
                            },
                            modifier = Modifier.padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (topic.is_locked) Color(0xFF4CAF50) else Color(0xFFE53935))
                        ) {
                            Icon(if (topic.is_locked) Icons.Default.LockOpen else Icons.Default.Lock, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (topic.is_locked) "Mở khóa bình luận" else "Khóa bình luận")
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))
                    Text(text = "Bình luận (${comments.size})", fontWeight = FontWeight.Bold, color = Color(0xFF009688))
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(comments) { comment ->
                    CommentBubble(
                        comment = comment,
                        currentUser = user,
                        onDelete = { viewModel.deleteComment(comment.comment_id!!, topic.topic_id!!) }
                    )
                }
            }

            if (!topic.is_locked) {
                Surface(shadowElevation = 16.dp, color = Color.White) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth().navigationBarsPadding().imePadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Viết bình luận của em...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.postComment(topic.topic_id!!, user?.username ?: "", commentText) { errorMsg ->
                                    scope.launch { snackbarHostState.showSnackbar(errorMsg) }
                                }
                                commentText = ""
                            }
                        }) {
                            Icon(Icons.Default.Send, null, tint = Color(0xFF009688), modifier = Modifier.size(28.dp))
                        }
                    }
                }
            } else {
                Surface(color = Color(0xFFF5F5F5), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Bài viết đã được giáo viên khóa bình luận.",
                        modifier = Modifier.padding(20.dp),
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun CommentBubble(comment: Comment, currentUser: User?, onDelete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Hiện tên thật (name) từ join query dựa trên username
            Text(
                text = comment.users?.name ?: "Người dùng", 
                fontWeight = FontWeight.Bold, 
                fontSize = 14.sp,
                color = Color(0xFF3F51B5)
            )
            Spacer(modifier = Modifier.weight(1f))
            // Phân quyền xóa: Giáo viên xóa mọi bài, học sinh chỉ xóa bài của mình (check username định danh)
            if (currentUser?.role == "teacher" || currentUser?.username == comment.username) {
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, tint = Color.LightGray)
                }
            }
        }
        Surface(
            color = Color(0xFFF1F3F4),
            shape = RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(text = comment.content ?: "", modifier = Modifier.padding(12.dp), fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
fun CreateTopicDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tạo bài viết mới", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title, 
                    onValueChange = { title = it }, 
                    label = { Text("Tiêu đề") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = content, 
                    onValueChange = { content = it }, 
                    label = { Text("Nội dung chia sẻ") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if(title.isNotBlank()) onConfirm(title, content) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
            ) { Text("Đăng bài") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
