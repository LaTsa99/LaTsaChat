package com.latsa.chatclient;

import com.latsa.chatclient.gui.LoginWindow;

import java.awt.*;

/**
 * A singleton class, which is the main class of the program.
 */
public class ClientApplication {

    private static ClientApplication single_instance = null;


    private ClientApplication() {

    }

    /**
     * @return singleton instance of class
     */
    public static ClientApplication getInstance() {
        return single_instance;
    }

    /**
     * Entry point of the program. Creates a new login window.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginWindow lw = new LoginWindow();
            }
        });
    }
}
