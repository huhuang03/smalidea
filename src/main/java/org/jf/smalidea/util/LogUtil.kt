package org.jf.smalidea.util

import com.intellij.openapi.diagnostic.Logger

private val log = Logger.getInstance("f5")

fun logi(msg: String) {
    log.debug(msg)
}

fun prompt(msg: String) {
    logi(msg)
    
    println(msg)
}