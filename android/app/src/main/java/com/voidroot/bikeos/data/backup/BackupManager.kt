package com.voidroot.bikeos.data.backup

import android.content.Context
import com.voidroot.bikeos.data.local.dao.AppStateDao
import com.voidroot.bikeos.data.local.dao.BikeProfileDao
import com.voidroot.bikeos.data.local.dao.DashboardWidgetDao
import com.voidroot.bikeos.data.local.dao.RideSessionDao
import com.voidroot.bikeos.data.local.dao.SettingsDao
import com.voidroot.bikeos.data.local.dao.ThemeColorsDao
import com.voidroot.bikeos.data.local.dao.UserProfileDao
import com.voidroot.bikeos.data.local.entity.AppStateEntity
import com.voidroot.bikeos.data.local.entity.BikeProfileEntity
import com.voidroot.bikeos.data.local.entity.DashboardWidgetEntity
import com.voidroot.bikeos.data.local.entity.RideSessionEntity
import com.voidroot.bikeos.data.local.entity.SettingsEntity
import com.voidroot.bikeos.data.local.entity.ThemeColorsEntity
import com.voidroot.bikeos.data.local.entity.UserProfileEntity
import dagger.hilt.android.qualifiers.ApplicationContext
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
 * Backup format: BikeOS's own `.bop` extension (a plain ZIP internally,
 * just branded) containing user.json / bike.json / settings.json /
 * dashboard.json / theme.json / app_state.json / rides.json / metadata.json
 * - per the builder's request to export "everything, including even the
 * suggestion/preference data", so restoring on a new phone picks up right
 * where the old one left off (including onboarding/signup being
 * considered already done).
 *
 * Still app-private-storage only (no file-picker UX yet) and still
 * unencrypted despite containing a password hash+salt - both flagged
 * previously and still true; the password is hashed+salted (not
 * plaintext) but a `.bop` file is still sensitive and should eventually be
 * encrypted before this feature is exposed to real users.
 */
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userProfileDao: UserProfileDao,
    private val bikeProfileDao: BikeProfileDao,
    private val settingsDao: SettingsDao,
    private val dashboardWidgetDao: DashboardWidgetDao,
    private val rideSessionDao: RideSessionDao,
    private val themeColorsDao: ThemeColorsDao,
    private val appStateDao: AppStateDao
) {
    companion object {
        const val BACKUP_FILE_NAME = "BikeOS_Backup.bop"
        private const val SCHEMA_VERSION = 2
    }

    private fun backupFile(): File = File(context.filesDir, BACKUP_FILE_NAME)

    suspend fun export(): File {
        val user = userProfileDao.get() ?: UserProfileEntity()
        val bike = bikeProfileDao.get() ?: BikeProfileEntity()
        val settings = settingsDao.get() ?: SettingsEntity()
        val widgets = dashboardWidgetDao.getAll()
        val theme = themeColorsDao.get() ?: ThemeColorsEntity()
        val appState = appStateDao.get() ?: AppStateEntity()

        val file = backupFile()
        ZipOutputStream(FileOutputStream(file)).use { zip ->
            writeJsonEntry(zip, "user.json", userToJson(user))
            writeJsonEntry(zip, "bike.json", bikeToJson(bike))
            writeJsonEntry(zip, "settings.json", settingsToJson(settings))
            writeJsonEntry(zip, "dashboard.json", widgetsToJson(widgets))
            writeJsonEntry(zip, "theme.json", themeToJson(theme))
            writeJsonEntry(zip, "app_state.json", appStateToJson(appState))
            writeJsonEntry(zip, "rides.json", ridesToJson(rideSessionDao.getAllOnce()))
            writeJsonEntry(zip, "metadata.json", metadataJson())
        }
        return file
    }

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
            entries["theme.json"]?.let { themeColorsDao.upsert(themeFromJson(JSONObject(it))) }
            entries["app_state.json"]?.let { appStateDao.upsert(appStateFromJson(JSONObject(it))) }
            // Ride history is intentionally NOT restored automatically -
            // needs de-duplication against whatever already exists locally
            // first (unchanged decision from Phase 2).

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
        put("username", e.username); put("firstName", e.firstName); put("lastName", e.lastName)
        put("email", e.email); put("passwordHash", e.passwordHash); put("passwordSalt", e.passwordSalt)
        put("age", e.age); put("heightCm", e.heightCm); put("weightKg", e.weightKg)
    }.toString()

    private fun userFromJson(o: JSONObject) = UserProfileEntity(
        0, o.optString("username"), o.optString("firstName"), o.optString("lastName"), o.optString("email"),
        o.optString("passwordHash"), o.optString("passwordSalt"),
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
        put("maxSpeedAlertKmh", e.maxSpeedAlertKmh); put("isDarkTheme", e.isDarkTheme)
        put("use24HourClock", e.use24HourClock); put("gearSuggestionsEnabled", e.gearSuggestionsEnabled)
        put("antiTheftAlarmEnabled", e.antiTheftAlarmEnabled)
        put("reminderNotificationsEnabled", e.reminderNotificationsEnabled)
        put("engineStartAnimationEnabled", e.engineStartAnimationEnabled)
    }.toString()

    private fun settingsFromJson(o: JSONObject) = SettingsEntity(
        0, o.optBoolean("useMetricUnits", true), o.optBoolean("soundEnabled", true),
        o.optInt("maxSpeedAlertKmh", 40), o.optBoolean("isDarkTheme", true),
        o.optBoolean("use24HourClock", true), o.optBoolean("gearSuggestionsEnabled", true),
        o.optBoolean("antiTheftAlarmEnabled", false), o.optBoolean("reminderNotificationsEnabled", true),
        o.optBoolean("engineStartAnimationEnabled", true)
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

    private fun themeToJson(e: ThemeColorsEntity) = JSONObject().apply {
        put("dayPrimary", e.dayPrimary); put("dayAccent", e.dayAccent); put("dayBackground", e.dayBackground)
        put("dayCardBackground", e.dayCardBackground); put("dayTextPrimary", e.dayTextPrimary)
        put("nightPrimary", e.nightPrimary); put("nightAccent", e.nightAccent); put("nightBackground", e.nightBackground)
        put("nightCardBackground", e.nightCardBackground); put("nightTextPrimary", e.nightTextPrimary)
    }.toString()

    private fun themeFromJson(o: JSONObject): ThemeColorsEntity {
        val d = ThemeColorsEntity()
        return d.copy(
            dayPrimary = o.optLong("dayPrimary", d.dayPrimary),
            dayAccent = o.optLong("dayAccent", d.dayAccent),
            dayBackground = o.optLong("dayBackground", d.dayBackground),
            dayCardBackground = o.optLong("dayCardBackground", d.dayCardBackground),
            dayTextPrimary = o.optLong("dayTextPrimary", d.dayTextPrimary),
            nightPrimary = o.optLong("nightPrimary", d.nightPrimary),
            nightAccent = o.optLong("nightAccent", d.nightAccent),
            nightBackground = o.optLong("nightBackground", d.nightBackground),
            nightCardBackground = o.optLong("nightCardBackground", d.nightCardBackground),
            nightTextPrimary = o.optLong("nightTextPrimary", d.nightTextPrimary)
        )
    }

    private fun appStateToJson(e: AppStateEntity) = JSONObject().apply {
        put("hasCompletedOnboarding", e.hasCompletedOnboarding)
        put("hasCompletedSignup", e.hasCompletedSignup)
        put("avgRideStartMinuteOfDay", e.avgRideStartMinuteOfDay)
        put("rideStartSampleCount", e.rideStartSampleCount)
    }.toString()

    private fun appStateFromJson(o: JSONObject) = AppStateEntity(
        id = 0,
        hasCompletedOnboarding = o.optBoolean("hasCompletedOnboarding", false),
        hasCompletedSignup = o.optBoolean("hasCompletedSignup", false),
        lastAppOpenEpochMs = System.currentTimeMillis(), // this device's own open time, not the old device's
        lastRideStartEpochDay = -1L,
        avgRideStartMinuteOfDay = o.optInt("avgRideStartMinuteOfDay", 480),
        rideStartSampleCount = o.optInt("rideStartSampleCount", 0)
    )

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
