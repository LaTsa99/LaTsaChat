package com.latsa.chatclient.gui;

import com.latsa.chatclient.controller.MessageAcceptor;

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

public class MainChatWindow extends JFrame {

    private Socket sock;
    private DataOutputStream dos;
    private DataInputStream dis;
    private Thread accepter;
    private MessageAcceptor ma;

    private JTextField chatInput;
    private JTextArea chatScreen;


    public MainChatWindow( Socket sock, DataOutputStream dos, DataInputStream dis)
    {
        super("Chat Application");

        this.sock = sock;
        this.dos = dos;
        this.dis = dis;

        initWindow();
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
                                   @Override
                                   public void windowClosing(WindowEvent e) {
                                       super.windowClosing(e);
                                       disconnect();
                                   }
                               });
        ma = new MessageAcceptor(this, dis);
        accepter = new Thread(ma);
        accepter.start();

    }

    private void initWindow()
    {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JMenuBar menu = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem disconnect = new JMenuItem("Disconnect");
        disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                disconnect();
                dispose();
                new LoginWindow();
            }
        });
        file.add(disconnect);
        menu.add(file);
        JMenu showUsers = new JMenu("Show users");
        menu.add(showUsers);
        this.setJMenuBar(menu);

        JPanel chatPanel = new JPanel();
        chatScreen = new JTextArea();
        chatScreen.setColumns(100);
        chatScreen.setRows(30);
        chatScreen.setEnabled(false);
        chatScreen.setDisabledTextColor(Color.BLACK);
        JScrollPane chatScroll = new JScrollPane(chatScreen);
        chatScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatScroll.setAutoscrolls(true);
        chatPanel.add(chatScroll);

        JPanel inputPanel = new JPanel();
        chatInput = new JTextField(80);
        chatInput.addActionListener(inputListener);
        inputPanel.add(chatInput);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(inputListener);
        inputPanel.add(sendButton);

        mainPanel.add(chatPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        this.add(mainPanel);
        this.pack();
    }

    private void disconnect()
    {
        if(sock != null)
        {
            try {

                dos.writeUTF("disconnect");
                ma.stop();
                String reply = dis.readUTF();
                if(reply.equals("OK"))
                {
                    ma.stop();
                    dis.close();
                    dos.close();
                    sock.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ActionListener inputListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String input = chatInput.getText();
            if(!input.equals("")) {
                try {
                    dos.writeUTF(String.format("msg#%s", input));
                    chatInput.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void setText(String s){
        chatScreen.append(s + "\n");
    }
}
