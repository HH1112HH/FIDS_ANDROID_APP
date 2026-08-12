package com.example.fidsapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- ViewModel cho Profile ---
class ProfileViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers = _allUsers.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun updatePassword(username: String, newPass: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                SupabaseClient.client.from("users").update({
                    set("matkhau", newPass)
                }) { filter { eq("username", username) } }
                _message.value = "Đổi mật khẩu thành công!"
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchAllUsers(currentUsername: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val users = SupabaseClient.client.from("users")
                    .select(columns = Columns.raw("*, surveys(*), diaries(*)"))
                    .decodeList<User>()
                // Lấy tất cả trừ người đang đăng nhập
                _allUsers.value = users.filter { it.username != currentUsername }
            } catch (e: Exception) {
                _message.value = "Lỗi tải dữ liệu: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() { _message.value = null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val viewModel: ProfileViewModel = viewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val scrollState = rememberScrollState()
    
    var showPersonalInfo by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showTeacherFeature by remember { mutableStateOf(false) }
    
    // State cho đổi mật khẩu
    var newPassword by remember { mutableStateOf("") }

    // State cho tính năng giáo viên
    val allUsers by viewModel.allUsers.collectAsState()
    var selectedClass by remember { mutableStateOf<String?>(null) }
    var selectedStudent by remember { mutableStateOf<User?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = bottomBar
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FF))
                .verticalScroll(scrollState)
        ) {
            // Header Profile (Chỉ hiện tên theo yêu cầu)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF3F51B5), RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = user?.name ?: "Người dùng",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Các mục chức năng
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                ProfileMenuItem(
                    title = "Thông tin cá nhân",
                    icon = Icons.Default.Info,
                    onClick = { showPersonalInfo = true }
                )
                ProfileMenuItem(
                    title = "Đổi mật khẩu",
                    icon = Icons.Default.Lock,
                    onClick = { showChangePassword = true }
                )
                
                // Chỉ hiện và cho phép bấm nếu là Teacher
                if (user?.role == "teacher") {
                    ProfileMenuItem(
                        title = "Quản lý lớp học (Giáo viên)",
                        icon = Icons.Default.School,
                        onClick = { 
                            viewModel.fetchAllUsers(user.username ?: "")
                            showTeacherFeature = true 
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Nút Đăng xuất
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đăng xuất", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // --- Dialogs ---

        // 1. Thông tin cá nhân (Tên, Lớp, SĐT kèm +84)
        if (showPersonalInfo) {
            AlertDialog(
                onDismissRequest = { showPersonalInfo = false },
                title = { Text("Thông tin cá nhân", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        InfoRow(label = "Tên", value = user?.name ?: "N/A")
                        InfoRow(label = "Lớp", value = user?.clazz ?: "N/A")
                        val formattedPhone = user?.phone?.let { 
                            if (it.startsWith("0")) "+84${it.substring(1)}" else "+84$it"
                        } ?: "N/A"
                        InfoRow(label = "Số điện thoại", value = formattedPhone)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPersonalInfo = false }) { Text("Đóng") }
                }
            )
        }

        // 2. Đổi mật khẩu
        if (showChangePassword) {
            AlertDialog(
                onDismissRequest = { showChangePassword = false },
                title = { Text("Đổi mật khẩu", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Mật khẩu mới") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPassword.isNotBlank()) {
                                viewModel.updatePassword(user?.username ?: "", newPassword)
                                showChangePassword = false
                                newPassword = ""
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        else Text("Cập nhật")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChangePassword = false }) { Text("Hủy") }
                }
            )
        }

        // 3. Tính năng giáo viên
        if (showTeacherFeature) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showTeacherFeature = false
                    selectedClass = null
                    selectedStudent = null
                },
                modifier = Modifier.fillMaxHeight(0.9f)
            ) {
                TeacherFeatureContent(
                    allUsers = allUsers,
                    selectedClass = selectedClass,
                    onClassSelect = { selectedClass = it },
                    selectedStudent = selectedStudent,
                    onStudentSelect = { selectedStudent = it },
                    onBack = {
                        if (selectedStudent != null) selectedStudent = null
                        else if (selectedClass != null) selectedClass = null
                        else showTeacherFeature = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE8EAF6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color(0xFF3F51B5))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TeacherFeatureContent(
    allUsers: List<User>,
    selectedClass: String?,
    onClassSelect: (String) -> Unit,
    selectedStudent: User?,
    onStudentSelect: (User) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Text(
                text = when {
                    selectedStudent != null -> "Chi tiết: ${selectedStudent.name}"
                    selectedClass != null -> "Lớp: $selectedClass"
                    else -> "Danh sách lớp"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            selectedStudent != null -> {
                // Hiển thị Lịch sử Khảo sát và Nhật ký của học sinh được chọn
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { Text("Lịch sử khảo sát", fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5)) }
                    if (selectedStudent.surveys.isEmpty()) {
                        item { Text("Chưa có khảo sát nào.", color = Color.Gray) }
                    }
                    items(selectedStudent.surveys) { survey ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(survey.content ?: "Khảo sát", fontWeight = FontWeight.Bold)
                                Text("Kết quả: ${survey.result}", fontSize = 14.sp)
                                Text("Điểm: ${survey.score}", fontSize = 14.sp)
                                Text("Ngày: ${survey.created_at}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { Text("Nhật ký rèn luyện", fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5)) }
                    if (selectedStudent.diaries.isEmpty()) {
                        item { Text("Chưa có nhật ký nào.", color = Color.Gray) }
                    }
                    items(selectedStudent.diaries) { diary ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Ngày ${diary.Day}: ${diary.content}", fontWeight = FontWeight.Bold)
                                Text(diary.descript ?: "", fontSize = 14.sp)
                                Text("Lúc: ${diary.created_at}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
            selectedClass != null -> {
                // Hiển thị danh sách tên các bạn trong lớp đã chọn
                val studentsInClass = allUsers.filter { it.clazz == selectedClass }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(studentsInClass) { student ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onStudentSelect(student) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = Color.Gray)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(student.name ?: student.username ?: "N/A", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            else -> {
                // Hiển thị danh sách các lớp sau khi truy xuất dữ liệu
                val classes = allUsers.mapNotNull { it.clazz }.distinct().sorted()
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(classes) { clazz ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onClassSelect(clazz) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Class, null, tint = Color(0xFF3F51B5))
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Lớp $clazz", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
