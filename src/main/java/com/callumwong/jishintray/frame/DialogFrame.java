package com.callumwong.jishintray.frame;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public abstract class DialogFrame extends JFrame {
    public DialogFrame(boolean visible) {
        super();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new MigLayout("insets 20 20 20 20", "[]", "[]"));

        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    toFront();
                    requestFocus();
                });
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

        createUI();
        pack();

        setTitle("About");
        setLocationRelativeTo(null);
        setVisible(visible);
    }

    protected abstract void createUI();
}
