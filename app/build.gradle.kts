plugins { id("com.android.application") }
android {
 namespace = "sk.octavia.aasolar"
 compileSdk = 36
 defaultConfig { applicationId = "sk.octavia.aasolar"; minSdk = 33; targetSdk = 36; versionCode = 2; versionName = "0.1.1" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
dependencies {
 implementation("dev.rikka.shizuku:api:13.1.5")
 implementation("dev.rikka.shizuku:provider:13.1.5")
}
