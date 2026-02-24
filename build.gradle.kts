// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    id("com.github.jk1.dependency-license-report") version "3.0.1"
    id("project-report")
}

licenseReport {
    // Не используем buildDir (deprecated property), явно указываем путь в каталоге проекта.
    outputDir = "$projectDir/build/reports/licenses"
    // Оставляем только JSON‑отчёт, чтобы не зависеть от HtmlReportRenderer.
    renderers = arrayOf(
        com.github.jk1.license.render.JsonReportRenderer("licenses.json")
    )
    projects = arrayOf(project(":app"))
}