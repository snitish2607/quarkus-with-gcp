package org.nirp.config

import org.eclipse.microprofile.config.ConfigProvider

object LoggingConfig {

    val LOG_NAME: String = ConfigProvider.getConfig()
        .getValue("quarkus.google.cloud.logging.default-log", String::class.java)
}