package com.vaagna.driver

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        Glide
            .with(this)
            .load(R.raw.ldr)
            .into(launchAnimation)
        var count = 0
        object : CountDownTimer(11500L, 1150L) {
            override fun onFinish() {
                startActivity(Intent(this@MainActivity, Dashboard::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                this.cancel()
            }

            override fun onTick(millisUntilFinished: Long) {
                if (count++ == 9) {
                    launchAnimation.setImageDrawable(null)
                    progressBar.visibility = View.INVISIBLE
                    blank.visibility = View.VISIBLE
                }
            }
        }.start()

    }
}
