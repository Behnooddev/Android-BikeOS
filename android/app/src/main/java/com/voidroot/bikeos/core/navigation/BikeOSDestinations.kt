package com.voidroot.bikeos.core.navigation

/**
 * Central route registry. String routes only live here - screens never
 * hardcode route strings, so adding a destination never requires touching
 * more than this file + the NavGraph.
 *
 * Startup flow: SPLASH decides (based on AppState) whether to land on
 * ONBOARDING, SIGNUP, or straight to HOME - see SplashScreen. HOME is the
 * permanent landing screen from the second app open onward; DASHBOARD
 * (the cluster) is only reached by pressing Start on HOME.
 */
object BikeOSDestinations {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val SIGNUP = "signup"

    const val CLUSTER_BOOT = "cluster_boot"
    const val DASHBOARD = "dashboard"

    const val MENU_HOME = "menu/home"
    const val MENU_APPEARANCE = "menu/appearance"
    const val MENU_CALCULATOR = "menu/calculator"
    const val MENU_SETTINGS = "menu/settings"
    const val MENU_ACCOUNT = "menu/account"
    const val MENU_ABOUT = "menu/about"
}
