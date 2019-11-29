package com.latsa.chatclient.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class LoginWindow extends JFrame {

    private JPanel mainPanel;
    private JTextField IPField;
    private JTextField PortField;
    private JTextField UsernameField;
    private JPasswordField PasswordField;
    private JButton ConnectButton;
    private JButton LoginButton;
    private JButton RegisterButton;

    private String ipAddress;
    private int portNumber;

    private Socket sock;
    private DataInputStream dis;
    private DataOutputStream dos;

    public LoginWindow()
    {
        super("Login");
        initWindow();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(320, 250);
        this.setResizable(false);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                disconnect();
            }
        });
    }

    private void initWindow()
    {
        GridLayout gl = new GridLayout(3, 1);
        mainPanel = new JPanel(gl);

        JPanel ipPanel = new JPanel();
        JPanel portPanel = new JPanel();
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

        this.add(mainPanel);
        this.pack();
    }

    private void loadLoginScreen()
    {
        mainPanel.removeAll();
        mainPanel.setLayout(new GridLayout(4, 1));

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
        UsernameField.requestFocusInWindow();
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



        this.add(mainPanel);
        this.pack();
    }

    ActionListener al = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            ipAddress = IPField.getText();
            portNumber = Integer.parseInt(PortField.getText());
            sock = null;
            try {
                sock = new Socket(ipAddress, portNumber);
                dos = new DataOutputStream(sock.getOutputStream());
                dis = new DataInputStream(sock.getInputStream());
                loadLoginScreen();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(LoginWindow.this, "No server on this ip and port!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(LoginWindow.this, "This isn't a port number!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ie)
            {
                JOptionPane.showMessageDialog(LoginWindow.this, "Port out of range!", "Error", JOptionPane.ERROR_MESSAGE);
            }


        }};

    ActionListener loginListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String username = UsernameField.getText();
            char[] password = PasswordField.getPassword();
            try {


                String pass = String.valueOf(password);
                dos.writeUTF(String.format("login#%s#%s", username, pass));

                String reply = dis.readUTF();
                if (reply.equals("OK")) {
                    dispose();
                    new MainChatWindow(sock, dos, dis, false);
                } else if (reply.equals("OK_ADMIN"))
                {
                    dispose();
                    new MainChatWindow(sock, dos, dis, true);
                }else
                {
                    JOptionPane.showMessageDialog(LoginWindow.this, "Wrong credentials!", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(LoginWindow.this, "Connection with the server lost!", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        }
    };

    ActionListener registerListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String reply;
            try {
                String pass = String.valueOf(PasswordField.getPassword());
                dos.writeUTF(String.format("register#%s#%s", UsernameField.getText(), pass));
                if((reply = dis.readUTF()).equals("OK")){
                    JOptionPane.showMessageDialog(LoginWindow.this, "Registration successfull!");
                }else if(reply.contains("Username already exists!"))
                {
                    JOptionPane.showMessageDialog(LoginWindow.this, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                }else
                {
                    JOptionPane.showMessageDialog(LoginWindow.this, "An error occured during registration!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(LoginWindow.this, "Connection with the server lost!", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
            }

        }
    };

    private void disconnect()
    {
        if(sock != null)
        {
            try {

                dos.writeUTF("disconnect");
                String reply = dis.readUTF();
                if(reply.equals("OK"))
                {
                    dis.close();
                    dos.close();
                    sock.close();
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(LoginWindow.this, "Connection with the server lost!", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        }
    }
}
