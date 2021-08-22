package com.tonyzyc.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class Login extends JFrame {

    private JLabel unameJLabel;
    private JTextField unameJTextField;
    private JButton btnJButton;
    private JButton cancelJButton;
    private int numOfPlayers;

    public Login(int numOfPlayers) {

        this.unameJLabel = new JLabel("登录名:", SwingConstants.CENTER);
        this.unameJTextField = new JTextField();
        this.btnJButton = new JButton("Login");
        this.cancelJButton = new JButton("Cancel");
        this.numOfPlayers = numOfPlayers;

        this.setLayout(new GridLayout(2, 2));
        this.add(unameJLabel);
        this.add(unameJTextField);
        this.add(btnJButton);
        this.add(cancelJButton);

        this.setSize(800, 600);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // add the listener
        LoginEvent loginEvent = new LoginEvent();
        this.btnJButton.addActionListener(loginEvent);
    }

    class LoginEvent implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // if click login, get the username
            String uname = unameJTextField.getText();
            // create a socket and connect to the server
            try {
                Socket socket = new Socket("127.0.0.1", 8888);
                // jump to the main window (game)
                new MainFrame(uname, socket, numOfPlayers);
                dispose();

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }

}
