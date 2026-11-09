package events.around.android

import android.app.Application
import events.around.shared.AppConfig
import events.around.shared.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.osmdroid.config.Configuration

class AroundApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // OpenStreetMap asks every app for its own User-Agent on tile requests.
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        initKoin(
            config = AppConfig(
                baseUrl = BuildConfig.BASE_URL,
                versionName = BuildConfig.VERSION_NAME,
                isDebug = BuildConfig.DEBUG,
            ),
        ) {
            androidContext(this@AroundApp)
        }
    }
}
