package com.joeloewi.mediastoreimagegallery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.joeloewi.mediastoreimagegallery.data.mediastore.dao.MediaStoreImageDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@HiltViewModel
class MediaStoreImagesViewModel @Inject constructor(
    mediaStoreImageDao: MediaStoreImageDao
) : ViewModel() {
    val cells = mediaStoreImageDao.pageSize

    val mediaStoreImages = Pager(
        config = PagingConfig(
            pageSize = cells,
        ),
        pagingSourceFactory = { mediaStoreImageDao.getImages() }
    ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
}