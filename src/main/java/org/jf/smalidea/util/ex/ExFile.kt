package org.jf.smalidea.util.ex

import java.io.File


fun File.writeBytesEx(bytes: ByteArray, overwrite: Boolean = false, autoCreateFolder: Boolean = true) {
    if (autoCreateFolder && !this.parentFile.exists()) {
        this.parentFile.mkdirs()
    }

    if (!overwrite && exists()) {
        return
    }

    writeBytes(bytes)
}

