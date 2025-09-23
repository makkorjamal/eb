package com.makkor.eb.ui.permissions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PermissionsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "permit"
    }
    val text: LiveData<String> = _text
}