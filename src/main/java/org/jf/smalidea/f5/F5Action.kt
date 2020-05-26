package org.jf.smalidea.f5

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project

/**
 * TODO:
 * - only available in smali file
 * -
 */
class F5Action: AnAction() {
    companion object {
        const val KEY_F5_ROOT_PATH = "key_f5_root_path"
    }

    override fun actionPerformed(e: AnActionEvent) {
        // get current file
        val project = e.getData(CommonDataKeys.PROJECT) as Project
    }

    private fun initialJavaSources(project: Project) {
        val rootPath = PropertiesComponent.getInstance(project).getValue(KEY_F5_ROOT_PATH, "")
        if (rootPath.isBlank()) {

        }
    }

    private fun realInitialJavaSource() {
        // please choice the apk path
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(".apk")
//        FileChooser.chooseFile()
//        FileChooser.chooseFile()
    }

}