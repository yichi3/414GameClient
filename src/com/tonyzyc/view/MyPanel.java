package com.tonyzyc.view;

import javax.swing.*;
import java.awt.*;

public class MyPanel extends JPanel {

    public MyPanel() {
        // if setLocation() or setBounds(), need to setLayout = null
        this.setLayout(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Image image = new ImageIcon("images/bg.jpg").getImage();
        g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
    }
}
