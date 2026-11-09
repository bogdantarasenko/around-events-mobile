# kotlinx.serialization keeps generated serializers through its own consumer rules.
# Ktor uses SLF4J optionally; it is not on the classpath.
-dontwarn org.slf4j.**
