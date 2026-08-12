package com.example.fidsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.fidsapp.ui.theme.FIDSAPPTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FIDSAPPTheme {
                val historyViewModel: HistoryViewModel = viewModel()
                val chatViewModel: ChatViewModel = viewModel()
                AppNavigation(historyViewModel, chatViewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(historyViewModel: HistoryViewModel, chatViewModel: ChatViewModel) {
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf<User?>(null) }
    var currentResult by remember { mutableStateOf<SurveyRecord?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLoginSuccess = { user ->
                if (currentUser == null) {
                    currentUser = user
                    historyViewModel.setUserData(user)
                }
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeScreen(
                user = currentUser,
                viewModel = historyViewModel,
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToSurvey = { type -> navController.navigate("survey/$type") },
                onNavigateToWiki = { navController.navigate("wiki") },
                onNavigateToSkill = { skillId -> navController.navigate("skill_detail/$skillId") },
                onNavigateToSOS = { navController.navigate("sos") },
                onNavigateToBreathing = { navController.navigate("breathing") },
                onNavigateToRoadmap = { navController.navigate("roadmap") },
                onNavigateToCommunity = { navController.navigate("community") },
                bottomBar = { 
                    StaticBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route -> 
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) 
                }
            )
        }
        composable("sos") {
            SOSScreen(
                onBack = { navController.popBackStack() },
                onNavigateToChat = { navController.navigate("chat") },
                onNavigateToBreathing = { navController.navigate("breathing") }
            )
        }
        composable("chat") {
            ChatScreen(
                viewModel = chatViewModel,
                bottomBar = { 
                    StaticBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route -> 
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) 
                }
            )
        }
        composable("profile") {
            ProfileScreen(
                user = currentUser,
                onLogout = {
                    currentUser = null
                    CoroutineScope(Dispatchers.IO).launch {
                        SupabaseClient.logout()
                    }
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                bottomBar = { 
                    StaticBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route -> 
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) 
                }
            )
        }
        composable("roadmap") {
            RoadmapScreen(
                viewModel = historyViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToProject = { projectId ->
                    navController.navigate("project_detail/$projectId")
                }
            )
        }
        composable("project_detail/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: "Project 1"
            val (projectName, tasks) = when (projectId) {
                "Project 1" -> "Dự án 1: Cải thiện Lớp học" to historyViewModel.project1Tasks
                "Project 2" -> "Dự án 2: Kết nối Gia đình" to historyViewModel.project2Tasks
                "Project 3" -> "Dự án 3: Cai nghiện Điện thoại" to historyViewModel.project3Tasks
                "Project 4" -> "Dự án 4: An toàn & Cộng đồng" to historyViewModel.project4Tasks
                else -> "Dự án rèn luyện" to historyViewModel.project1Tasks
            }
            ProjectDetailScreen(
                projectName = projectName,
                tasks = tasks,
                currentUser = currentUser,
                viewModel = historyViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("wiki") {
            WikiFidsScreen(onBack = { navController.popBackStack() })
        }
        composable("skill_detail/{skillId}") { backStackEntry ->
            val skillId = backStackEntry.arguments?.getString("skillId") ?: ""
            SkillDetailScreen(skillId = skillId, onBack = { navController.popBackStack() })
        }
        composable("breathing") {
            BreathingScreen(onBack = { navController.popBackStack() })
        }
        composable("history") {
            HistoryScreen(viewModel = historyViewModel, onBack = { navController.popBackStack() })
        }
        composable("survey/{type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            SurveyScreen(
                type = type,
                questions = historyViewModel.questions,
                onComplete = { answers ->
                    currentResult = if (type == "Khảo sát") {
                        historyViewModel.calculateSurveyResult(answers, currentUser)
                    } else {
                        historyViewModel.calculateTestResult(answers, type, currentUser)
                    }
                    navController.navigate("result")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("result") {
            currentResult?.let { result ->
                ResultScreen(result = result, onHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                })
            }
        }
        composable("community") {
            CommunityScreen(user = currentUser, onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun StaticBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Trang chủ") },
            selected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Chat, null) },
            label = { Text("Tư vấn") },
            selected = currentRoute == "chat",
            onClick = { onNavigate("chat") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Tài khoản") },
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") }
        )
    }
}
