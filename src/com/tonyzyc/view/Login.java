package com.tonyzyc.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class Login extends JFrame {

    private final JLabel unameJLabel;
    private final JTextField unameJTextField;
    private final JButton btnJButton;
    private final JButton cancelJButton;
    private final JLabel numOfPlayersJLabel;
    private final JTextField numOfPlayersJTextField;

    public Login() {

        this.unameJLabel = new JLabel("登录名:", SwingConstants.CENTER);
        this.unameJTextField = new JTextField();
        this.btnJButton = new JButton("Login");
        this.cancelJButton = new JButton("Cancel");
        this.numOfPlayersJLabel = new JLabel("游戏人数", SwingConstants.CENTER);
        this.numOfPlayersJTextField = new JTextField();

        this.setLayout(new GridLayout(3, 2));
        this.add(unameJLabel);
        this.add(unameJTextField);
        this.add(numOfPlayersJLabel);
        this.add(numOfPlayersJTextField);
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
            int numOfPlayers = 0;
            try {
                numOfPlayers = Integer.parseInt(numOfPlayersJTextField.getText());
                if (numOfPlayers % 2 != 0) {
                    JOptionPane.showMessageDialog(null, "游戏人数必须为偶数!");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "请输入游戏数字");
                return;
            }
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
