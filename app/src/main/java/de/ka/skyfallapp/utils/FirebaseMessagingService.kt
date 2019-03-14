package de.ka.skyfallapp.utils

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class FirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        Timber.d("From: ${remoteMessage?.from}")

        remoteMessage?.data?.isNotEmpty()?.let {
            Timber.e("Message data payload: %s", remoteMessage.data)

        }

        remoteMessage?.notification?.let {
            Timber.e("Message Notification Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String?) {
        Timber.e("Refreshed token: $token")
    }

    companion object {
        fun generateToken() {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Timber.e("getInstanceId failed ${task.exception}")
                        return@OnCompleteListener
                    }

                    val token = task.result?.token

                    // Log
                    Timber.e("Firebase token: $token")

                    // store
                })
        }
    }
}