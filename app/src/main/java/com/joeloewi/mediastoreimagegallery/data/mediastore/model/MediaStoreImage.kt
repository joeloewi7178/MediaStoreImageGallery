package com.joeloewi.mediastoreimagegallery.data.mediastore.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaStoreImage(
    val id: Long,
    val contentUri: Uri,
    val dateAdded: Long
): Parcelable
