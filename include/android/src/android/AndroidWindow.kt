package featurea.android

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.Snackbar

val Activity.contentView: View get() = findViewById(android.R.id.content)

val Activity.screenSizeDp: Size
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    get() {
        val rect = Rect()
        contentView.getWindowVisibleDisplayFrame(rect)
        return Size(rect.width(), rect.height())
    }

fun linearLayoutOf(view: View): LinearLayout {
    view.layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    val linearLayout = LinearLayout(view.context).apply {
        layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        orientation = LinearLayout.VERTICAL
    }
    linearLayout.addView(view)
    return linearLayout
}

fun <T : ViewDataBinding> LayoutInflater.inflate(layoutId: Int, container: ViewGroup?): T =
    DataBindingUtil.inflate<T>(this, layoutId, container, false)

fun <T : ViewDataBinding> Activity.bindLayout(layoutId: Int): T = DataBindingUtil.setContentView(this, layoutId)

fun Activity.snackbar(message: String) = Snackbar.make(contentView, message, Snackbar.LENGTH_SHORT).show()
