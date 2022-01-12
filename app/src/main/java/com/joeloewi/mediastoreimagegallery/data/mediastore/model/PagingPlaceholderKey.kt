package com.joeloewi.mediastoreimagegallery.data.mediastore.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PagingPlaceholderKey(
    private val index: Int
): Parcelable
