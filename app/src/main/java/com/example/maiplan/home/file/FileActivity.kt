package com.example.maiplan.home.file

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.maiplan.theme.AppTheme

/**
 * This activity is responsible for managing and displaying File screens.
 *
 * The activity sets up the UI content using Jetpack Compose and applies the app a theme.
 * Sets up the navigation between File screens and with other screens on the Home component.
 */
class FileActivity : AppCompatActivity() {

    /**
     * Lifecycle method onCreate is called when the activity is created.
     *
     * Sets up the Compose UI and the navigation.
     *
     * @param savedInstanceState Saved state of this activity if previously existed.
     *
     * @see AppTheme
     * @see FileScreenWithNav
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppTheme { FileScreenWithNav(this) } }
    }
}