package de.ka.skyfallapp.utils

import android.app.PendingIntent
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

    /**
     * Called when a firebase message has been received.
     *
     * @param remoteMessage the object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Timber.d("From: ${remoteMessage?.from}")

        remoteMessage?.data?.isNotEmpty()?.let {
            Timber.e("Firebase message data payload: %s", remoteMessage.data)
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(this, 0, ShareUtils.buildConsensusShareIntent("1"), 0)

            val builder = NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My notification")
                .setContentText("Much longer text that cannot fit one line...")
                .setContentIntent(pendingIntent)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line...")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                // notificationId is a unique int for each notification that you must define
                notify(12, builder.build())
            }
        }

        remoteMessage?.notification?.let {
            Timber.e("Firebase message Notification Body: ${it.body}")
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun onNewToken(token: String?) {
        if (token.isNullOrEmpty()) {
            return
        }

        Timber.e("Registering a refreshed Firebase token: $token")
        repository.registerPushToken(PushTokenBody(token))
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { Timber.e("Token registered: $it") }
            .start(compositeDisposable)
    }
}