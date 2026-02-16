plugins {
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.realm).apply(false)
    alias(libs.plugins.skie) apply false
    alias(libs.plugins.nmcp.aggregation)
}

nmcpAggregation {
    centralPortal {
        username = getPropertyValue(project, "ossrhUsername")
        password = getPropertyValue(project, "ossrhPassword")

        // `publishingType` can be replaced w/ USER_MANAGED to validate on the web.
        publishingType = "AUTOMATIC" // This is the default, but we surface it.
    }
}

dependencies {
    nmcpAggregation(project(":STCommon"))
    nmcpAggregation(project(":STCore"))
    nmcpAggregation(project(":STDatabase"))
    nmcpAggregation(project(":STNetwork"))
}

private fun getPropertyValue(project: Project, propertyName: String): String? {
    if (project.hasProperty(propertyName)) return project.property(propertyName) as String

    val systemValue: String? = System.getenv(propertyName)
    return systemValue
}
