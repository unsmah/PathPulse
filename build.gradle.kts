// Root build script: plugin versions live in gradle/libs.versions.toml and are applied
// per-module. Nothing else is configured here on purpose — the app module owns all config.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}