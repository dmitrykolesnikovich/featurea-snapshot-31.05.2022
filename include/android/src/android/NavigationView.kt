package featurea.android

import android.view.MenuItem
import com.google.android.material.navigation.NavigationView

fun NavigationView.assignCheckedMenuItem(menuItem: MenuItem) {
    uncheckAllMenuItems()
    menuItem.isChecked = true
}

fun NavigationView.uncheckAllMenuItems() {
    for (index in 0 until menu.size()) {
        menu.getItem(index).isChecked = false
    }
}
