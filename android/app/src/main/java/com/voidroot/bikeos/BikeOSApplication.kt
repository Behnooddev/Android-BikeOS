package com.voidroot.bikeos

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * BikeOS Application entry point.
 *
 * Phase 2: Hilt is now wired (@HiltAndroidApp generates the DI container
 * root). Repositories/DAOs/Database are provided via [com.voidroot.bikeos.di.DatabaseModule].
 */
@HiltAndroidApp
class BikeOSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
