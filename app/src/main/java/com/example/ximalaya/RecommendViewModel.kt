package com.example.ximalaya

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack
import com.ximalaya.ting.android.opensdk.model.album.Album
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RecommendViewModel : ViewModel() {
    val albumList = liveData {
        emit(getRecommendData())
    }

    //获取推荐内容
    private suspend fun getRecommendData() = suspendCoroutine<List<Album>> {
        val map = HashMap<String, String>()
        map[DTransferConstants.LIKE_COUNT] = Constants.RECOMMEND_COUNT.toString()
        CommonRequest.getGuessLikeAlbum(map, object : IDataCallBack<GussLikeAlbumList> {
            override fun onSuccess(p0: GussLikeAlbumList?) {
                it.resume(p0?.albumList!!)
            }

            override fun onError(p0: Int, p1: String) {
                Log.d("Hello", "onError: $p0")
                Log.d("Hello", "onError: $p1")
            }
        })
    }
}