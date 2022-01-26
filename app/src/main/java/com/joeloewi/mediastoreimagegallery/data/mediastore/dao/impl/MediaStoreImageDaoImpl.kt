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

    override suspend fun getImages(limit: Int, offset: Int): List<MediaStoreImage> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
        )

        //bundle을 이용하는 쿼리는 26부터 사용가능한데 26, 27의 가상 기기에서 이미 로드된 데이터를 다시 로드 하는 문제가 있어
        //사용이 강제화된 29부터 사용하도록 함
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val bundle = bundleOf(
                ContentResolver.QUERY_ARG_OFFSET to offset,
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
                "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT $limit OFFSET $offset",
                null
            )
        }!!.use {
            val mediaStoreImages = mutableListOf<MediaStoreImage>()

            val cursorIndexOfId =
                it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val cursorIndexOfDateAdded =
                it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val id = it.getLong(cursorIndexOfId)
                val dateAdded = it.getLong(cursorIndexOfDateAdded)
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

            mediaStoreImages
        }
    }

    override suspend fun getTotalCount(): Int {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver.query(
                uri,
                projection,
                null,
                null
            )
        } else {
            contentResolver.query(
                uri,
                projection,
                null,
                null,
                null,
                null
            )
        }!!.use {
            it.count
        }
    }

    override fun getPagedImages(): PagingSource<Int, MediaStoreImage> =
        object : PagingSource<Int, MediaStoreImage>() {

            private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    invalidate()
                }
            }

            init {
                contentResolver.registerContentObserver(
                    uri,
                    true,
                    contentObserver
                )

                registerInvalidatedCallback {
                    contentResolver.unregisterContentObserver(contentObserver)
                }
            }

            override fun getRefreshKey(state: PagingState<Int, MediaStoreImage>): Int? {
                val page = state.anchorPosition?.let { state.closestPageToPosition(it) }
                val prevKey = page?.prevKey ?: 0

                val item = state.anchorPosition?.let { state.closestItemToPosition(it) }

                return prevKey + (page?.data?.indexOf(item)?.takeIf { it >= 0 } ?: 0)
            }

            override val jumpingSupported: Boolean = true

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaStoreImage> {
                var limit = params.loadSize
                var offset = params.key ?: 0

                if (params is LoadParams.Refresh) {
                    //params.loadSize가 initialLoadSize
                    //기본값은 initialLoadSize = pageSize * 3
                    if (params.placeholdersEnabled) {
                        limit = maxOf(limit / pageSize, 2) * pageSize

                        val idealStart = offset - limit / 2
                        offset = maxOf(0, idealStart / pageSize * pageSize)
                    } else {
                        offset = maxOf(0, offset - limit / 2)
                    }
                } else {
                    if (params is LoadParams.Prepend) {
                        limit = minOf(limit, offset)
                        offset -= limit
                    }
                }

                return try {
                    val mediaStoreImages = getImages(limit, offset)
                    val totalCount = getTotalCount()

                    if (invalid) {
                        LoadResult.Invalid()
                    } else {
                        val prevKey = if (offset == 0) null else offset
                        val nextKey = offset + mediaStoreImages.size

                        if (params is LoadParams.Refresh) {
                            LoadResult.Page(
                                data = mediaStoreImages,
                                prevKey = if (mediaStoreImages.isEmpty()) null else prevKey,
                                nextKey = if (mediaStoreImages.isEmpty()) null else nextKey,
                                itemsBefore = offset,
                                itemsAfter = totalCount - mediaStoreImages.size - offset
                            )
                        } else {
                            LoadResult.Page(
                                data = mediaStoreImages,
                                prevKey = if (mediaStoreImages.isEmpty() && params is LoadParams.Prepend) null else prevKey,
                                nextKey = if (mediaStoreImages.isEmpty() && params is LoadParams.Append) null else nextKey,
                                itemsBefore = offset,
                                itemsAfter = totalCount - mediaStoreImages.size - offset
                            )
                        }
                    }
                } catch (cause: Throwable) {
                    LoadResult.Error(cause)
                }
            }
        }

}
