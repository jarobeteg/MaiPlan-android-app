package com.example.maiplan.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.WindowCompat.getInsetsController
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import com.example.maiplan.main.MainActivity
import com.example.maiplan.network.NetworkChecker

open class BaseActivity : ComponentActivity() {
    lateinit var networkChecker: NetworkChecker
    lateinit var sessionManager: SessionManager

    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)

        config.fontScale = 1.0f
        config.densityDpi = DisplayMetrics.DENSITY_DEFAULT

        val context = newBase.createConfigurationContext(config)

        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable edge-to-edge display by telling the system not to fit content within system windows (status/navigation bars).
        setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        networkChecker = NetworkChecker(this)
        sessionManager = SessionManager(this)

        setupSystemBars()
    }

    protected open fun setupSystemBars() {
        val isDarkTheme = resources.configuration.uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES

        val insetsController = getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme

        window.setBackgroundDrawable(null)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    fun logout() {
        sessionManager.clearAll()

        val options = ActivityOptionsCompat.makeCustomAnimation(
            this,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent, options.toBundle())
        finish()
    }
}