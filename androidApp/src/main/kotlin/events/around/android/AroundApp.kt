package events.around.android

import android.app.Application
import events.around.shared.AppConfig
import events.around.shared.di.initKoin
import org.koin.android.ext.koin.androidContext

class AroundApp : Application() {
    override fun onCreate() {
        super.onCreate()
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
