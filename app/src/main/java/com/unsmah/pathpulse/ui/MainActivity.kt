package com.unsmah.pathpulse.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.vector.ImageVector
import com.unsmah.pathpulse.R
import com.unsmah.pathpulse.location.TrackingService
import com.unsmah.pathpulse.ui.navigation.NavGraph

class MainActivity : ComponentActivity() {

    var viewModel: MainViewModel? = null
        private set

    private var serviceBound = false
    private var trackingService: TrackingService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
            trackingService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = MainViewModel(application)

        setContent {
            PathPulseTheme {
                MainApp(
                    viewModel = viewModel!!,
                    onStartTracking = { trackId ->
                        viewModel?.startTracking(this@MainActivity, trackId)
                    },
                    onStopTracking = {
                        viewModel?.stopTracking(this@MainActivity)
                    },
                    onPauseTracking = {
                        viewModel?.pauseTracking(this@MainActivity)
                    },
                    onResumeTracking = {
                        viewModel?.resumeTracking(this@MainActivity)
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, TrackingService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }
}

data class BottomNavItem(
    val route: String,
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = "track",
        titleRes = R.string.tab_track,
        selectedIcon = Icons.Filled.EmojiEvents,
        unselectedIcon = Icons.Outlined.EmojiEvents
    ),
    BottomNavItem(
        route = "history",
        titleRes = R.string.tab_history,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    ),
    BottomNavItem(
        route = "map",
        titleRes = R.string.tab_map,
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map
    ),
    BottomNavItem(
        route = "reports",
        titleRes = R.string.tab_reports,
        selectedIcon = Icons.Filled.Report,
        unselectedIcon = Icons.Outlined.Report
    ),
    BottomNavItem(
        route = "settings",
        titleRes = R.string.tab_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)

@Composable
fun MainApp(
    viewModel: MainViewModel,
    onStartTracking: (Long) -> Unit,
    onStopTracking: () -> Unit,
    onPauseTracking: () -> Unit,
    onResumeTracking: () -> Unit
) {
    val navController = rememberNavController()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTabIndex) {
        navController.navigate(bottomNavItems[selectedTabIndex].route)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selectedTabIndex == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(item.titleRes)) },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            viewModel = viewModel,
            onStartTracking = onStartTracking,
            onStopTracking = onStopTracking,
            onPauseTracking = onPauseTracking,
            onResumeTracking = onResumeTracking,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
