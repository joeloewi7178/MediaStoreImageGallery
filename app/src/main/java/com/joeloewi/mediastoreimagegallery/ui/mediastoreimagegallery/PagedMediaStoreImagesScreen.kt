package com.joeloewi.mediastoreimagegallery.ui.mediastoreimagegallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.joeloewi.mediastoreimagegallery.ui.theme.MediaStoreImageGalleryTheme

@Composable
fun PagedMediaStoreImagesScreen(
    index: Int
) {

    PagedMediaStoreImagesContent(
        index = index
    )
}

@Composable
fun PagedMediaStoreImagesContent(
    index: Int
) {
    Scaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("$index")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PagedMediaStoreImagesScreenPreview() {
    MediaStoreImageGalleryTheme {
        PagedMediaStoreImagesContent(
            index = 0
        )
    }
}