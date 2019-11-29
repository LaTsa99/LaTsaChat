package com.latsa.chatclient.gui;

import com.latsa.chatclient.controller.MessageAcceptor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.TreeMap;

public class MainChatWindow extends JFrame {

    private Socket sock;
    private DataOutputStream dos;
    private volatile DataInputStream dis;
    private Thread accepter;
    private MessageAcceptor ma;
    private TreeMap<String, String> users;
    private String SelectedUser;
    private String state;

    private JTextField chatInput;
    private JTextArea chatScreen;
    private JTable onlineUsers;
    private JPopupMenu popup;

    private boolean admin;
    private boolean exit;

    public MainChatWindow( Socket sock, DataOutputStream dos, DataInputStream dis, boolean admin)
    {
        super("Chat Application");

        this.sock = sock;
        this.dos = dos;
        this.dis = dis;
        this.admin = admin;

        initWindow();
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
                                   @Override
                                   public void windowClosing(WindowEvent e) {
                                       exit = true;
                                       try {
                                           dos.writeUTF("disconnect");
                                       } catch (IOException ex) {
                                           ex.printStackTrace();
                                       }
                                   }
                               });
        ma = new MessageAcceptor(this, dis);
        accepter = new Thread(ma);
        accepter.start();
        users = new TreeMap<>();
        exit = false;
    }

    private void initWindow()
    {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JMenuBar menu = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem disconnect = new JMenuItem("Disconnect");
        disconnect.addActionListener(actionEvent -> {
            exit = false;
            try {
                dos.writeUTF("disconnect");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        file.add(disconnect);


        JMenuItem userMenu = new JMenuItem("Show users");
        userMenu.addActionListener(actionEvent -> showUsers());
        file.add(userMenu);
        menu.add(file);
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

    public void disconnect()
    {
        if(sock != null)
        {
            try {
                ma.stop();
                dis.close();
                dos.close();
                sock.close();
                dispose();
                if(!exit)
                    new LoginWindow();
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
        chatScreen.setCaretPosition(chatScreen.getDocument().getLength());
    }

    public void addUser(String name, String isOnline)
    {
        if(users.containsKey(name)) {
            if (!users.get(name).equals(isOnline))
                users.replace(name, isOnline);
        }else
        {
            users.put(name, isOnline);
        }
    }

    private void showUsers()
    {

        JFrame tableFrame = new JFrame();
        tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultTableModel model = new DefaultTableModel();
        onlineUsers = new JTable(model);

        model.addColumn("name");
        model.addColumn("online");

        Set<String> nameSet = users.keySet();
        for(String s : nameSet)
        {
            model.addRow(new Object[]{s, users.get(s)});
        }

        if(admin)
        {
            popup = new JPopupMenu();
            JMenuItem kickMenu = new JMenuItem("Kick user");
            JMenuItem banMenu = new JMenuItem("Ban user");

            kickMenu.addActionListener(new KickListener());
            popup.add(kickMenu);
            popup.add(banMenu);

            onlineUsers.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    super.mouseReleased(e);
                    if(e.isPopupTrigger()) {
                        JTable source = (JTable) e.getSource();
                        int row = source.rowAtPoint(e.getPoint());
                        int column = source.columnAtPoint(e.getPoint());

                        SelectedUser = (String)source.getValueAt(row, 0);
                        state = (String)source.getValueAt(row, 1);

                        if (!source.isRowSelected(row))
                            source.changeSelection(row, column, false, false);

                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
        }

        JScrollPane scroller = new JScrollPane(onlineUsers);

        tableFrame.add(scroller);
        tableFrame.pack();
        tableFrame.setVisible(true);
    }

    public void userKicked(String reply)
    {
        if (reply.equals("KICKED")) {
            JOptionPane.showMessageDialog(MainChatWindow.this, "User successfully kicked!");
        } else if (reply.equals("ADMIN")) {
            JOptionPane.showMessageDialog(MainChatWindow.this, "You can't kick an admin!", "Warning", JOptionPane.WARNING_MESSAGE);
        } else if (reply.equals("YOU")) {
            JOptionPane.showMessageDialog(MainChatWindow.this, "Why would you want to kick yourself???", "What?", JOptionPane.QUESTION_MESSAGE);
        } else if (reply.equals("REKT")){
            JOptionPane.showMessageDialog(MainChatWindow.this, "You have been kicked!", "Rekt", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class KickListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(state.equals("Online")) {
                try {
                    dos.writeUTF(String.format("kick#%s", SelectedUser));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
