package de.ka.rgreed.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import de.ka.rgreed.R
import de.ka.rgreed.repo.Repository
import de.ka.rgreed.repo.api.models.PushTokenBody
import de.ka.rgreed.repo.subscribeRepoCompletion
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class FirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    @Keep
    data class Notification(
        val type: String,
        val consensusId: String = "-1",
        val consensusTitle: String = "",
        val consensusDescription: String = ""
    )

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
                enableLights(true)
                lightColor = Color.BLUE
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

        if (repository.profileManager.currentProfile.username == null
            || !repository.profileManager.currentProfile.isPushEnabled
        ) {
            Timber.e("Firebase message dropped: Push Notifications are disabled or not logged in.")
            return
        }

        val notification =
            try {
                Gson().fromJson<Notification>(JSONObject(remoteMessage?.data).toString(), Notification::class.java)
            } catch (exception: JsonSyntaxException) {
                Timber.e(exception, "Could not parse notification json")
                return
            }

        Timber.e("Parsed notification: $notification")

        notification?.let {

            var title = it.consensusTitle
            var description = it.consensusDescription

            when (it.type) {
                "CONSENSUS_FINISHED" -> {
                    title = applicationContext.getString(R.string.notification_consensus_finished_title)
                    description = String.format(
                        applicationContext.getString(R.string.notification_consensus_finished_description),
                        it.consensusTitle
                    )
                }
                "CONSENSUS_VOTING_START_DATE_REACHED" -> {
                    title = applicationContext.getString(R.string.notification_consensus_votingstart_title)
                    description = String.format(
                        applicationContext.getString(R.string.notification_consensus_votingstart_description),
                        it.consensusTitle
                    )
                }
            }

            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(this, 0, ShareUtils.buildConsensusViewIntent(it.consensusId), 0)

            val builder = NotificationCompat.Builder(this, CHANNEL_CONSENSUS_ID)
                .setSmallIcon(R.drawable.ic_finished)
                .setContentTitle(title)
                .setContentText(description)
                .setAutoCancel(true)
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