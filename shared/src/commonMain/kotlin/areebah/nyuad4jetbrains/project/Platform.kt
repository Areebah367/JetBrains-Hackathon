package areebah.nyuad4jetbrains.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform