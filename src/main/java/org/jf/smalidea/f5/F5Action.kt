package org.jf.smalidea.f5

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Consumer
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.channels.consumesAll
import org.jf.smalidea.util.logi
import org.jf.smalidea.util.promptError
import java.io.File
import java.util.regex.Pattern

/**
 * TODO:
 * - only available in smali file
 * - work even not select the file. but the current file
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
        actionRootPath = "${project.basePath}/${PROJECT_ROOT_PATH}"

        val fem = FileEditorManager.getInstance(project)
        val curFile = fem.selectedFiles.firstOrNull()

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

        initialJavaSources(project).map {initialRst ->
            if (initialRst) {
                var dstFilePath = "${project.basePath}/${PROJECT_SOURCE_PATH}/${env.className.replace(".", "/")}.class"
                logi("After calculate, the dstFilePath is: $dstFilePath")

                if (!File(dstFilePath).exists()) {
                    // fuck we need(in this plugin?) handle the rename className
                    val sourceFile = File(curFile.path)

                    val firstLine = sourceFile.useLines { it.firstOrNull() }?: ""
                    val pattern = Pattern.compile("#\\s*([^\\s]*)\\s*$")
                    val match = pattern.matcher(firstLine)
                    if (match.find()) {
                        val realClassName = match.group(1)
                        val className = env.className.substring(0, env.className.lastIndexOf(".")) + "." + realClassName
                        dstFilePath = "${project.basePath}/${PROJECT_SOURCE_PATH}/${className.replace(".", "/")}.class"
                    }
                }
                if (!File(dstFilePath).exists()) {
                    promptError("Can't find dstFile ${dstFilePath}, may be you can check the $actionRootPath directory")
                    return@map initialRst
                }
                OpenFileDescriptor(project, LocalFileSystem.getInstance().findFileByIoFile(File(dstFilePath))!!).navigate(true)
            }
            initialRst
        }.subscribe()
    }

    private fun initialJavaSources(project: Project): Observable<Boolean> {
        return if (actionRootPath.isBlank() || !File(actionRootPath).exists()) {
            realInitialJavaSource(project)
        } else {
            Observable.just(true)
        }
    }

    private fun realInitialJavaSource(project: Project): Observable<Boolean> {
        // please choice the apk path
        return Observable.create {emitter ->
            val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(".apk")
            FileChooser.chooseFile(descriptor, project, null) {
                if (it == null) {
                    promptError("You have select no apk file")
                    emitter.onNext(false)
                    emitter.onComplete()
                } else {
                    parseTheApk(it.path, actionRootPath, "${project.basePath}/${PROJECT_SOURCE_PATH}")
                    emitter.onNext(true)
                    emitter.onComplete()
                }
            }
        }
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