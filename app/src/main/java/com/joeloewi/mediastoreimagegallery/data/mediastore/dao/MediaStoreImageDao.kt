package com.joeloewi.mediastoreimagegallery.data.mediastore.dao

import android.content.ContentResolver
import android.net.Uri
import androidx.paging.PagingSource
import com.joeloewi.mediastoreimagegallery.data.mediastore.model.MediaStoreImage

interface MediaStoreImageDao {

    val uri: Uri

    val contentResolver: ContentResolver

    fun getImages(limit: Int, offset: Int): PagingSource<Int, MediaStoreImage>
}