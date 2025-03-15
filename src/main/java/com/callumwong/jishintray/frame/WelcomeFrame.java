package com.callumwong.jishintray.frame;

import javax.swing.*;

public class WelcomeFrame extends DialogFrame {
    public WelcomeFrame() {
        super(true, "Welcome");
    }

    @Override
    protected void createUI() {
        add(new JLabel("""
                <html>
                <p>It seems like this is your first launch, so:</p>
                <h2>Thank you for installing JishinTray! &#60;3</h2>
                <p>This app lives in the system tray.</p>
                <p>To configure or exit the app, click on the tray icon.</p>
                <p>To view this dialog again, delete the configuration file.</p>
                </html>
                """), "wrap");

        addCloseButton();
    }
}
