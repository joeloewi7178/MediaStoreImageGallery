package com.joeloewi.mediastoreimagegallery.util

import android.media.MediaActionSound
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UtilModule {

    //여러번 할당하면 메모리 문제가 발생하여 hilt로 관리
    @ViewModelScoped
    @Provides
    fun provideMediaActionSound(): MediaActionSound = MediaActionSound()
}