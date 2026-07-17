package com.voidroot.bikeos.data.backup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.voidroot.bikeos.data.local.dao.BikeProfileDao
import com.voidroot.bikeos.data.local.dao.DashboardWidgetDao
import com.voidroot.bikeos.data.local.dao.RideSessionDao
import com.voidroot.bikeos.data.local.dao.SettingsDao
import com.voidroot.bikeos.data.local.dao.UserProfileDao
import com.voidroot.bikeos.data.local.entity.BikeProfileEntity
import com.voidroot.bikeos.data.local.entity.DashboardWidgetEntity
import com.voidroot.bikeos.data.local.entity.RideSessionEntity
import com.voidroot.bikeos.data.local.entity.SettingsEntity
import com.voidroot.bikeos.data.local.entity.UserProfileEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * Backup format per the Data Architecture spec: a single ZIP containing
 * user.json / bike.json / settings.json / dashboard.json / rides.json /
 * metadata.json.
 *
 * Phase 2 scope: the export/import logic itself, using app-private storage
 * (`context.filesDir`) as the source/destination. Wiring this to a real
 * file picker (Storage Access Framework - so the user can choose where the
 * ZIP goes / pick a ZIP to restore) is left for a later UI polish pass;
 * that's a UI concern, not a data-architecture one, so it doesn't block
 * this phase's "runnable" requirement.
 *
 * NOTE - Privacy: this backup contains personal data (name, email, age,
 * height, weight). It is currently unencrypted. Encrypting the ZIP (e.g.
 * password-protected or wrapped with Android Keystore-derived key) is a
 * known gap flagged in the architecture review and should be closed before
 * this feature is exposed to real users, not deferred indefinitely.
 */
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userProfileDao: UserProfileDao,
    private val bikeProfileDao: BikeProfileDao,
    private val settingsDao: SettingsDao,
    private val dashboardWidgetDao: DashboardWidgetDao,
    private val rideSessionDao: RideSessionDao
) {
    companion object {
        const val BACKUP_FILE_NAME = "BikeOS_Backup.zip"
        private const val SCHEMA_VERSION = 1
    }

    private fun backupFile(): File = File(context.filesDir, BACKUP_FILE_NAME)

    suspend fun export(): File {
        val user = userProfileDao.get() ?: UserProfileEntity()
        val bike = bikeProfileDao.get() ?: BikeProfileEntity()
        val settings = settingsDao.get() ?: SettingsEntity()
        val widgets = dashboardWidgetDao.getAll()

        val file = backupFile()
        ZipOutputStream(FileOutputStream(file)).use { zip ->
            writeJsonEntry(zip, "user.json", userToJson(user))
            writeJsonEntry(zip, "bike.json", bikeToJson(bike))
            writeJsonEntry(zip, "settings.json", settingsToJson(settings))
            writeJsonEntry(zip, "dashboard.json", widgetsToJson(widgets))
            writeJsonEntry(zip, "rides.json", ridesToJson(rideSessionDao.getAllOnce()))
            writeJsonEntry(zip, "metadata.json", metadataJson())
        }
        return file
    }

    /**
     * Restores from [BACKUP_FILE_NAME] in app-private storage.
     * Validates schema version before touching any table - an incompatible
     * or corrupt backup must never partially overwrite the user's data.
     */
    suspend fun import(): Result<Unit> {
        val file = backupFile()
        if (!file.exists()) return Result.failure(IllegalStateException("No backup file found"))

        return try {
            val entries = mutableMapOf<String, String>()
            ZipInputStream(FileInputStream(file)).use { zip ->
                var entry: ZipEntry? = zip.nextEntry
                while (entry != null) {
                    entries[entry.name] = zip.readBytes().decodeToString()
                    entry = zip.nextEntry
                }
            }

            val metadata = entries["metadata.json"]?.let { JSONObject(it) }
                ?: return Result.failure(IllegalStateException("Backup missing metadata.json"))
            if (metadata.optInt("schemaVersion") != SCHEMA_VERSION) {
                return Result.failure(IllegalStateException("Backup schema version incompatible"))
            }

            entries["user.json"]?.let { userProfileDao.upsert(userFromJson(JSONObject(it))) }
            entries["bike.json"]?.let { bikeProfileDao.upsert(bikeFromJson(JSONObject(it))) }
            entries["settings.json"]?.let { settingsDao.upsert(settingsFromJson(JSONObject(it))) }
            entries["dashboard.json"]?.let { dashboardWidgetDao.upsertAll(widgetsFromJson(JSONArray(it))) }
            // Ride history is intentionally NOT restored automatically here -
            // re-inserting historical rides needs de-duplication against
            // whatever already exists locally, which is a Phase 2.x follow-up,
            // not implemented yet to avoid silently duplicating ride history.

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun writeJsonEntry(zip: ZipOutputStream, name: String, json: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(json.toByteArray())
        zip.closeEntry()
    }

    private fun metadataJson() = JSONObject().apply {
        put("schemaVersion", SCHEMA_VERSION)
        put("exportedAtEpochMs", System.currentTimeMillis())
    }.toString()

    private fun userToJson(e: UserProfileEntity) = JSONObject().apply {
        put("username", e.username); put("name", e.name); put("email", e.email)
        put("age", e.age); put("heightCm", e.heightCm); put("weightKg", e.weightKg)
    }.toString()

    private fun userFromJson(o: JSONObject) = UserProfileEntity(
        0, o.optString("username"), o.optString("name"), o.optString("email"),
        o.optInt("age"), o.optInt("heightCm"), o.optInt("weightKg")
    )

    private fun bikeToJson(e: BikeProfileEntity) = JSONObject().apply {
        put("bikeName", e.bikeName); put("bikeType", e.bikeType); put("wheelSizeInches", e.wheelSizeInches)
        put("frontGearCount", e.frontGearCount); put("rearGearCount", e.rearGearCount)
        put("currentFrontGear", e.currentFrontGear); put("currentRearGear", e.currentRearGear)
    }.toString()

    private fun bikeFromJson(o: JSONObject) = BikeProfileEntity(
        0, o.optString("bikeName"), o.optString("bikeType"),
        o.optDouble("wheelSizeInches", 27.5).toFloat(),
        o.optInt("frontGearCount", 1), o.optInt("rearGearCount", 1),
        o.optInt("currentFrontGear", 1), o.optInt("currentRearGear", 1)
    )

    private fun settingsToJson(e: SettingsEntity) = JSONObject().apply {
        put("useMetricUnits", e.useMetricUnits); put("soundEnabled", e.soundEnabled)
        put("maxSpeedAlertKmh", e.maxSpeedAlertKmh)
    }.toString()

    private fun settingsFromJson(o: JSONObject) = SettingsEntity(
        0, o.optBoolean("useMetricUnits", true), o.optBoolean("soundEnabled", true),
        o.optInt("maxSpeedAlertKmh", 40)
    )

    private fun widgetsToJson(widgets: List<DashboardWidgetEntity>) = JSONArray().apply {
        widgets.forEach {
            put(JSONObject().apply {
                put("widgetKey", it.widgetKey); put("position", it.position); put("enabled", it.enabled)
            })
        }
    }.toString()

    private fun widgetsFromJson(array: JSONArray): List<DashboardWidgetEntity> =
        (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            DashboardWidgetEntity(o.getString("widgetKey"), o.getInt("position"), o.optBoolean("enabled", true))
        }

    private fun ridesToJson(rides: List<RideSessionEntity>): String = JSONArray().apply {
        rides.forEach { r ->
            put(JSONObject().apply {
                put("startTimeEpochMs", r.startTimeEpochMs)
                put("endTimeEpochMs", r.endTimeEpochMs)
                put("durationSeconds", r.durationSeconds)
                put("distanceKm", r.distanceKm)
                put("calories", r.calories)
                put("avgSpeedKmh", r.avgSpeedKmh)
                put("maxSpeedKmh", r.maxSpeedKmh)
                put("avgCadenceRpm", r.avgCadenceRpm)
                put("maxCadenceRpm", r.maxCadenceRpm)
                put("rideMode", r.rideMode)
            })
        }
    }.toString()
}
