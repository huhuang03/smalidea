package org.jf.smalidea.f5

import org.jf.smalidea.util.ex.writeBytesEx
import org.jf.smalidea.util.logi
import org.jf.smalidea.util.promptError
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.zip.ZipInputStream

class ApkFile(private val path: String) {

    /**
     * @param tmpRoot extra root
     * @param sourceRoot extra source root
     */
    fun extra(tmpRoot: String, sourceRoot: String): Boolean {
        // How can I do this in background??

        logi("parse $path to $tmpRoot, src to $sourceRoot")
        if (!File(path).exists()) {
            promptError("$path not exist")
            return false
        }


        val zip = ZipInputStream(FileInputStream(path))
        var ze = zip.nextEntry
        while (ze != null) {
            if (ze.name.endsWith(".dex")) {
                try {
                    val dexFile = File(tmpRoot, ze.name)
                    dexFile.writeBytesEx(zip.readBytes())
                    val jarFile = dex2Jar(dexFile)
                    if (jarFile != null) {
                        unzipJarFile(jarFile, sourceRoot)
                    } else {
                        return false
                    }
                } catch (e: Throwable) {
                    promptError("extra ${ze.name} failed")
                    e.printStackTrace()
                    return false
                }
            }
            ze = zip.nextEntry
        }
        return false
    }

    private fun dex2Jar(input: File): File? {
        val jarFile = File(input.parent, input.nameWithoutExtension + "-dex2jar.jar")
        println("being dex2jar: " + input.absolutePath + " to file " + jarFile.absolutePath)
        if (jarFile.exists()) {
            return jarFile
        }

        val runtime = Runtime.getRuntime()
        try {
            runtime.exec("d2j-dex2jar.sh ${input.absolutePath}", null, input.parentFile).waitFor()
        } catch (e: IOException) {
            promptError("Can't run d2j-dex2jar.sh, maybe you can check your path")
            return null
        }
        return jarFile
    }

    private fun unzipJarFile(input: File, outputRoot: String) {
        val zip = ZipInputStream(FileInputStream(input))
        var ze = zip.nextEntry
        while (ze != null) {
            if (!ze.isDirectory) {
                val outputFile = File(outputRoot, ze.name)
                outputFile.writeBytesEx(zip.readBytes())
            }
            ze = zip.nextEntry
        }
    }
}