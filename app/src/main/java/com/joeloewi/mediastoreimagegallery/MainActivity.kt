package com.joeloewi.mediastoreimagegallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.joeloewi.mediastoreimagegallery.ui.mediastoreimagegallery.CameraScreen
import com.joeloewi.mediastoreimagegallery.ui.mediastoreimagegallery.GriddedMediaStoreImagesScreen
import com.joeloewi.mediastoreimagegallery.ui.mediastoreimagegallery.PagedMediaStoreImagesScreen
import com.joeloewi.mediastoreimagegallery.ui.theme.MediaStoreImageGalleryTheme
import dagger.hilt.android.AndroidEntryPoint

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
                        GriddedMediaStoreImagesScreen(
                            navController = navController,
                            mediaStoreImagesViewModel = hiltViewModel()
                        )
                    }

                    composable("pagedMediaStoreImagesScreen") {
                        PagedMediaStoreImagesScreen()
                    }

                    composable("cameraScreen") {
                        CameraScreen()
                    }
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalPermissionsApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MediaStoreImageGalleryTheme {
        MediaStoreImageGalleryApp()
    }
}