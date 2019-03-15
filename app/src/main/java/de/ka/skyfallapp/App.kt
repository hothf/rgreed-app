package de.ka.skyfallapp

import android.app.Application
import de.ka.skyfallapp.utils.FirebaseMessagingService
import org.koin.android.ext.android.startKoin
import timber.log.Timber

/**
 * App creation point. Please keep as simple as possible - keep an eye on memory leaks: please
 * do not access the app context through a singleton here;
 * The application context is already available for all viewModels extending
 * the [de.ka.skyfallapp.base.BaseViewModel].
 **/
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // start injecting with koin
        startKoin(this, listOf(appModule))

        // debug logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // generate firebase messaging token
        // FirebaseMessagingService.generateToken()
    }

}