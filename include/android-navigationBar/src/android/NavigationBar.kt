package featurea.android.navigationBar

import android.view.View
import android.view.Window
import android.view.WindowManager

fun Window.hideNavigationBar() {
    clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    var uiOptions = decorView.systemUiVisibility
    uiOptions = uiOptions or View.SYSTEM_UI_FLAG_LOW_PROFILE
    uiOptions = uiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    uiOptions = uiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    uiOptions = uiOptions or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    uiOptions = uiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
    decorView.systemUiVisibility = uiOptions
}

fun Window.showNavigationBar() {
    clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    var uiOptions = decorView.systemUiVisibility
    uiOptions = uiOptions and View.SYSTEM_UI_FLAG_LOW_PROFILE.inv()
    uiOptions = uiOptions and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
    uiOptions = uiOptions and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
    uiOptions = uiOptions and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.inv()
    uiOptions = uiOptions and View.SYSTEM_UI_FLAG_FULLSCREEN.inv()
    decorView.systemUiVisibility = uiOptions
}
