package com.stayfocus.reminder.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.stayfocus.reminder.ui.theme.FocusPrimary
import com.stayfocus.reminder.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val todos by viewModel.todos.collectAsState()
    val pendingCount = todos.count { !it.isCompleted }

    val dateFormat = remember { SimpleDateFormat("M月d日 EEEE", Locale.CHINESE) }
    val currentDateStr = remember { dateFormat.format(Date()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedTab == 0) "驻留待办" else "强提醒配置",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        if (selectedTab == 0) {
                            Text(
                                text = currentDateStr,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        if (pendingCount > 0) {
                            BadgedBox(badge = { Badge { Text("$pendingCount") } }) {
                                Icon(Icons.Default.Checklist, contentDescription = "今日待办")
                            }
                        } else {
                            Icon(Icons.Default.Checklist, contentDescription = "今日待办")
                        }
                    },
                    label = { Text("待办清单") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FocusPrimary,
                        selectedTextColor = FocusPrimary,
                        indicatorColor = FocusPrimary.copy(alpha = 0.12f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
                    label = { Text("强提醒设置") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FocusPrimary,
                        selectedTextColor = FocusPrimary,
                        indicatorColor = FocusPrimary.copy(alpha = 0.12f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> TodoListScreen(viewModel = viewModel)
                1 -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
