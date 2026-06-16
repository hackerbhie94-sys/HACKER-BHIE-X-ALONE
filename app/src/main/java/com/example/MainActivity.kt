package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.ChatEngine
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

enum class Screen {
    SPLASH,
    LOGIN,
    HOME,
    CHAT,
    ADMIN
}

@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by ChatEngine.isDarkMode.collectAsState()
            val currentUser by ChatEngine.currentUser.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
                var activeRoomId by remember { mutableStateOf<String?>(null) }

                BackHandler {
                    when (currentScreen) {
                        Screen.CHAT -> {
                            activeRoomId = null
                            currentScreen = Screen.HOME
                        }
                        Screen.ADMIN -> {
                            currentScreen = Screen.HOME
                        }
                        Screen.HOME -> {
                            finish()
                        }
                        else -> {
                            finish()
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() with fadeOut()
                            },
                            label = "screenRouterAnim"
                        ) { screen ->
                            when (screen) {
                                Screen.SPLASH -> {
                                    SplashView(
                                        onSplashFinished = {
                                            currentScreen = if (currentUser != null) Screen.HOME else Screen.LOGIN
                                        }
                                    )
                                }
                                Screen.LOGIN -> {
                                    LoginView(
                                        onLoginSuccess = {
                                            currentScreen = Screen.HOME
                                        }
                                    )
                                }
                                Screen.HOME -> {
                                    MainDashboardView(
                                        onRoomSelected = { roomId ->
                                            activeRoomId = roomId
                                            currentScreen = Screen.CHAT
                                        },
                                        onAdminTerminalTriggered = {
                                            currentScreen = Screen.ADMIN
                                        },
                                        onLogout = {
                                            ChatEngine.logout()
                                            currentScreen = Screen.LOGIN
                                        }
                                    )
                                }
                                Screen.CHAT -> {
                                    val roomId = activeRoomId
                                    if (roomId != null) {
                                        ActiveChatView(
                                            roomId = roomId,
                                            onBack = {
                                                activeRoomId = null
                                                currentScreen = Screen.HOME
                                            }
                                        )
                                    } else {
                                        currentScreen = Screen.HOME
                                    }
                                }
                                Screen.ADMIN -> {
                                    AdminDashboardView(
                                        onBack = {
                                            currentScreen = Screen.HOME
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
