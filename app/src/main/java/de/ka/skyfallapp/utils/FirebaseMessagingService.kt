package de.ka.skyfallapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.ka.skyfallapp.R
import de.ka.skyfallapp.repo.Repository
import de.ka.skyfallapp.repo.api.models.PushTokenBody
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import io.reactivex.disposables.CompositeDisposable
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class FirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    val compositeDisposable = CompositeDisposable()
    val repository: Repository by inject()

    override fun onCreate() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_consensus)
            val descriptionText = getString(R.string.notification_channel_consensus_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_CONSENSUS_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Called when a firebase message has been received.
     *
     * @param remoteMessage the object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Timber.e("Firebase message data ${remoteMessage?.data?.toString()}")
        Timber.e("Firebase message Notification: ${remoteMessage?.notification?.toString()}")

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, ShareUtils.buildConsensusShareIntent("-1"), 0)

        remoteMessage?.notification?.let {
            val builder = NotificationCompat.Builder(this, CHANNEL_CONSENSUS_ID)
                .setSmallIcon(R.drawable.ic_small_public)
                .setContentTitle(it.title)
                .setContentText(it.body)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                notify(CONSENSUS_ID, builder.build())
            }
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun onNewToken(token: String?) {
        repository.profileManager.updateProfile { pushToken = token }

        if (!token.isNullOrEmpty() && !repository.profileManager.isPushTokenConfirmed(token)) {
            Timber.e("Registering a refreshed Firebase token: $token")
            repository.registerPushToken(PushTokenBody(token))
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion { }
                .start(compositeDisposable)
        }
    }

    companion object {

        const val CHANNEL_CONSENSUS_ID = "1"
        const val CONSENSUS_ID = 1
    }
}