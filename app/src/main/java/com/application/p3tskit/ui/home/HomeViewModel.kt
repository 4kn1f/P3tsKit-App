package com.application.p3tskit.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.application.p3tskit.data.pref.AuthModel
import com.application.p3tskit.data.remote.repository.AuthRepository

class HomeViewModel(private val authRepository: AuthRepository): ViewModel() {

    fun getSession(): LiveData<AuthModel> {
        return authRepository.getSession().asLiveData()
    }
}