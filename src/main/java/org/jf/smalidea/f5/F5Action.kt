package org.jf.smalidea.f5

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.jf.smalidea.util.logi
import org.jf.smalidea.util.promptError
import java.io.File
import java.util.regex.Pattern

/**
 * TODO:
 * - only available in smali file
 * -
 */
class F5Action: AnAction() {
    companion object {
        private const val PROJECT_ROOT_PATH = "tmp/f5/"
        const val PROJECT_SOURCE_PATH = "${PROJECT_ROOT_PATH}src"
    }
    lateinit var actionRootPath: String

    override fun actionPerformed(e: AnActionEvent) {
        // get current file
        val project = e.getData(CommonDataKeys.PROJECT) as Project
        val curFile = e.getData(CommonDataKeys.VIRTUAL_FILE) as VirtualFile?
        actionRootPath = "${project.basePath}/${PROJECT_ROOT_PATH}"

        if (curFile == null) {
            promptError("You have select no file")
            return
        }
        if (!curFile.path.endsWith(".smali")) {
            promptError("You must select a smali file")
            return
        }

        val env = ActionEnv(project, curFile)
        if (env.className.isBlank()) {
            return
        }

        val initialRst = initialJavaSources(project)
        if (!initialRst) {
            println()
        }

        val dstFilePath = "${project.basePath}/${PROJECT_SOURCE_PATH}/${env.className.replace(".", "/")}.class"
        logi("After calculate, the dstFilePath is: $dstFilePath")

        if (!File(dstFilePath).exists()) {
            promptError("Can't find dstFile, may be you can check the $actionRootPath directory")
        }

        OpenFileDescriptor(project, LocalFileSystem.getInstance().findFileByIoFile(File(dstFilePath))!!).navigate(true)
    }

    private fun initialJavaSources(project: Project): Boolean {
        return if (actionRootPath.isBlank() || !File(actionRootPath).exists()) {
            realInitialJavaSource(project)
        } else {
            true
        }
    }

    private fun realInitialJavaSource(project: Project): Boolean {
        // please choice the apk path
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(".apk")
        val chosen = FileChooser.chooseFile(descriptor, project, null)
        if (chosen == null) {
            promptError("You have select nothing")
            return false
        }

        return parseTheApk(chosen.path, actionRootPath, "${project.basePath}/${PROJECT_SOURCE_PATH}")
    }

    private fun parseTheApk(apkPath: String, tmpRoot: String, sourceRoot: String): Boolean {
        return ApkFile(apkPath).extra(tmpRoot, sourceRoot)
    }


}

class ActionEnv(val project: Project, val currentFile: VirtualFile) {
    val currentFilePath = currentFile.path
    val className = extraClassNameFromPath()

    init {
    }

    private fun extraClassNameFromPath(): String {
        val regex = Pattern.compile(".*smali(_[^/]+)?/(.*).smali")
        val match = regex.matcher(currentFilePath)
        if (match.find()) {
            return match.group(2)
        } else {
            promptError("Can't extra className from smali file path: $currentFilePath")
            return ""
        }
    }

}