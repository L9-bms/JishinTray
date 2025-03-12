package com.callumwong.jishintray.frame;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public abstract class DialogFrame extends JFrame {
    public DialogFrame(boolean visible, String title) {
        super();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new MigLayout("insets 20 20 20 20", "[]", "[]"));

        createUI();
        pack();

        setTitle(title);
        setLocationRelativeTo(null);
        setAutoRequestFocus(true);
        setVisible(visible);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);

        if (b) {
            // request focus
            SwingUtilities.invokeLater(() -> {
                setAlwaysOnTop(true);
                setAlwaysOnTop(false);
            });
        }
    }

    protected abstract void createUI();
}
