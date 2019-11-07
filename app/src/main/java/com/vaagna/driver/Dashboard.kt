package com.vaagna.driver

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.activity_dashboard.*

class Dashboard : AppCompatActivity(), InstallStateUpdatedListener {

    private lateinit var appUpdateManager: AppUpdateManager
    private val REQUEST_CODE_FLEXIBLE = 1
    private val REQUEST_CODE_IMMEDIATE = 2
    private var videoId = 0
    private lateinit var mMediaPlayer: MediaPlayer
    private val videoColors = mapOf(
        Pair(R.raw.space, R.color.Two)
        , Pair(R.raw.bloodmoon, R.color.Black)
        , Pair(R.raw.waves, R.color.One)
        , Pair(R.raw.city, R.color.Four)
        , Pair(R.raw.tenor, R.color.Three)
        , Pair(R.raw.circle, R.color.One)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.attributes.flags =
                window.attributes.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
            window.attributes.flags =
                window.attributes.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION.inv()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        }
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(this)
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            Log.i("Dashboard","onSuccess listener")
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                Log.i("Dashboard","UPDATE_AVAILABLE, flex:${it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)}, imm:${it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)}")
                if (it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) || it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
                    requestUpdate(it,true)
                else if (it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
                    requestUpdate(it,false)
            }
        }
        var pos = 0
        videoView.setOnPreparedListener {
            mMediaPlayer = it
            mMediaPlayer.isLooping = true
            mMediaPlayer.start()
        }
        videoView.setOnClickListener {
            if (mMediaPlayer.isPlaying) {
                mMediaPlayer.pause()
                pos = mMediaPlayer.currentPosition
            } else {
                mMediaPlayer.start()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    mMediaPlayer.seekTo((pos).toLong(), MediaPlayer.SEEK_CLOSEST)
                else
                    mMediaPlayer.seekTo(pos)
            }
        }
        VideoBackground.setOnClickListener {
            videoView.visibility = View.INVISIBLE
            getVideo()
        }
        videoView.setZOrderOnTop(true)
        getVideo()
    }

    private fun getVideo() {
        var temp = videoColors.keys.random()
        while (videoId == temp) {
            temp = videoColors.keys.random()
        }
        videoId = temp
        VideoBackground.setBackgroundColor(resources.getColor(videoColors.getValue(videoId)))
        videoView.setVideoURI(Uri.parse("android.resource://$packageName/${videoId}"))
        videoView.visibility = View.VISIBLE
    }

    override fun onStateUpdate(state: InstallState?) {
        Log.i("Dashboard","onStateUpdate ${state?.installStatus()}")
        when (state?.installStatus()) {
            InstallStatus.FAILED -> {
                makeSnackBar("Update FAILED!")
                Log.i("Dashboard","onStateUpdate FAILED")
            }
            InstallStatus.CANCELED -> {
                makeSnackBar("Update CANCELED!")
                Log.i("Dashboard","onStateUpdate CANCELED")
            }
            InstallStatus.DOWNLOADED -> {
                Log.i("Dashboard","onStateUpdate DOWNLOAD")
                Snackbar
                    .make(findViewById<View>(android.R.id.content),"Update Completed",Snackbar.LENGTH_INDEFINITE)
                    .setAction("Restart"){
                        appUpdateManager.completeUpdate()
                        appUpdateManager.unregisterListener(this)
                    }
                    .show()
            }
//            InstallStatus.INSTALLED -> { makeSnackBar("Update INSTALLED!") }
//            InstallStatus.DOWNLOADING -> { makeSnackBar("Update DOWNLOADING!") }
//            InstallStatus.INSTALLING -> { makeSnackBar("Update INSTALLING!") }
//            InstallStatus.PENDING->{}
//            InstallStatus.UNKNOWN->{}
            else -> {
//                Log.i("Dashboard","onStateUpdate ${state?.installStatus()}")
            }
        }
    }

    private fun requestUpdate(appUpdateInfo: AppUpdateInfo, isFlexible : Boolean) {
        val type = if(isFlexible) AppUpdateType.FLEXIBLE else AppUpdateType.IMMEDIATE
        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, type, this, REQUEST_CODE_FLEXIBLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("Dashboard","onActivityResult $resultCode")
        when (resultCode) {
            Activity.RESULT_OK -> { }
            Activity.RESULT_CANCELED -> { makeSnackBar("Update request cancelled!") }
            ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> { makeSnackBar("Update request failed!") }
        }
    }

    private fun makeSnackBar(message: String) {
        Snackbar
            .make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(this)
    }
}
