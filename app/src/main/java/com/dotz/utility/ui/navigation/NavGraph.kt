package com.dotz.utility.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dotz.utility.ui.calculator.CalculatorScreen
import com.dotz.utility.ui.calendar.CalendarScreen
import com.dotz.utility.ui.clock.ClockScreen
import com.dotz.utility.ui.notes.NoteEditScreen
import com.dotz.utility.ui.notes.NotesScreen
import com.dotz.utility.ui.settings.SettingsScreen

/** All navigation destinations in the app */
sealed class Route(val path: String, val label: String = "", val icon: ImageVector? = null) {
    object Calculator : Route("calculator", "calculator", Icons.Outlined.Calculate)
    object Calendar   : Route("calendar", "calendar", Icons.Outlined.CalendarMonth)
    object Clock      : Route("clock", "clock", Icons.Outlined.AccessTime)
    object Notes      : Route("notes", "notes", Icons.Outlined.StickyNote2)
    object NoteEdit   : Route("note_edit/{noteId}") {
        fun withId(id: Long) = "note_edit/$id"
        const val ARG = "noteId"
    }
    object Settings   : Route("settings")
}

private val navItems = listOf(
    Route.Calculator,
    Route.Calendar,
    Route.Clock,
    Route.Notes
)

@Composable
fun DotzNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            // Permanent bottom bar with icons
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp,
                modifier = Modifier.height(80.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.path } == true
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    navController.navigate(screen.path) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            screen.icon?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = screen.label,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (selected) 
                                        MaterialTheme.colorScheme.onSurface 
                                    else 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = screen.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                color = if (selected) 
                                    MaterialTheme.colorScheme.onSurface 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.Calculator.path,
            modifier = Modifier.padding(padding)
        ) {
            composable(Route.Calculator.path) {
                CalculatorScreen()
            }
            composable(Route.Calendar.path) {
                CalendarScreen()
            }
            composable(Route.Clock.path) {
                ClockScreen()
            }
            composable(Route.Notes.path) {
                NotesScreen(
                    onEditNote = { id -> navController.navigate(Route.NoteEdit.withId(id)) }
                )
            }
            composable(
                route = Route.NoteEdit.path,
                arguments = listOf(navArgument(Route.NoteEdit.ARG) { type = NavType.LongType })
            ) { backStack ->
                val noteId = backStack.arguments?.getLong(Route.NoteEdit.ARG) ?: -1L
                NoteEditScreen(
                    noteId = noteId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Route.Settings.path) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
