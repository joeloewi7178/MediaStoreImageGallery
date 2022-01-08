package com.joeloewi.mediastoreimagegallery.data.mediastore

import com.joeloewi.mediastoreimagegallery.data.mediastore.dao.MediaStoreImageDao
import com.joeloewi.mediastoreimagegallery.data.mediastore.dao.impl.MediaStoreImageDaoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class MediaStoreModule {

    @Binds
    @ViewModelScoped
    abstract fun bindMediaStoreImageDao(mediaStoreImageDaoImpl: MediaStoreImageDaoImpl): MediaStoreImageDao
}