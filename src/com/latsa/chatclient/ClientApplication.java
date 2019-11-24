package com.latsa.chatclient;

import com.latsa.chatclient.gui.LoginWindow;
import com.latsa.chatclient.gui.MainChatWindow;
import com.sun.tools.javac.Main;

import java.awt.*;

import static com.latsa.chatclient.ChatStates.Login;
import static com.latsa.chatclient.ChatStates.MainWindow;
import static com.latsa.chatclient.ChatStates.Exit;

public class ClientApplication {

    private static ClientApplication single_instance = null;

    private static ChatStates state;

    private ClientApplication()
    {

    }

    public static ClientApplication getInstance()
    {
        return single_instance;
    }

    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginWindow lw = new LoginWindow();
            }
        });
    }

    public static void setState(ChatStates new_state) {
        state = new_state;
    }

    public static ChatStates getState(){
        return state;
    }
}
