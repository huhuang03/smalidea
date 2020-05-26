package org.jf.smalidea.thTest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class FirstAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        System.out.println("The action performed");
        Messages.showMessageDialog("Hello intellij action, changed", "Info", null);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

}
