package featurea.desktop.jfx

import javafx.scene.control.*

open class FSMenuBar : MenuBar() {

    fun findCheckMenuItem(vararg titles: String): CheckMenuItem =
        findMenuItem(*titles, init = { CheckMenuItem(titles.last()) }) as CheckMenuItem

    fun findMenuItem(vararg titles: String, init: () -> MenuItem = { MenuItem(titles.last()) }): MenuItem {
        lateinit var result: MenuItem
        lateinit var currentMenu: Menu
        for (title in titles) {
            // filter
            if (title.isEmpty()) {
                val separatorMenuItem = SeparatorMenuItem()
                currentMenu.items.add(separatorMenuItem)
                result = separatorMenuItem
                break
            }

            // action
            val currentMenus = if (title === titles.first()) {
                menus
            } else {
                currentMenu.items.filterIsInstance<Menu>()
            }

            val nextMenu: Menu = currentMenus.findMenu(title)
            if (title === titles.first()) {
                if (!menus.contains(nextMenu)) {
                    menus.add(nextMenu)
                }
            } else {
                if (title === titles.last()) {
                    val menuItem: MenuItem = init()
                    if (!currentMenu.containsMenuItem(menuItem)) {
                        currentMenu.items.add(menuItem)
                    }
                    result = currentMenu.items.find { it.text == menuItem.text } ?: menuItem
                } else {
                    if (!currentMenu.items.contains(nextMenu)) {
                        currentMenu.items.add(nextMenu)
                    }
                }
            }
            currentMenu = nextMenu
        }
        return result
    }

}

fun MenuBar.removeAllTrailingSeparatorMenuItems() {
    for (menu in menus) {
        if (menu.items.last() is SeparatorMenuItem) {
            menu.items.removeLast()
        }
    }
}

/*internals*/

private fun List<Menu>.findMenu(title: String): Menu = find { it.text == title } ?: Menu(title)

private fun Menu.containsMenuItem(menuItem: MenuItem): Boolean = items.any { it.text == menuItem.text }
