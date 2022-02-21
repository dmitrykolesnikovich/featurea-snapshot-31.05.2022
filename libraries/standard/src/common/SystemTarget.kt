package featurea

enum class SystemTarget(val specifier: String) {
    DESKTOP("desktop"),
    STUDIO("studio"),
    ANDROID("android"),
    JVM("jvm"),
    JS("js"),
    IOS("ios")
}

expect val System.Companion.target: SystemTarget

val SystemTarget.isMobile: Boolean get() = this == SystemTarget.ANDROID || this == SystemTarget.IOS
val SystemTarget.isDesktop: Boolean get() = this == SystemTarget.DESKTOP
expect val isPhoneOs: Boolean
expect val isIphoneBrowser: Boolean
