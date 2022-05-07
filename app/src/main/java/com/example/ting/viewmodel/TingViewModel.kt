package com.example.ting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.ting.model.*
import com.example.ting.other.toast
import com.example.ting.repository.TingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TingViewModel @Inject constructor(
    private val tingRepository: TingRepository
) : ViewModel() {
    val albumList = tingRepository.getRecommendData().cachedIn(viewModelScope).asLiveData()
    fun getTrackList(albumId: Int) =
        tingRepository.getDetailData(albumId).cachedIn(viewModelScope).asLiveData()

    val playList by lazy { tingRepository.getPlayList() }
    private val _dailyWord by lazy { MutableStateFlow(DailyWord()) }
    val dailyWord get() = _dailyWord.asStateFlow()
    val newSong by lazy { tingRepository.getNewSong() }
    val topList by lazy { tingRepository.getTopList() }
    val categoryAll by lazy { tingRepository.getTypeList() }
    private val _categorySelected by lazy { MutableStateFlow(listOf<String>()) }
    val categorySelected get() = _categorySelected.asStateFlow()
    val highQualityPlaylist by lazy { tingRepository.getHighQualityPlaylist() }
    val playlistCatPager by lazy { mutableMapOf<String, Flow<PagingData<Playlists>>>() }
    private val _loginState by lazy { MutableStateFlow(LoginResponse()) }
    val loginState get() = _loginState.asStateFlow()
    private val _userData by lazy { MutableStateFlow(AccountDetail()) }
    val userData get() = _userData.asStateFlow()
    private val _isReady by lazy { MutableStateFlow(false) }
    val isReady get() = _isReady.asStateFlow()
    private val _userPlaylist by lazy { MutableStateFlow(UserPlaylist()) }
    val userPlaylist get() = _userPlaylist.asStateFlow()
    fun getTopPlaylist(category: String) =
        tingRepository.getTopPlaylist(category).cachedIn(viewModelScope)

    fun refreshDailyWord() {
        tingRepository.getDailyWord().onEach {
            _dailyWord.value = it
        }.launchIn(viewModelScope)
    }

    fun refreshSelectedCategory() {
        tingRepository.getHotPlaylistTags().onEach {
            _categorySelected.value = it
        }.launchIn(viewModelScope)
    }

    fun getSongList(id: Long) = tingRepository.getSongList(id)
    fun loginCellPhone(phone: String, password: String) {
        tingRepository.loginCellphone(phone, password).onEach {
            _loginState.value = it
        }.launchIn(viewModelScope)
    }

    fun refreshLibraryPage(id: Long) {
        tingRepository.getUserPlaylists(id).onEach {
            _userPlaylist.value = it
        }.launchIn(viewModelScope)
    }

    fun init() {
        combine(tingRepository.refreshLogin(), tingRepository.getAccountDetail()) { a, b ->
            a to b
        }.onEach {
            if (it.first["code"].toString() == "301") {
                "未登录".toast()
            }
            _userData.value = it.second
        }.onCompletion {
            _isReady.value = true
        }.launchIn(viewModelScope)
    }

    init {
        refreshDailyWord()
        refreshSelectedCategory()
        init()
    }
}