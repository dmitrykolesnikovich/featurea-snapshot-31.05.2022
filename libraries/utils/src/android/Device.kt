package featurea.utils

import android.annotation.SuppressLint
import android.provider.Settings
import featurea.android.MainActivityProxy
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

@SuppressLint("HardwareIds")
actual class Device actual constructor(override val module: Module) : Component {

    private val mainActivity by lazy { import(MainActivityProxy) }

    actual val id: String
        get() = Settings.Secure.getString(mainActivity.contentResolver, Settings.Secure.ANDROID_ID)

}
