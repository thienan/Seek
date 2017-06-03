package de.aaronoe.picsplash.ui.photodetail

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import de.aaronoe.picsplash.R
import de.aaronoe.picsplash.data.model.PhotosReply
import de.aaronoe.picsplash.data.model.singleItem.SinglePhoto
import de.aaronoe.picsplash.data.remote.UnsplashInterface
import de.aaronoe.picsplash.util.DisplayUtils
import de.aaronoe.picsplash.util.PhotoDownloadUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Created by aaron on 01.06.17.
 *
 */
class DetailPresenterImpl(val context : Context,
                          val apiService: UnsplashInterface,
                          val view : DetailContract.View,
                          val photo: PhotosReply) : DetailContract.Presenter {

    val clientId = context.getString(R.string.client_id)

    override fun getIntentForImage(image: Bitmap) {

        val bmpUri = DisplayUtils.getLocalBitmapUri(context, image)
        if (bmpUri != null) {
            val shareIntent = Intent(Intent.ACTION_SEND)

            shareIntent.setDataAndType(bmpUri, "image/*")
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            view.showShareBottomsheet(shareIntent)
        } else {
            view.showSnackBarShareError(context.getString(R.string.no_share))
        }
    }

    override fun saveImage() {
        PhotoDownloadUtils.downloadImage(context,
                (context as PhotoDownloadUtils.imageDownloadListener),
                photo, PhotoDownloadUtils.TYPE_DOWNLOAD)
    }

    override fun setImageAsWallpaper() {
        PhotoDownloadUtils.downloadImage(context,
                (context as PhotoDownloadUtils.imageDownloadListener),
                photo, PhotoDownloadUtils.TYPE_WALLPAPER)
    }


    override fun getDetailsForPhoto() {
        val call = apiService.getPhotoById(photo.id, clientId)
        view.showLoading()
        call.enqueue(object : Callback<SinglePhoto> {
            override fun onResponse(call: Call<SinglePhoto>?, response: Response<SinglePhoto>) {
                view.hideLoading()
                view.showDetailPane(response.body())
            }
            override fun onFailure(call: Call<SinglePhoto>?, t: Throwable?) {
                view.hideMetaPane()
                view.showSnackBarShareError(context.getString(R.string.no_meta_data))
            }

        })
    }

}