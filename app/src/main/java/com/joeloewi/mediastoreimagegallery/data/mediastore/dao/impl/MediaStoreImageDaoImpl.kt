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

    override val pageSize: Int = 8

    override fun getImages(): PagingSource<Int, MediaStoreImage> =
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

            override fun getRefreshKey(state: PagingState<Int, MediaStoreImage>): Int? =
                state.anchorPosition?.let { state.closestPageToPosition(it) }?.prevKey?.minus(1)

            override val jumpingSupported: Boolean = true

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaStoreImage> {

                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATE_ADDED,
                )

                val limit = params.loadSize

                val offset = params.key ?: 0

                //bundle을 이용하는 쿼리는 26부터 사용가능한데 26, 27의 가상 기기에서 이미 로드된 데이터를 다시 로드 하는 문제가 있어
                //사용이 강제화된 29부터 사용하도록 함
                val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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

                    LoadResult.Page(
                        data = mediaStoreImages,
                        prevKey = if (mediaStoreImages.isEmpty() && params is LoadParams.Prepend) null else offset.takeIf { it > 0 }
                            ?.let { it - 1 },
                        nextKey = if (mediaStoreImages.isEmpty() && params is LoadParams.Append) null else offset + (limit / pageSize)
                    )
                } catch (cause: Throwable) {
                    LoadResult.Error(cause)
                } finally {
                    cursor?.close()
                }
            }
        }

}
