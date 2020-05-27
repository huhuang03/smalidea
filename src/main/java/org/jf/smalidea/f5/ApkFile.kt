package org.jf.smalidea.f5

import org.jf.smalidea.util.ex.writeBytesEx
import org.jf.smalidea.util.logi
import org.jf.smalidea.util.prompt
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

class ApkFile(private val path: String) {

    /**
     * @param tmpRoot extra root
     * @param sourceRoot extra source root
     */
    fun extra(tmpRoot: String, sourceRoot: String): Boolean {
        logi("parse $path to $tmpRoot, src to $sourceRoot")
        if (!File(path).exists()) {
            prompt("$path not exist")
            return false
        }


        val zip = ZipInputStream(FileInputStream(path))
        var ze = zip.nextEntry
        while (ze != null) {
            if (ze.name.endsWith(".dex")) {
                val outputFile = File(tmpRoot, ze.name)
                outputFile.writeBytesEx(zip.readBytes())
                val jarFile = dex2Jar(outputFile)
                unzipJarFile(jarFile, sourceRoot)
            }
            ze = zip.nextEntry
        }
        return false
    }

    private fun dex2Jar(input: File): File {
        val jarFile = File(input.parent, input.nameWithoutExtension + "-dex2jar.jar")
        println(jarFile)
        if (jarFile.exists()) {
            return jarFile
        }

        val runtime = Runtime.getRuntime()
        runtime.exec("d2j-dex2jar.sh ${input.absolutePath}", null, input.parentFile).waitFor()
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