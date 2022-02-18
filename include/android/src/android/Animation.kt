package featurea.android

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.LinearInterpolator

fun fadeInAnimation(time: Long, action: () -> Unit = {}) = AnimationSet(false).apply {
    addAnimation(AlphaAnimation(0f, 1f).apply {
        interpolator = LinearInterpolator()
        duration = time
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) = action()
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
        })
    })
}

fun fadeOutAnimation(time: Long, action: () -> Unit) = AnimationSet(false).apply {
    addAnimation(AlphaAnimation(1f, 0f).apply {
        interpolator = LinearInterpolator()
        duration = time
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) = action()
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
        })
    })
}
