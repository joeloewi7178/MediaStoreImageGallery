package com.joeloewi.mediastoreimagegallery.data.mediastore.dao

import android.content.ContentResolver
import android.net.Uri
import androidx.paging.PagingSource
import com.joeloewi.mediastoreimagegallery.data.mediastore.model.MediaStoreImage

interface MediaStoreImageDao {

    val uri: Uri

    val contentResolver: ContentResolver

    val pageSize: Int

    suspend fun getImages(limit: Int, offset: Int): List<MediaStoreImage>

    suspend fun getTotalCount(): Int

    fun getPagedImages(): PagingSource<Int, MediaStoreImage>
}