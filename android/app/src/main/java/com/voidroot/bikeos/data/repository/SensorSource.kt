package com.voidroot.bikeos.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * A source of [SensorSnapshot]s. [SensorRepository] picks between
 * implementations based on [AppSettings.hardwareFreeModeEnabled] - the
 * dashboard/DashboardViewModel never knows or cares which one is active.
 */
interface SensorSource {
    fun stream(): Flow<SensorSnapshot>
}
