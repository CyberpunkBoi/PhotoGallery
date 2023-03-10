package com.example.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.telecom.Call
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

private const val TAG = "FlickrFetchr"

class FlickrFetchr {

    private val flickrApi: FlickrApi

    init {
        val retrofit: Retrofit =
            Retrofit.Builder()
                .baseUrl("https://api.flickr.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        flickrApi =
            retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        val responseLiveData:
                MutableLiveData<List<GalleryItem>> =
            MutableLiveData()
        val flickrRequest: Call<FlickrResponse> =
            flickrApi.fetchPhotos()

        flickrRequest.enqueue(object :
            Callback<FlickrResponse> {
            override fun onFailure(call:
                                   Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos"
                    , t)
            }

            override fun onResponse(
                call: Call<FlickrResponse>,
                response: Response<FlickrResponse>
            ) {
                Log.d(TAG, "Response received")
                val flickrResponse:
                        FlickrResponse? = response.body()
                val photoResponse:
                        PhotoResponse? = flickrResponse?.photos
                var galleryItems:
                        List<GalleryItem> = photoResponse?.galleryItems
                    ?: mutableListOf()
                galleryItems =
                    galleryItems.filterNot {
                        it.url.isBlank()
                    }
                responseLiveData.value =
                    galleryItems

            }
        })

        return responseLiveData
    }

    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> =
            flickrApi.fetchUrlBytes(url).execute()
        val bitmap =
            response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG,
            "Decoded bitmap=$bitmap from Response=$response")
        return bitmap
    }
}