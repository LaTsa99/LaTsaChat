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

/**
 * Main swing window of the client side application.
 */
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

    /**
     * Creates a new chat window.
     *
     * @param sock The socket, which is connected to the server.
     * @param dos The output stream channel to the server.
     * @param dis The input stream channel from the server.
     * @param admin Says, if the current client admin privileges has.
     */
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

    /**
     * Initiates the chat window.
     */
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
        chatInput = new JTextField(90);
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

    /**
     * Disconnects from the server.
     */
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

    /**
     * Checks, if there is text in the input field, and
     * sends it to the server.
     */
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

    /**
     * Displays given text on the chat area.
     *
     * @param s text to display
     */
    public void setText(String s){
        chatScreen.append(s + "\n");
        chatScreen.setCaretPosition(chatScreen.getDocument().getLength());
    }


    /**
     * Adds user to users status table, or updates their
     * status.
     *
     * @param name name of the user
     * @param isOnline current online status
     */
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

    /**
     * Creates a new window with a table in it, which contains
     * the users of the server and their online status. If the client
     * is admin, users can be kicked or banned here.
     */
    private void showUsers()
    {

        JFrame tableFrame = new JFrame();
        tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultTableModel model = new DefaultTableModel();
        onlineUsers = new JTable(model);
        onlineUsers.setEnabled(false);

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
            banMenu.addActionListener(new BanListener());
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

    /**
     * Handles messages, that are connected to kicking process.
     * If the reply equals to 'REKT', the client gets kicked from
     * the server. Else, a popup window appears about the reply
     * from the server.
     *
     * @param reply reply from the server
     */
    public void userKicked(String reply)
    {
        MainChatWindow main = MainChatWindow.this;
        if (reply.equals("KICKED")) {
            JOptionPane.showMessageDialog(main, "User successfully kicked!");
        } else if (reply.equals("ADMIN")) {
            JOptionPane.showMessageDialog(main, "You can't kick an admin!", "Warning", JOptionPane.WARNING_MESSAGE);
        } else if (reply.equals("YOU")) {
            JOptionPane.showMessageDialog(main, "Why would you want to kick yourself???", "What?", JOptionPane.QUESTION_MESSAGE);
        } else if (reply.equals("REKT")){
            JOptionPane.showMessageDialog(main, "You have been kicked!", "Rekt", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Same as userKicked, but this is about ban. If client
     * gets banned, a pop up window will appear displaying the
     * reason of the ban.
     *
     * @param reply reply from the server
     */
    public void userBanned(String reply)
    {
        MainChatWindow main = MainChatWindow.this;
        if (reply.equals("BANNED")) {
            JOptionPane.showMessageDialog(main, "User successfully banned!");
        } else if (reply.equals("ADMIN")) {
            JOptionPane.showMessageDialog(main, "You can't ban an admin!", "Warning", JOptionPane.WARNING_MESSAGE);
        } else if (reply.equals("YOU")) {
            JOptionPane.showMessageDialog(main, "Why would you want to ban yourself???", "What?", JOptionPane.QUESTION_MESSAGE);
        }else
        {
            error(String.format("You have been banned for the following reason:\n%s", reply));
            dispose();
        }
    }

    /**
     * Listens, if the client (who is an admin) wants to kick another
     * user. If so, communicates with server.
     */
    private class KickListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            int sure = JOptionPane.showConfirmDialog(MainChatWindow.this, "Are you sure?");
            if(sure == 0) {
                if (state.equals("Online")) {
                    try {
                        dos.writeUTF(String.format("kick#%s", SelectedUser));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else
                    JOptionPane.showMessageDialog(MainChatWindow.this, "User is offline.");
            }
        }
    }

    /**
     * Listens, if client (admin) wants to ban a client. If so, a pop up
     * window appears, where the client can give the reason of ban. Then
     * communicates with the server.
     */
    private class BanListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String reason = JOptionPane.showInputDialog(MainChatWindow.this, "Why do you want to ban this user?");
            int sure = JOptionPane.showConfirmDialog(MainChatWindow.this, "Are you sure?");
            if(sure == 0)
            {
                try {
                    dos.writeUTF(String.format("ban#%s#%s", SelectedUser, reason));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Creates a pop up window with the given message.
     *
     * @param issue error message
     */
    private void error(String issue)
    {
        JOptionPane.showMessageDialog(MainChatWindow.this, issue, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
