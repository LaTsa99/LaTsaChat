package com.latsa.chatclient.gui;

import javax.swing.*;

public class MainChatWindow extends JFrame {

    public MainChatWindow()
    {
        super("Chat Application");

        initWindow();
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void initWindow()
    {
        JPanel mainPanel = new JPanel();
        JLabel mainLabel = new JLabel("hello bazmeg");
        mainPanel.add(mainLabel);
        this.add(mainPanel);
        this.pack();
    }
}
