package com.joeloewi.mediastoreimagegallery.viewmodel

import android.media.MediaActionSound
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val mediaActionSound: MediaActionSound
) : ViewModel() {
    fun playShutterClickSound() {
        mediaActionSound.play(MediaActionSound.SHUTTER_CLICK)
    }
}