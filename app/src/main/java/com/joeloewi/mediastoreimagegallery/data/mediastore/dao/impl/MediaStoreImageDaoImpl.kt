package com.joeloewi.mediastoreimagegallery.data.mediastore.dao.impl

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.core.os.bundleOf
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.joeloewi.mediastoreimagegallery.data.mediastore.dao.MediaStoreImageDao
import com.joeloewi.mediastoreimagegallery.data.mediastore.model.MediaStoreImage
import javax.inject.Inject

class MediaStoreImageDaoImpl @Inject constructor(
    application: Application
) : MediaStoreImageDao {

    override val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    override val contentResolver: ContentResolver = application.contentResolver

    override fun getImages(limit: Int, offset: Int): PagingSource<Int, MediaStoreImage> =
        object : PagingSource<Int, MediaStoreImage>() {

            init {
                contentResolver.registerContentObserver(
                    uri,
                    true,
                    object : ContentObserver(Handler(Looper.getMainLooper())) {
                        override fun onChange(selfChange: Boolean) {
                            super.onChange(selfChange)
                            invalidate()
                        }
                    }
                )
            }

            private val pageSize = 16

            override fun getRefreshKey(state: PagingState<Int, MediaStoreImage>): Int? =
                state.anchorPosition?.let { state.closestPageToPosition(it)?.prevKey }

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaStoreImage> {

                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATE_ADDED,
                )

                val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val bundle = bundleOf(
                        ContentResolver.QUERY_ARG_OFFSET to limit * offset,
                        ContentResolver.QUERY_ARG_LIMIT to limit,
                        ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(MediaStore.Images.Media.DATE_ADDED),
                        ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                    )

                    contentResolver.query(
                        uri,
                        projection,
                        bundle,
                        null
                    )
                } else {
                    contentResolver.query(
                        uri,
                        projection,
                        null,
                        null,
                        "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT $limit OFFSET ${limit * offset}",
                        null
                    )
                }

                return try {
                    val mediaStoreImages = mutableListOf<MediaStoreImage>()

                    val cursorIndexOfId =
                        cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val cursorIndexOfDateAdded =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(cursorIndexOfId)
                        val dateAdded = cursor.getLong(cursorIndexOfDateAdded)
                        val contentUri = ContentUris.withAppendedId(
                            uri,
                            id
                        )

                        mediaStoreImages.add(
                            MediaStoreImage(
                                id = id,
                                contentUri = contentUri,
                                dateAdded = dateAdded
                            )
                        )
                    }

                    val position = params.key

                    LoadResult.Page(
                        data = mediaStoreImages,
                        prevKey = position?.let { it - 1 },
                        nextKey = if (mediaStoreImages.isEmpty()) null else position?.let { it + (params.loadSize / pageSize) }
                    )
                } catch (cause: Throwable) {
                    LoadResult.Error(cause)
                } finally {
                    cursor?.close()
                }
            }
        }

}
