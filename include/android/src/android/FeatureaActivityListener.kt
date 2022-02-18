package featurea.android

import android.content.Intent
import android.content.res.Configuration

interface FeatureaActivityListener {
    suspend fun onNewIntent(intent: Intent) {}
    suspend fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}
    suspend fun onConfigurationChanged(configuration: Configuration) {}
}
