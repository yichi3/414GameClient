package com.tonyzyc.view;

import com.alibaba.fastjson.JSON;
import com.tonyzyc.model.Message;
import com.tonyzyc.model.Player;
import com.tonyzyc.model.Poker;
import com.tonyzyc.model.PokerLabel;
import com.tonyzyc.thread.ChuPaiCountThread;
import com.tonyzyc.thread.ReceiveThread;
import com.tonyzyc.thread.SendThread;
import com.tonyzyc.util.GameUtil;
import com.tonyzyc.util.PokerRule;
import com.tonyzyc.util.PokerType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainFrame extends JFrame {
    public MyPanel myPanel;
    public String uname;
    public Socket socket;
    public SendThread sendThread;
    public ReceiveThread receiveThread;
    public int numOfPlayers;
    public Player currentPlayer;
    // ready label before each game
    public JButton readyJButton;
    public boolean isReady;
    public JButton chuPaiJButton;
    public JButton buChuJButton;
    public JButton chaJButton;
    public JButton gouJButton;
    public JLabel timeLabel;
    public JLabel msgLabel;
    // store the current poker label list
    public List<PokerLabel> pokerLabels = new ArrayList<>();

    public boolean needSendGong;
    public JLabel sendGongJLabel;
    public boolean needReceiveGone;
    // counter thread, used to countdown for time
    public ChuPaiCountThread chuPaiCountThread = new ChuPaiCountThread(100, this);
    // store the selected pokerLabels
    public List<PokerLabel> selectedPokerLabels = new ArrayList<>();
    // 显示当前出牌list
    public List<PokerLabel> showOutPokerLabels = new ArrayList<>();
    // check if the current player 是否出牌
    public boolean isOut;
    // check if current player 正在叉
    public boolean isCha;
    // last player who 出牌
    public int prevPlayerId = -1;

    public MainFrame(String uname, Socket socket, int numOfPlayers) {
        System.out.println("this is user " + uname);
        this.numOfPlayers = numOfPlayers;
        this.uname = uname;
        this.socket = socket;
        // set the window attribute
        this.setSize(1200, 700);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // add the panel
        myPanel = new MyPanel();
        myPanel.setBounds(0, 0, 1200, 700);
        this.add(myPanel);

        init();

        // use a thread to send message
        sendThread = new SendThread(socket, null);
        sendThread.start();

        // use another thread to receive message
        receiveThread = new ReceiveThread(socket, this, numOfPlayers);
        receiveThread.start();

        getReady();
    }

    private void init() {
        // init 出牌，不出，计时器
        chuPaiJButton = new JButton("出牌");
        chuPaiJButton.setBounds(330, 350, 100, 40);
        chuPaiJButton.setFont(new Font("Dialog", 1, 20));
        chuPaiJButton.addMouseListener(new MyMouseEvent());
        chuPaiJButton.setVisible(false);
        this.myPanel.add(chuPaiJButton);

        buChuJButton = new JButton("不出");
        buChuJButton.setBounds(440, 350, 100, 40);
        buChuJButton.setFont(new Font("Dialog", 1, 20));
        buChuJButton.addMouseListener(new MyMouseEvent());
        buChuJButton.setVisible(false);
        this.myPanel.add(buChuJButton);

        chaJButton = new JButton("叉");
        chaJButton.setBounds(550, 350, 100, 40);
        chaJButton.setFont(new Font("Dialog", 1, 20));
        chaJButton.addMouseListener(new MyMouseEvent());
        chaJButton.setVisible(false);
        this.myPanel.add(chaJButton);

        gouJButton = new JButton("勾");
        gouJButton.setBounds(660, 350, 100, 40);
        gouJButton.setFont(new Font("Dialog", 1, 20));
        gouJButton.addMouseListener(new MyMouseEvent());
        gouJButton.setVisible(false);
        this.myPanel.add(gouJButton);

        timeLabel = new JLabel();
        timeLabel.setBounds(770, 350, 50, 50);
        timeLabel.setFont(new Font("Dialog", 0, 30));
        timeLabel.setForeground(Color.red);
        timeLabel.setVisible(false);
        this.myPanel.add(timeLabel);

        msgLabel = new JLabel();
        msgLabel.setVisible(false);
        this.myPanel.add(msgLabel);
    }

    private void getReady() {
        readyJButton = new JButton("Ready");
        readyJButton.setBounds(500, 400, 200, 40);
        readyJButton.setFont(new Font("Dialog", 1, 30));
        readyJButton.addMouseListener(new MyMouseEvent());
        this.myPanel.add(readyJButton);

        this.repaint();
    }

    public void showAllPlayersInfo(List<Player> players) {
        // show current player's poker list
        for (Player player : players) {
            if (player.getPlayerUname().equals(uname)) {
                currentPlayer = player;
            }
        }
        List<Poker> pokers = currentPlayer.getPokers();
        for (int i = 0; i < pokers.size(); i++) {
            Poker poker = pokers.get(i);
            PokerLabel pokerLabel = new PokerLabel(poker.getId(), poker.getName(), poker.getColor(), poker.getNum(), poker.isHun());
            // show the poker
            pokerLabel.turnUp();
            this.pokerLabels.add(pokerLabel);
            this.myPanel.add(pokerLabel);
            // set Z axis overlap
            this.myPanel.setComponentZOrder(pokerLabel, 0);
            GameUtil.move(pokerLabel, 180 + 30 * i, 450, true);
        }

        Collections.sort(pokerLabels);

        for (int i = 0; i < pokerLabels.size(); i++) {
            this.myPanel.setComponentZOrder(pokerLabels.get(i), 0);
            GameUtil.move(pokerLabels.get(i), 180 + 30 * i, 450, true);
        }

        if (needSendGong) {
            // if 上贡, provide button for the player to send the poker to server
            sendGong();
        }

    }

    private void sendGong() {

    }

    // add click event to poker
    public void addClickEventToPoker() {
        for (PokerLabel pokerLabel : pokerLabels) {
            pokerLabel.addMouseListener(new PokerEvent());
        }
    }

    // 显示出牌按钮和不出牌以及倒计时
    public void showChuPaiJButton() {
        chuPaiJButton.setVisible(true);
        buChuJButton.setVisible(true);
        timeLabel.setVisible(true);
        this.repaint();
        // start 出牌定时器
        chuPaiCountThread = new ChuPaiCountThread(100, this);
        chuPaiCountThread.start();
    }

    public void showChaJButton() {
        chaJButton.setVisible(true);
        this.repaint();
    }

    public void showGouJButton() {
        gouJButton.setVisible(true);
        this.repaint();
    }

    // display 出牌, 不出牌message
    public void showMsg(int typeId, String msg) {
        System.out.println(uname + " show message: " + typeId + " " + msg);
        msgLabel.setBounds(650, 250, 200, 80);
        if (typeId == 3) {
            msgLabel.setText(msg + " 不出");
        } else if (typeId == 5) {
            msgLabel.setText(msg + " 叉");
        } else if (typeId == 6) {
            msgLabel.setText(msg + " 勾");
        }
        msgLabel.setFont(new Font("Dialog", 0, 20));
        msgLabel.setVisible(true);
        this.repaint();
    }

    public void showOutPokerList(List<Poker> outPokers) {
        // clear previous outPokerList
        for (PokerLabel p: showOutPokerLabels) {
            myPanel.remove(p);
        }
        showOutPokerLabels.clear();
        for (int i = 0; i < outPokers.size(); i++) {
            Poker poker = outPokers.get(i);
            PokerLabel pokerLabel = new PokerLabel(poker.getId(), poker.getName(), poker.getColor(), poker.getNum(), poker.isHun());
            pokerLabel.setLocation(400 + 30 * i, 200);
            pokerLabel.turnUp();
            myPanel.add(pokerLabel);
            showOutPokerLabels.add(pokerLabel);
            myPanel.setComponentZOrder(pokerLabel, 0);
        }
        this.repaint();
    }

    // remove out poker from current player's poker list
    public void removeOutPokerFromPokerList() {
        // 1. remove from current poker list
        pokerLabels.removeAll(selectedPokerLabels);
        // 2. remove from label
        for (PokerLabel p: selectedPokerLabels) {
            myPanel.remove(p);
        }
        // 3. sort remaining pokerLabel
        for (int i = 0; i < pokerLabels.size(); i++) {
            myPanel.setComponentZOrder(pokerLabels.get(i), 0);
            GameUtil.move(pokerLabels.get(i), 180 + 30 * i, 450, false);
        }
        // clear selected poker list
        selectedPokerLabels.clear();
        msgLabel.setVisible(false);
        this.repaint();
    }

    public void timeOutRemoveButton() {
        // when timeout, remove button
        chuPaiJButton.setVisible(false);
        buChuJButton.setVisible(false);
        timeLabel.setVisible(false);
        chaJButton.setVisible(false);
        gouJButton.setVisible(false);
    }

    public List<Poker> changePokerLabelToPoker(List<PokerLabel> pokerLabelList) {
        List<Poker> list = new ArrayList<>();
        for (PokerLabel pokerLabel: pokerLabelList) {
            Poker poker = new Poker(pokerLabel.getId(), pokerLabel.getName(), pokerLabel.getColor(), pokerLabel.getNum());
            list.add(poker);
        }
        return list;
    }

    class MyMouseEvent implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (!isReady && e.getSource().equals(readyJButton)) {
                // after click, reset text and send message to server
                isReady = true;
                readyJButton.setText("Waiting for other players");
                readyJButton.setBounds(500, 400, 200, 40);
                readyJButton.setFont(new Font("Dialog", 0, 15));
                // currently, player does not know his playerId, use 0 here
                Message msg = new Message(1, 0, uname, "Ready", null);
                sendThread.setMsg(JSON.toJSONString(msg));
            } else if (e.getSource().equals(chuPaiJButton)) {
                // need to check if the out poker is valid
                PokerType pokerType = PokerRule.checkPokerType(selectedPokerLabels);
                if (!pokerType.equals(PokerType.p_error)) {
                    if (prevPlayerId == -1 || prevPlayerId == currentPlayer.getId() || PokerRule.isBigger(showOutPokerLabels, selectedPokerLabels)) {
                        // stop the counter
                        isOut = true;
                        chuPaiCountThread.setRun(false);
                        chuPaiJButton.setVisible(false);
                        buChuJButton.setVisible(false);
                        timeLabel.setVisible(false);
                        chaJButton.setVisible(false);
                        gouJButton.setVisible(false);
                    } else {
                        JOptionPane.showMessageDialog(null, "请按规则出牌");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "不符合牌型");
                }

            } else if (e.getSource().equals(buChuJButton)) {
                isOut = false;
                chuPaiCountThread.setRun(false);
                chuPaiJButton.setVisible(false);
                buChuJButton.setVisible(false);
                chaJButton.setVisible(false);
                gouJButton.setVisible(false);
                timeLabel.setVisible(false);
            } else if (e.getSource().equals(chaJButton)) {
                try {
                    if (PokerRule.isCha(showOutPokerLabels, selectedPokerLabels)) {
                        isOut = true;
                        chaJButton.setVisible(false);
                        // send message to server
                        Message msg = new Message(5, currentPlayer.getId(), uname, "叉", changePokerLabelToPoker(selectedPokerLabels));
                        removeOutPokerFromPokerList();
                        String msgJSONString = JSON.toJSONString(msg);
                        sendThread.setMsg(msgJSONString);
                    } else {
                        JOptionPane.showMessageDialog(null, "请按规则出牌");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (e.getSource().equals(gouJButton)) {
                System.out.println("Click 勾");
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    // implement mouse events
    class PokerEvent implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            // choose the poker or un-choose the poker
            PokerLabel pokerLabel = (PokerLabel) e.getSource();
            if (pokerLabel.isSelected()) {
                pokerLabel.setLocation(pokerLabel.getX(), pokerLabel.getY() + 30);
                selectedPokerLabels.remove(pokerLabel);
                pokerLabel.setSelected(false);
            } else {
                pokerLabel.setLocation(pokerLabel.getX(), pokerLabel.getY() - 30);
                selectedPokerLabels.add(pokerLabel);
                pokerLabel.setSelected(true);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
}
