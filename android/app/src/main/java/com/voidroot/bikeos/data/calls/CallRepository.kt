package com.voidroot.bikeos.data.calls

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class IncomingCall(val contactName: String?, val phoneNumber: String)

/**
 * Watches for a ringing call and exposes it as [incomingCall] for the
 * Calls dashboard widget. Answer/reject are driven by the handlebar's
 * Gear Up/Gear Down buttons (see DashboardViewModel.onDeviceButtonEvent) -
 * this class doesn't know about BLE at all, it's purely "read call state,
 * offer answer()/reject()".
 *
 * Needs READ_PHONE_STATE (call state), READ_CONTACTS (name lookup - shown
 * per the spec's ask for the contact's name, not just a number), and
 * ANSWER_PHONE_CALLS (API 26+, to actually answer/reject). All three are
 * runtime-dangerous permissions - [startListening] no-ops if they aren't
 * granted rather than crashing; the caller (DashboardScreen) is
 * responsible for requesting them first.
 */
@Singleton
class CallRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager

    private val _incomingCall = MutableStateFlow<IncomingCall?>(null)
    val incomingCall: StateFlow<IncomingCall?> = _incomingCall.asStateFlow()

    private var isListening = false
    private var legacyListener: PhoneStateListener? = null
    private var modernCallback: TelephonyCallback? = null

    val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.ANSWER_PHONE_CALLS)
        } else {
            arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS)
        }

    fun hasRequiredPermissions(): Boolean =
        requiredPermissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

    @SuppressLint("MissingPermission")
    fun startListening() {
        if (isListening || !hasRequiredPermissions() || telephonyManager == null) return
        isListening = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    handleCallState(state, null)
                }
            }
            modernCallback = callback
            telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
        } else {
            @Suppress("DEPRECATION")
            val listener = object : PhoneStateListener() {
                @Suppress("DEPRECATION")
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    handleCallState(state, phoneNumber)
                }
            }
            legacyListener = listener
            @Suppress("DEPRECATION")
            telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopListening() {
        if (!isListening) return
        isListening = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            modernCallback?.let { telephonyManager?.unregisterTelephonyCallback(it) }
            modernCallback = null
        } else {
            @Suppress("DEPRECATION")
            legacyListener?.let { telephonyManager?.listen(it, PhoneStateListener.LISTEN_NONE) }
            legacyListener = null
        }
        _incomingCall.value = null
    }

    private fun handleCallState(state: Int, phoneNumber: String?) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                val number = phoneNumber ?: "Unknown"
                _incomingCall.value = IncomingCall(contactName = lookupContactName(number), phoneNumber = number)
            }
            else -> _incomingCall.value = null // IDLE or OFFHOOK - call answered/ended/rejected elsewhere
        }
    }

    @SuppressLint("MissingPermission")
    private fun lookupContactName(phoneNumber: String): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                }
            }
        return null
    }

    @SuppressLint("MissingPermission")
    fun answerCall() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            telecomManager?.acceptRingingCall()
        }
        // Pre-API26: no non-system API to accept a call programmatically -
        // the rider still needs to tap the phone in that case. Acceptable
        // gap given minSdk 29 means this path is effectively unreachable
        // for real installs anyway.
    }

    @SuppressLint("MissingPermission")
    fun rejectCall() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            telecomManager?.endCall()
        }
        _incomingCall.value = null
    }
}
