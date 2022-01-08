package com.joeloewi.mediastoreimagegallery.ui.mediastoreimagegallery

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.joeloewi.mediastoreimagegallery.ui.theme.MediaStoreImageGalleryTheme

@Composable
fun GriddedMediaStoreImagesScreen(
    navController: NavController
) {

    GriddedMediaStoreImageContent(
        onImageClick = {
            navController.navigate("pagedMediaStoreImagesScreen")
        },
        onAddImageClick = {
            navController.navigate("cameraScreen")
        }
    )
}

@Composable
fun GriddedMediaStoreImageContent(
    onImageClick: () -> Unit,
    onAddImageClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddImageClick) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = Icons.Default.AddAPhoto.name
                )
            }
        }
    ) {
        Column {
            Text("GriddedMediaStoreImagesScreen")
            Button(onClick = onImageClick) {
                Text(text = "pagedMediaStoreImagesScreen")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GriddedMediaStoreImagesScreenPreview() {
    MediaStoreImageGalleryTheme {
        GriddedMediaStoreImageContent(
            onImageClick = {},
            onAddImageClick = {}
        )
    }
}