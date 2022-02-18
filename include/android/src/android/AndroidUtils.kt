package featurea.android

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import featurea.runtime.Module

fun Bitmap.flip(): Bitmap {
    val source: Bitmap = this
    val flipMatrix = Matrix().apply {
        postScale(1f, -1f)
    }
    val result = Bitmap.createBitmap(source, 0, 0, source.width, source.height, flipMatrix, true)
    return result
}

fun postDelayed(delay: Long, block: () -> Unit) {
    val handler = Handler()
    handler.postDelayed(block, delay)
}

fun <T : Fragment> FragmentActivity.getCurrentFragment(): T {
    val lastFragment = supportFragmentManager.fragments.lastOrNull()
    if (lastFragment is NavHostFragment) {
        return lastFragment.getChildFragmentManager().fragments[0] as T
    } else {
        return supportFragmentManager.fragments.lastOrNull() as T
    }
}

val Module.androidContext: FeatureaActivity get() = importComponent(MainActivityProxy)
val Module.mainActivity: FeatureaActivity get() = importComponent(MainActivityProxy)
val Context.sharedPreferences: SharedPreferences get() = PreferenceManager.getDefaultSharedPreferences(this)
