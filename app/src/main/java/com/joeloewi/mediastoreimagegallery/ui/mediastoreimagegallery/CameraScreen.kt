package com.joeloewi.mediastoreimagegallery.ui.mediastoreimagegallery

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.joeloewi.mediastoreimagegallery.ui.theme.MediaStoreImageGalleryTheme

@Composable
fun CameraScreen() {

    CameraContent()
}

@Composable
fun CameraContent() {
    Scaffold {
        Text("CameraScreen")
    }
}

@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    MediaStoreImageGalleryTheme {
        CameraContent()
    }
}