package com.voidroot.bikeos.core.navigation

/**
 * Central route registry. String routes only live here - screens never
 * hardcode route strings, so adding a destination never requires touching
 * more than this file + the NavGraph.
 */
object BikeOSDestinations {
    const val DASHBOARD = "dashboard"

    const val MENU_HOME = "menu/home"
    const val MENU_APPEARANCE = "menu/appearance"
    const val MENU_CALCULATOR = "menu/calculator"
    const val MENU_SETTINGS = "menu/settings"
    const val MENU_ACCOUNT = "menu/account"
    const val MENU_ABOUT = "menu/about"
}
