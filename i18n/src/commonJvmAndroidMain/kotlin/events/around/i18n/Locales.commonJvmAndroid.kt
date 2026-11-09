package events.around.i18n

actual fun String.format(vararg args: Any): String =
    java.lang.String.format(this, *args)
