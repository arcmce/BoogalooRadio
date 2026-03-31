package com.arcmce.boogaloo.ui.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.DrawableRes
import com.arcmce.boogaloo.BuildConfig
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.collectAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.arcmce.boogaloo.ui.viewmodel.CloudcastViewModelFactory
import com.arcmce.boogaloo.ui.viewmodel.LiveViewModel
import com.arcmce.boogaloo.ui.viewmodel.LiveViewModelFactory
import com.arcmce.boogaloo.data.repository.FavoritesRepository
import com.arcmce.boogaloo.ui.viewmodel.FavoritesViewModel
import com.arcmce.boogaloo.ui.viewmodel.FavoritesViewModelFactory
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModelFactory



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val repository = Repository()

        val sharedViewModel: SharedViewModel by viewModels { SharedViewModelFactory(application) }
        val liveViewModel: LiveViewModel by viewModels { LiveViewModelFactory(repository, application) }
        val catchUpViewModel: CatchUpViewModel by viewModels { CatchUpViewModelFactory(repository) }
        val cloudcastViewModelFactory = CloudcastViewModelFactory(repository)
        val favoritesRepository = FavoritesRepository(application)
        val favoritesViewModel: FavoritesViewModel by viewModels { FavoritesViewModelFactory(application, favoritesRepository) }

        setContent {
            BoogalooJetpackTheme {
                val topBarColor = MaterialTheme.colorScheme.surface
                val view = LocalView.current
                val lightIcons = topBarColor.luminance() > 0.5f

                SideEffect {
                    // Control status/navigation bar icon contrast
                    val controller = WindowInsetsControllerCompat(window, view)
                    controller.isAppearanceLightStatusBars = lightIcons
                    controller.isAppearanceLightNavigationBars = topBarColor.luminance() > 0.5f
                }

                AppContent(
                    liveViewModel = liveViewModel,
                    catchUpViewModel = catchUpViewModel,
                    sharedViewModel = sharedViewModel,
                    favoritesViewModel = favoritesViewModel,
                    cloudcastViewModelFactory = cloudcastViewModelFactory,
                    context = this)
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
    liveViewModel: LiveViewModel,
    catchUpViewModel: CatchUpViewModel,
    sharedViewModel: SharedViewModel,
    favoritesViewModel: FavoritesViewModel,
    cloudcastViewModelFactory: CloudcastViewModelFactory,
    context: Context,
) {
    val isDarkTheme = isSystemInDarkTheme()

    val navController = rememberNavController()

    LaunchedEffect(isDarkTheme) {
        if (BuildConfig.DEBUG) Log.d("MainActivity", "theme change detected")
        sharedViewModel.setIsDarkTheme(isDarkTheme)
    }

    var showSocialsSheet by rememberSaveable { mutableStateOf(false) }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val selectedTab = when {
        currentRoute?.startsWith("pastShow") == true -> "CatchUp"
        currentRoute == "Favorites" -> "Favorites"
        currentRoute == "CatchUp" -> "CatchUp"
        else -> "Live"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = TopAppBarDefaults.windowInsets,
                    navigationIcon = {
                        if (currentRoute?.startsWith("pastShow") == true) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    title = {
                        Image(
                            painter = painterResource(if (isDarkTheme) R.drawable.logo_long_white else R.drawable.logo_long_black),
                            contentDescription = "Boogaloo Radio",
                            modifier = Modifier
                                .height(32.dp),
                            contentScale = ContentScale.Fit
                        )
                    },
                    actions = {
                        IconButton(onClick = { showSocialsSheet = true }) {
                            Icon(Icons.Outlined.Info, contentDescription = "About")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == "Live",
                        onClick = {
                            navController.navigate("Live") {
                                popUpTo("Live") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                if (selectedTab == "Live") Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == "Favorites",
                        onClick = {
                            navController.navigate("Favorites") {
                                popUpTo("Live") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                if (selectedTab == "Favorites") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorites"
                            )
                        },
                        label = { Text("Favorites") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == "CatchUp",
                        onClick = {
                            if (selectedTab == "CatchUp") {
                                sharedViewModel.triggerMixesTabTapped()
                            } else {
                                navController.navigate("CatchUp") {
                                    popUpTo("Live") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                if (selectedTab == "CatchUp") Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                                contentDescription = "Mixes"
                            )
                        },
                        label = { Text("Mixes") }
                    )
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Navigation host to switch between LiveView and CatchUpView
                NavHost(
                    navController = navController,
                    startDestination = "Live",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("Live") { LiveView(liveViewModel, sharedViewModel, favoritesViewModel) }
                    composable("Favorites") { FavoritesView(favoritesViewModel, sharedViewModel, navController) }
                    composable("CatchUp") { CatchUpView(catchUpViewModel, navController, sharedViewModel, favoritesViewModel) }

                    composable("pastShow/{slug}") { backStackEntry ->
                        val slug = backStackEntry.arguments?.getString("slug") ?: return@composable
                        val cloudcastViewModel: CloudcastViewModel = viewModel(backStackEntry, factory = cloudcastViewModelFactory)
                        CloudcastView(cloudcastViewModel, slug, sharedViewModel, favoritesViewModel, onNavigateBack = { navController.popBackStack() })
                    }
                }

                val isScheduleOpen by sharedViewModel.isScheduleOpen.collectAsState()
                AnimatedVisibility(
                    visible = currentRoute != "Live" || isScheduleOpen,
                    enter = fadeIn(animationSpec = tween(800)),
                    exit = fadeOut(animationSpec = tween(800)),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.66f to Color.Black.copy(alpha = 0.65f),
                                    1f to Color.Black.copy(alpha = 0.65f)
                                )
                            )
                    )
                }

                PlaybackControls(
                    context,
                    sharedViewModel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clickable {  }
                )
            }
        }

        if (showSocialsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSocialsSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                SocialsSheetContent(context)
            }
        }
    }
}








@Composable
fun SocialsSheetContent(context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            "Follow us",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        SocialLink(context, R.drawable.ic_instagram, "Instagram", "https://instagram.com/boogalooradio")
        SocialLink(context, R.drawable.ic_facebook, "Facebook", "https://facebook.com/boogalooradio")
        SocialLink(context, R.drawable.ic_x, "X", "https://x.com/boogaloo_radio")
        SocialLink(context, R.drawable.ic_web, "Website", "https://boogalooradio.com")
    }
}

@Composable
fun SocialLink(context: Context, @DrawableRes iconRes: Int, label: String, url: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    YourAppTheme {
//        TestPlaybackServiceComposable(context = LocalContext.current)
//    }
//}
