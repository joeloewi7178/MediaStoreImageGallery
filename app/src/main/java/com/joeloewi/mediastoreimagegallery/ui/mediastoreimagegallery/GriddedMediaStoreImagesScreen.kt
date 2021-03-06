package com.joeloewi.mediastoreimagegallery.ui.mediastoreimagegallery

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.fade
import com.google.accompanist.placeholder.placeholder
import com.joeloewi.mediastoreimagegallery.data.mediastore.model.MediaStoreImage
import com.joeloewi.mediastoreimagegallery.data.mediastore.model.PagingPlaceholderKey
import com.joeloewi.mediastoreimagegallery.ui.theme.MediaStoreImageGalleryTheme
import com.joeloewi.mediastoreimagegallery.viewmodel.MediaStoreImagesViewModel

@ExperimentalFoundationApi
@ExperimentalPermissionsApi
@Composable
fun GriddedMediaStoreImagesScreen(
    navController: NavController,
    mediaStoreImagesViewModel: MediaStoreImagesViewModel
) {
    val localContext = LocalContext.current
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = mutableListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    )
    //????????? ???????????? ????????? ??? ?????????
    val (doNotShowRationale, onDoNotShowRationaleChange) = rememberSaveable { mutableStateOf(false) }

    PermissionsRequired(
        multiplePermissionsState = multiplePermissionsState,
        //???????????? ???????????? ?????????????????? ???????????? ???????????? ????????? ??????
        permissionsNotGrantedContent = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (doNotShowRationale) {
                    Text(text = "????????? ???????????? ??? ????????? ????????? ??? ????????????.")
                } else {
                    Text(text = "?????? ?????? ??? ??????????????? ???????????? ?????? ????????? ???????????????.")
                    Row {
                        TextButton(
                            onClick = { multiplePermissionsState.launchMultiplePermissionRequest() }
                        ) {
                            Text("??????")
                        }
                        TextButton(
                            onClick = { onDoNotShowRationaleChange(true) }
                        ) {
                            Text("??????")
                        }
                    }
                }
            }
        },
        //???????????? ?????? ?????? ????????? ??? ??????
        permissionsNotAvailableContent = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "?????? ?????? ?????? ???????????? ??? ?????? ????????? ?????????????????? ?????? ????????? ?????? ?????????.")
                Button(
                    onClick = {
                        //??? ?????? ??????
                        val appDetailIntent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${localContext.packageName}")
                        ).apply {
                            addCategory(Intent.CATEGORY_DEFAULT)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }

                        localContext.startActivity(appDetailIntent)
                    }
                ) {
                    Text("????????????")
                }
            }
        }
    ) {
        val mediaStoreImages = mediaStoreImagesViewModel.mediaStoreImages.collectAsLazyPagingItems()

        GriddedMediaStoreImageContent(
            mediaStoreImages = mediaStoreImages,
            cells = mediaStoreImagesViewModel.cells,
            onImageClick = { index ->
                navController.navigate("pagedMediaStoreImagesScreen/${index}")
            },
            onAddImageClick = {
                navController.navigate("cameraScreen")
            }
        )
    }
}

@ExperimentalFoundationApi
@Composable
fun GriddedMediaStoreImageContent(
    mediaStoreImages: LazyPagingItems<MediaStoreImage>,
    cells: Int,
    onImageClick: (Int) -> Unit,
    onAddImageClick: () -> Unit
) {
    //???????????? ???????????? ?????? ???????????? ???????????? ??????(??????)??? ??????????????? ????????? ?????????
    val mediaStoreImagesLoadState = mediaStoreImages.loadState
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(mediaStoreImagesLoadState) {
        var errorOccurred = false

        mediaStoreImages.loadState.source.forEach { _, loadState ->
            if (loadState is LoadState.Error) {
                errorOccurred = true
                return@forEach
            }
        }

        if (errorOccurred) {
            val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                message = "????????? ??????????????????.",
                actionLabel = "?????? ??????",
                duration = SnackbarDuration.Indefinite
            )

            when (snackbarResult) {
                SnackbarResult.Dismissed -> {

                }
                SnackbarResult.ActionPerformed -> {
                    mediaStoreImages.retry()
                }
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddImageClick) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = Icons.Default.AddAPhoto.name
                )
            }
        }
    ) {
        if (mediaStoreImagesLoadState.refresh is LoadState.NotLoading && mediaStoreImagesLoadState.append.endOfPaginationReached && mediaStoreImages.itemCount == 0) {
            //????????? ?????? ???
            //???????????? ????????? ????????? ??? ?????? ????????? ?????? ?????? ?????? ???????????? ???????????? ???????????? ??????
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    modifier = Modifier.fillMaxSize(0.2f),
                    imageVector = Icons.Default.BrokenImage,
                    contentDescription = Icons.Default.BrokenImage.name
                )
                Text(text = "???????????? ????????????.")
            }
        } else {
            LazyVerticalGrid(
                cells = GridCells.Fixed(count = cells)
            ) {
                itemsIndexed(
                    items = mediaStoreImages,
                    key = { _, item ->
                        item.id
                    }
                ) { index, mediaStoreImage ->
                    val density = LocalContext.current.resources.displayMetrics.density
                    val width = LocalView.current.width
                    val size = (width / density) / cells

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(mediaStoreImage?.contentUri)
                            .build(),
                        modifier = Modifier
                            .size(size.dp)
                            .clickable { onImageClick(index) }
                            .placeholder(
                                visible = mediaStoreImage == null,
                                color = Color.Gray,
                                highlight = PlaceholderHighlight.fade(
                                    highlightColor = MaterialTheme.colors.background,
                                )
                            ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                }
            }
        }
    }
}

@ExperimentalFoundationApi
fun <T : Any> LazyGridScope.itemsIndexed(
    items: LazyPagingItems<T>,
    key: ((index: Int, item: T) -> Any)? = null,
    spans: (LazyGridItemSpanScope.(index: Int, item: T?) -> GridItemSpan)? = null,
    itemContent: @Composable LazyItemScope.(index: Int, value: T?) -> Unit
) = items(
    count = items.itemCount,
    spans?.let { { spans(it, items.peek(it)) } }
) { index ->
    key(
        if (key == null) null else {
            val item = items.peek(index)
            if (item == null) {
                PagingPlaceholderKey(index)
            } else {
                key(index, item)
            }
        }
    ) {
        itemContent(index, items[index])
    }
}

@Preview(showBackground = true)
@Composable
fun GriddedMediaStoreImagesScreenPreview() {
    MediaStoreImageGalleryTheme {
        /*GriddedMediaStoreImageContent(
            onImageClick = {},
            onAddImageClick = {}
        )*/
    }
}