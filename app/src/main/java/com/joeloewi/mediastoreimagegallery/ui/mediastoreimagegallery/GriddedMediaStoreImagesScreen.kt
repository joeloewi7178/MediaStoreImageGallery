package com.joeloewi.mediastoreimagegallery.ui.mediastoreimagegallery

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyGridScope
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.joeloewi.mediastoreimagegallery.R
import com.joeloewi.mediastoreimagegallery.data.mediastore.model.MediaStoreImage
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
    //권한을 요구하는 이유를 볼 것인지
    val (doNotShowRationale, onDoNotShowRationaleChange) = rememberSaveable { mutableStateOf(false) }

    PermissionsRequired(
        multiplePermissionsState = multiplePermissionsState,
        //허가되지 않았거나 요청하였지만 사용자가 하나라도 거부한 경우
        permissionsNotGrantedContent = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (doNotShowRationale) {
                    Text(text = "권한이 거부되어 이 기능을 사용할 수 없습니다.")
                } else {
                    Text(text = "사진 촬영 및 가져오기를 수행하기 위한 권한이 필요합니다.")
                    Row {
                        TextButton(
                            onClick = { multiplePermissionsState.launchMultiplePermissionRequest() }
                        ) {
                            Text("허용")
                        }
                        TextButton(
                            onClick = { onDoNotShowRationaleChange(true) }
                        ) {
                            Text("거부")
                        }
                    }
                }
            }
        },
        //사용자가 다시 묻지 않음을 한 경우
        permissionsNotAvailableContent = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "다시 묻지 않음 처리되어 앱 정보 화면의 권한설정에서 권한 허가를 해야 합니다.")
                Button(
                    onClick = {
                        //앱 정보 화면
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
                    Text("이동하기")
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
    //이미지를 리스트화 하는 경우에는 이미지의 높이(크기)가 지정되어야 제대로 표시됨
    val density = LocalContext.current.resources.displayMetrics.density
    val width = LocalView.current.width
    val size = (width / density) / cells

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
        LazyVerticalGrid(
            cells = GridCells.Adaptive(minSize = size.dp)
        ) {
            itemsIndexed(
                items = mediaStoreImages,
                key = { index, mediaStoreImage ->
                    mediaStoreImage.id
                }
            ) { index, mediaStoreImage ->
                Image(
                    painter = rememberImagePainter(
                        data = mediaStoreImage?.contentUri,
                        builder = {
                            crossfade(true)
                            placeholder(R.drawable.image_placeholder)
                        }
                    ),
                    modifier = Modifier
                        .size(size = size.dp)
                        .clickable { onImageClick(index) },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
            }
        }
    }
}

@ExperimentalFoundationApi
fun <T : Any> LazyGridScope.itemsIndexed(
    items: LazyPagingItems<T>,
    key: ((index: Int, item: T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(index: Int, value: T?) -> Unit
) {
    items(
        count = items.itemCount
    ) { index ->
        key(
            if (key == null) null else {
                val item = items.peek(index)
                if (item == null) {
                    index
                } else {
                    key(index, item)
                }
            }
        ) {
            itemContent(index, items[index])
        }
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