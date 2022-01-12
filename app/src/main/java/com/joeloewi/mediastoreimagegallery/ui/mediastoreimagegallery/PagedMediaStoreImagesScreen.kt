package com.joeloewi.mediastoreimagegallery.ui.mediastoreimagegallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerDefaults
import com.google.accompanist.pager.rememberPagerState
import com.joeloewi.mediastoreimagegallery.R
import com.joeloewi.mediastoreimagegallery.data.mediastore.model.MediaStoreImage
import com.joeloewi.mediastoreimagegallery.data.mediastore.model.PagingPlaceholderKey
import com.joeloewi.mediastoreimagegallery.ui.theme.MediaStoreImageGalleryTheme
import com.joeloewi.mediastoreimagegallery.viewmodel.MediaStoreImagesViewModel
import dev.chrisbanes.snapper.ExperimentalSnapperApi

@ExperimentalSnapperApi
@ExperimentalPagerApi
@Composable
fun PagedMediaStoreImagesScreen(
    index: Int,
    mediaStoreImagesViewModel: MediaStoreImagesViewModel
) {
    val mediaStoreImages = mediaStoreImagesViewModel.mediaStoreImages.collectAsLazyPagingItems()

    PagedMediaStoreImagesContent(
        index = index,
        mediaStoreImages = mediaStoreImages
    )
}

@ExperimentalSnapperApi
@ExperimentalPagerApi
@Composable
fun PagedMediaStoreImagesContent(
    index: Int,
    mediaStoreImages: LazyPagingItems<MediaStoreImage>
) {
    val scaffoldState = rememberScaffoldState()
    val horizontalPagerState = rememberPagerState()

    LaunchedEffect(horizontalPagerState.pageCount) {
        if (horizontalPagerState.pageCount > index) {
            horizontalPagerState.runCatching {
                scrollToPage(page = index)
            }.onFailure { cause ->
                cause.localizedMessage?.let {
                    scaffoldState.snackbarHostState.showSnackbar(it)
                }
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState
    ) {
        HorizontalPager(
            count = mediaStoreImages.itemCount,
            flingBehavior = PagerDefaults.flingBehavior(horizontalPagerState),
            state = horizontalPagerState
        ) { page ->
            key(
                PagingPlaceholderKey(page)
            ) {
                val mediaStoreImage = mediaStoreImages[page]

                Image(
                    painter = rememberImagePainter(
                        data = mediaStoreImage?.contentUri,
                        builder = {
                            crossfade(true)
                            placeholder(R.drawable.image_placeholder)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Black),
                    contentDescription = null,
                    alignment = Alignment.Center
                )
            }
        }
    }
}

@ExperimentalSnapperApi
@ExperimentalPagerApi
@Preview(showBackground = true)
@Composable
fun PagedMediaStoreImagesScreenPreview() {
    MediaStoreImageGalleryTheme {
        /*PagedMediaStoreImagesContent(
            index = 0
        )*/
    }
}