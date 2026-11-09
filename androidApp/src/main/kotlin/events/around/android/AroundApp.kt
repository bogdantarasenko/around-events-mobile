package events.around.android

import android.app.Application
import events.around.shared.AppConfig
import events.around.shared.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.maplibre.android.MapLibre

class AroundApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
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
