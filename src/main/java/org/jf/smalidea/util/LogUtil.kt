package org.jf.smalidea.util

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

private val log = Logger.getInstance("f5")

fun logi(msg: String, project: Project? = null) {
    log.debug(msg)
}

fun promptError(msg: String, project: Project? = null) {
    Notifications.Bus.notify(Notification("Smali-F5", "", msg, NotificationType.ERROR), project)
    print(msg)
}

fun promptInfo(msg: String, project: Project? = null) {
    Notifications.Bus.notify(Notification("Smali-F5", "", msg, NotificationType.INFORMATION), project)
}
