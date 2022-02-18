package featurea.android.simulator

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import featurea.android.ProgressBarProxy
import featurea.android.ProgressLayoutProxy
import featurea.android.ProgressTextViewProxy
import featurea.android.simulator.databinding.LoaderLayoutBinding
import featurea.runtime.Module

fun Module.installLoaderLayout(inflater: LayoutInflater, withSplash: Boolean): LoaderLayoutBinding {
    val binding: LoaderLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.loader_layout, null, false)
    val progressLayout = binding.root as ViewGroup

    provideComponent(ProgressBarProxy(binding.progressBar))
    provideComponent(ProgressLayoutProxy(progressLayout))
    provideComponent(ProgressTextViewProxy(binding.progressTextView))
    binding.progressBar.isVisible = !withSplash
    binding.progressTextView.isVisible = !withSplash
    progressLayout.setBackgroundColor(if (withSplash) Color.BLACK else Color.WHITE)
    /*
    binding.splashScreen.visibility = if (simulator.hasSplash) VISIBLE else GONE
    binding.splashScreen.setGifImageResource(R.drawable.splash_screen)
    binding.splashScreen.complete {
        progressLayoutProxy.complete()
    }
    mainActivityContentView.addView(binding.root)
    */
    return binding
}
