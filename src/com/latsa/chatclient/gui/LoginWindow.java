package com.latsa.chatclient.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginWindow extends JFrame {

    private JPanel mainPanel;
    private JTextField IPField;
    private JTextField PortField;
    private JTextField UsernameField;
    private JPasswordField PasswordField;
    private JLabel ErrorLabel;
    private JButton ConnectButton;
    private JButton LoginButton;
    private JButton RegisterButton;

    private String ipAddress;
    private int portNumber;


    public LoginWindow()
    {
        super("Login");
        initWindow();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(320, 250);
        this.setResizable(false);
    }

    private void initWindow()
    {
        GridLayout gl = new GridLayout(4, 1);
        mainPanel = new JPanel(gl);

        JPanel ipPanel = new JPanel();
        JPanel portPanel = new JPanel();
        JPanel errorPanel = new JPanel();
        JPanel buttonPanel = new JPanel();

        JLabel ipLabel = new JLabel("Enter IP Address:");
        IPField = new JTextField(20);
        IPField.addActionListener(al);
        ipPanel.add(ipLabel);
        ipPanel.add(IPField);
        mainPanel.add(ipPanel);

        JLabel portLabel = new JLabel("Enter Port number:");
        PortField = new JTextField(20);
        PortField.addActionListener(al);
        portPanel.add(portLabel);
        portPanel.add(PortField);
        mainPanel.add(portPanel);

        ConnectButton = new JButton("Connect");
        ConnectButton.addActionListener(al);
        buttonPanel.add(ConnectButton);
        mainPanel.add(buttonPanel);

        ErrorLabel = new JLabel("");
        ErrorLabel.setForeground(new Color(255, 0, 0));
        errorPanel.add(ErrorLabel);
        mainPanel.add(errorPanel);



        this.add(mainPanel);
        this.pack();
    }

    private void loadLoginScreen()
    {
        mainPanel.removeAll();
        mainPanel.setLayout(new GridLayout(5, 1));

        JPanel infoPanel = new JPanel();
        JPanel loginPanel = new JPanel();
        JPanel passwordPanel = new JPanel();
        JPanel errorPanel = new JPanel();
        JPanel buttonPanel = new JPanel();

        JLabel infoLabel = new JLabel(String.format("Connecting to %s:%d", ipAddress, portNumber));
        infoPanel.add(infoLabel);
        mainPanel.add(infoPanel);

        JLabel ipLabel = new JLabel("Username:");
        UsernameField = new JTextField(20);
        UsernameField.addActionListener(loginListener);
        loginPanel.add(ipLabel);
        loginPanel.add(UsernameField);
        mainPanel.add(loginPanel);

        JLabel portLabel = new JLabel("Password:");
        PasswordField = new JPasswordField(20);
        PasswordField.addActionListener(loginListener);
        passwordPanel.add(portLabel);
        passwordPanel.add(PasswordField);
        mainPanel.add(passwordPanel);

        LoginButton = new JButton("Login");
        RegisterButton = new JButton("Register");
        LoginButton.addActionListener(loginListener);
        RegisterButton.addActionListener(registerListener);
        buttonPanel.add(LoginButton);
        buttonPanel.add(RegisterButton);
        mainPanel.add(buttonPanel);

        ErrorLabel = new JLabel("");
        ErrorLabel.setForeground(new Color(255, 0, 0));
        errorPanel.add(ErrorLabel);
        mainPanel.add(errorPanel);



        this.add(mainPanel);
        this.pack();
    }

    ActionListener al = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            ipAddress = IPField.getText();
            portNumber = Integer.parseInt(PortField.getText());
            loadLoginScreen();
        }
    };

    ActionListener loginListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            ErrorLabel.setText("Logged in");
            dispose();
            new MainChatWindow();
        }
    };

    ActionListener registerListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            ErrorLabel.setText("Registered");
        }
    };
}
