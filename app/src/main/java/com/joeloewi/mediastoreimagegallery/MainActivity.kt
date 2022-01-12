package com.joeloewi.mediastoreimagegallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.joeloewi.mediastoreimagegallery.ui.mediastoreimagegallery.CameraScreen
import com.joeloewi.mediastoreimagegallery.ui.mediastoreimagegallery.GriddedMediaStoreImagesScreen
import com.joeloewi.mediastoreimagegallery.ui.mediastoreimagegallery.PagedMediaStoreImagesScreen
import com.joeloewi.mediastoreimagegallery.ui.theme.MediaStoreImageGalleryTheme
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.snapper.ExperimentalSnapperApi

@ExperimentalSnapperApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalPermissionsApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediaStoreImageGalleryTheme {
                MediaStoreImageGalleryApp()
            }
        }
    }
}

@ExperimentalPagerApi
@ExperimentalSnapperApi
@ExperimentalFoundationApi
@ExperimentalPermissionsApi
@Composable
fun MediaStoreImageGalleryApp() {
    val navController = rememberNavController()

    Scaffold { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "mediaStoreImage"
            ) {
                navigation(
                    startDestination = "griddedMediaStoreImagesScreen",
                    route = "mediaStoreImage"
                ) {
                    composable("griddedMediaStoreImagesScreen") {
                        val parentEntry = remember { navController.getBackStackEntry("mediaStoreImage") }

                        GriddedMediaStoreImagesScreen(
                            navController = navController,
                            mediaStoreImagesViewModel = hiltViewModel(parentEntry)
                        )
                    }

                    composable(
                        route = "pagedMediaStoreImagesScreen/{index}",
                        arguments = listOf(
                            navArgument("index") {
                                type = NavType.IntType
                            }
                        )
                    ) { navBackStackEntry ->
                        val index = navBackStackEntry.arguments?.getInt("index") ?: 0
                        val parentEntry = remember { navController.getBackStackEntry("mediaStoreImage") }

                        PagedMediaStoreImagesScreen(
                            index = index,
                            mediaStoreImagesViewModel = hiltViewModel(parentEntry)
                        )
                    }

                    composable("cameraScreen") {
                        CameraScreen()
                    }
                }
            }
        }
    }
}

@ExperimentalSnapperApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalPermissionsApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MediaStoreImageGalleryTheme {
        MediaStoreImageGalleryApp()
    }
}