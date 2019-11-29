package com.latsa.chatclient.controller;

import com.latsa.chatclient.gui.MainChatWindow;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.IOException;

public class MessageAcceptor implements Runnable {

    private MainChatWindow window;
    private DataInputStream dis;
    private boolean stop;

    public MessageAcceptor(MainChatWindow window, DataInputStream dis)
    {
        this.window = window;
        this.dis = dis;
        stop = false;
    }

    @Override
    public void run() {
        while(!stop)
        {
            try {
                String msg = dis.readUTF();
                if(msg.contains("#"))
                {
                    String[] data = msg.split("#");
                    if(data[0].equals("user"))
                        window.addUser(data[1], data[2]);
                    else if(data[0].equals("kick"))
                        window.userKicked(data[1]);
                    else
                        window.setText(msg);
                } else if(msg.equals("disconnect"))
                {
                    window.disconnect();
                }
                else
                    window.setText(msg);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(window, "Connection to server lost!", "Fatal", JOptionPane.ERROR_MESSAGE);
                window.dispose();
                stop = true;
            }
        }
    }

    public void stop()
    {
        stop = true;
    }

}
