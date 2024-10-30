package com.arcmce.boogaloo.ui.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.network.repository.Repository
import com.arcmce.boogaloo.ui.theme.BoogalooJetpackTheme
import com.arcmce.boogaloo.ui.viewmodel.CatchUpViewModel
import com.arcmce.boogaloo.ui.viewmodel.CatchUpViewModelFactory
import com.arcmce.boogaloo.ui.viewmodel.CloudcastViewModel
import com.arcmce.boogaloo.ui.viewmodel.ColorViewModel
import com.arcmce.boogaloo.ui.viewmodel.ColorViewModelFactory
import com.arcmce.boogaloo.ui.viewmodel.ScheduleViewModel
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


data class TabBarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeAmount: Int? = null
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = Repository()

        val sharedViewModel: SharedViewModel by viewModels { SharedViewModelFactory(repository, application) }
//        val liveViewModel: LiveViewModel by viewModels { LiveViewModelFactory(repository, application) }
        val catchUpViewModel: CatchUpViewModel by viewModels { CatchUpViewModelFactory(repository) }
        val cloudcastViewModel: CloudcastViewModel by viewModels()
        val colorViewModel: ColorViewModel by viewModels { ColorViewModelFactory(repository, application) }
        val scheduleViewModel: ScheduleViewModel by viewModels()

        setContent {
            BoogalooJetpackTheme {
                AppContent(
//                    liveViewModel = liveViewModel,
                    scheduleViewModel = scheduleViewModel,
                    catchUpViewModel = catchUpViewModel,
                    cloudcastViewModel = cloudcastViewModel,
                    colorViewModel = colorViewModel,
                    sharedViewModel = sharedViewModel,
                    context = this,
                    window = window)
            }
        }

        startBackgroundCoroutine(sharedViewModel)
    }

    private fun startBackgroundCoroutine(sharedViewModel: SharedViewModel) {

        val scope = CoroutineScope(Dispatchers.Default)

        scope.launch {
            while (true) {
                sharedViewModel.fetchRadioSchedule()

                delay(5 * 60 * 1000)
            }
        }

        scope.launch {
            while (true) {
                sharedViewModel.getOnAirItem()

                delay(10 * 1000)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
//    liveViewModel: LiveViewModel,
    scheduleViewModel: ScheduleViewModel,
    catchUpViewModel: CatchUpViewModel,
    cloudcastViewModel: CloudcastViewModel,
    colorViewModel: ColorViewModel,
    sharedViewModel: SharedViewModel,
    context: Context,
    window: Window,
) {
    val isDarkTheme = isSystemInDarkTheme()

    val liveTab = TabBarItem(title = "Live", selectedIcon = Icons.Filled.Home, unselectedIcon = Icons.Outlined.Home)
    val catchUpTab = TabBarItem(title = "CatchUp", selectedIcon = Icons.Filled.Notifications, unselectedIcon = Icons.Outlined.Notifications)
    val scheduleTab = TabBarItem(title = "Schedule", selectedIcon = Icons.Filled.Warning, unselectedIcon = Icons.Outlined.Warning)
    val colorTab = TabBarItem(title = "Color", selectedIcon = Icons.Filled.Add, unselectedIcon = Icons.Outlined.Add)


    // creating a list of all the tabs
    val tabBarItems = listOf(liveTab, scheduleTab, catchUpTab, colorTab)

    val navController = rememberNavController()

    val artworkColorSwatch by sharedViewModel.artworkColorSwatch.collectAsState()

    window.statusBarColor = artworkColorSwatch?.rgb ?: Color.Gray.toArgb()

    LaunchedEffect(isDarkTheme) {
        Log.d("MainActivity", "theme change detected")
        sharedViewModel.setIsDarkTheme(isDarkTheme)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                Icon(
                    painter = painterResource(id = R.drawable.logo_long_white),
                    contentDescription = "Boogaloo logo long black",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.Start)
                        .padding(16.dp)
                        .height(32.dp)
                    , // Size can be adjusted as needed
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            bottomBar = {
                TabView(tabBarItems, navController)
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Navigation host to switch between LiveView and CatchUpView
                NavHost(
                    navController = navController,
                    startDestination = liveTab.title,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(liveTab.title) { LiveView(sharedViewModel )}
                    composable(scheduleTab.title) { ScheduleView(scheduleViewModel, sharedViewModel) }
                    composable(catchUpTab.title) { CatchUpView(catchUpViewModel, sharedViewModel, navController) }
                    composable(colorTab.title) { ColorView(colorViewModel, sharedViewModel) }

                    composable("pastShow/{slug}") { CloudcastView(cloudcastViewModel, sharedViewModel) }
                }

                // TODO become visible when service state is playing
                PlaybackControls(
                    context,
                    sharedViewModel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
//                        .padding(bottom = 32.dp)
                        .clickable {  }
                )
            }
        }
    }
}

// ----------------------------------------
// This is a wrapper view that allows us to easily and cleanly
// reuse this component in any future project
@Composable
fun TabView(tabBarItems: List<TabBarItem>, navController: NavController) {
    var selectedTabIndex by rememberSaveable {
        mutableStateOf(0)
    }

    // Observe the current back stack entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Update selectedTabIndex based on the current destination
    LaunchedEffect(navBackStackEntry) {
        val currentDestination = navBackStackEntry?.destination?.route
        tabBarItems.forEachIndexed { index, tabBarItem ->
            if (tabBarItem.title == currentDestination) {
                selectedTabIndex = index
            }
        }
    }


    NavigationBar {
        // looping over each tab to generate the views and navigation for each item
        tabBarItems.forEachIndexed { index, tabBarItem ->
            NavigationBarItem(
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = index
                    navController.navigate(tabBarItem.title) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    TabBarIconView(
                        isSelected = selectedTabIndex == index,
                        selectedIcon = tabBarItem.selectedIcon,
                        unselectedIcon = tabBarItem.unselectedIcon,
                        title = tabBarItem.title,
                        badgeAmount = tabBarItem.badgeAmount
                    )
                },
                label = {Text(tabBarItem.title)})
        }
    }
}

// This component helps to clean up the API call from our TabView above,
// but could just as easily be added inside the TabView without creating this custom component
@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String,
    badgeAmount: Int? = null
) {
    BadgedBox(badge = { TabBarBadgeView(badgeAmount) }) {
        Icon(
            imageVector = if (isSelected) {selectedIcon} else {unselectedIcon},
            contentDescription = title
        )
    }
}

// This component helps to clean up the API call from our TabBarIconView above,
// but could just as easily be added inside the TabBarIconView without creating this custom component
@Composable
fun TabBarBadgeView(count: Int? = null) {
    if (count != null) {
        Badge {
            Text(count.toString())
        }
    }
}
// end of the reusable components that can be copied over to any new projects
// ----------------------------------------







//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    YourAppTheme {
//        TestPlaybackServiceComposable(context = LocalContext.current)
//    }
//}
