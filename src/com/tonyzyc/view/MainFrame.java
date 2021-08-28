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
import java.util.*;
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
    public JTextField hunJTextField;
    public JButton hunSubmitJButton;
    public JButton firstPlayerJButton;
    public JButton shangGongJButton;
    public JButton selectGongJButton;
    public JButton huiGongJButton;
    public JButton cancelGongJButton;
    public JButton guanShangJButton;
    // count for total number of gou/cha pokers
    public int lowerCount = 1;
    // store the current poker label list
    public List<PokerLabel> pokerLabels = new ArrayList<>();
    // counter thread, used to countdown for time
    public ChuPaiCountThread chuPaiCountThread = new ChuPaiCountThread(30, this);
    // store the selected pokerLabels
    public List<PokerLabel> selectedPokerLabels = new ArrayList<>();
    public List<PokerLabel> selectedGongPokerLabels = new ArrayList<>();
    // 显示当前出牌list
    public List<PokerLabel> showOutPokerLabels = new ArrayList<>();
    // check if the current player 是否出牌
    public boolean isOut;
    // last player who 出牌
    public int prevPlayerId = -1;
    public Set<Integer> donePlayerIds = new HashSet<>();

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
        // set hun number
        hunJTextField = new JTextField();
        hunJTextField.setBounds(330, 350, 100, 40);
        hunJTextField.setVisible(false);
        this.myPanel.add(hunJTextField);

        hunSubmitJButton = new JButton("选择\"混\"");
        hunSubmitJButton.setBounds(440, 350, 100, 40);
        hunSubmitJButton.setFont(new Font("Dialog", 1, 20));
        hunSubmitJButton.addMouseListener(new MyMouseEvent());
        hunSubmitJButton.setVisible(false);
        this.myPanel.add(hunSubmitJButton);

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

        firstPlayerJButton = new JButton("先出");
        firstPlayerJButton.setBounds(330, 350, 100, 40);
        firstPlayerJButton.setFont(new Font("Dialog", 1, 20));
        firstPlayerJButton.addMouseListener(new MyMouseEvent());
        firstPlayerJButton.setVisible(false);
        this.myPanel.add(firstPlayerJButton);

        shangGongJButton = new JButton("上供");
        shangGongJButton.setBounds(330, 350, 100, 40);
        shangGongJButton.setFont(new Font("Dialog", 1, 20));
        shangGongJButton.addMouseListener(new MyMouseEvent());
        shangGongJButton.setVisible(false);
        this.myPanel.add(shangGongJButton);

        selectGongJButton = new JButton("选供");
        selectGongJButton.setBounds(330, 350, 100, 40);
        selectGongJButton.setFont(new Font("Dialog", 1, 20));
        selectGongJButton.addMouseListener(new MyMouseEvent());
        selectGongJButton.setVisible(false);
        this.myPanel.add(selectGongJButton);

        huiGongJButton = new JButton("回供");
        huiGongJButton.setBounds(440, 350, 100, 40);
        huiGongJButton.setFont(new Font("Dialog", 1, 20));
        huiGongJButton.addMouseListener(new MyMouseEvent());
        huiGongJButton.setVisible(false);
        this.myPanel.add(huiGongJButton);

        cancelGongJButton = new JButton("取消");
        cancelGongJButton.setBounds(550, 350, 100, 40);
        cancelGongJButton.setFont(new Font("Dialog", 1, 20));
        cancelGongJButton.addMouseListener(new MyMouseEvent());
        cancelGongJButton.setVisible(false);
        this.myPanel.add(cancelGongJButton);

        guanShangJButton = new JButton("管上");
        guanShangJButton.setBounds(330, 350, 100, 40);
        guanShangJButton.setFont(new Font("Dialog", 1, 20));
        guanShangJButton.addMouseListener(new MyMouseEvent());
        guanShangJButton.setVisible(false);
        this.myPanel.add(guanShangJButton);
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

        Collections.sort(pokerLabels, Collections.reverseOrder());

        for (int i = 0; i < pokerLabels.size(); i++) {
            this.myPanel.setComponentZOrder(pokerLabels.get(i), 0);
            GameUtil.move(pokerLabels.get(i), 180 + 30 * i, 450, true);
        }

    }

    // add click event to poker
    public void addClickEventToPoker() {
        for (PokerLabel pokerLabel : pokerLabels) {
            pokerLabel.addMouseListener(new PokerEvent(selectedPokerLabels));
        }
    }

    public void addClickEventToGong() {
        for (PokerLabel p: showOutPokerLabels) {
            p.addMouseListener(new PokerEvent(selectedGongPokerLabels));
            System.out.println("Add listener to " + p);
        }
    }

    // 显示出牌按钮和不出牌以及倒计时
    public void showChuPaiJButton(boolean isFirst) {
        chuPaiJButton.setVisible(true);
        buChuJButton.setVisible(!isFirst);
        timeLabel.setVisible(true);
        this.repaint();
        // start 出牌定时器
        chuPaiCountThread = new ChuPaiCountThread(30, this);
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

    public boolean isNextPlayer(int playerId) {
        // check if current player is the next player
        while (donePlayerIds.contains((playerId + 1) % numOfPlayers)) {
            playerId++;
        }
        return (playerId + 1) % numOfPlayers == currentPlayer.getId();
    }

    // display 出牌, 不出牌message
    public void showMsg(int typeId, String msg) {
        msgLabel.setBounds(650, 100, 200, 80);
        if (typeId == 3) {
            msgLabel.setText(msg + " 不出");
        } else if (typeId == 5) {
            msgLabel.setText(msg + " 叉");
            System.out.println(uname + " show message: " + msg + " 叉");
        } else if (typeId == 6) {
            msgLabel.setText(msg + " 勾");
            System.out.println(uname + " show message: " + msg + " 勾");
        } else if (typeId == 10) {
            msgLabel.setText(msg + "出完牌了!");
        } else if (typeId == 15) {
            msgLabel.setText(msg + "报片!");
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
            this.myPanel.setComponentZOrder(pokerLabel, 0);
        }
        this.repaint();
    }

    public void showOutGongList(List<Poker> pickedGongPoker) throws Exception {
        // first remove picked poker from showOutPokerLabels
        if (pickedGongPoker.size() != 1) {
            throw new Exception("pickedGongPoker size should be 1");
        }
        Poker pickedPoker = pickedGongPoker.get(0);
        for (PokerLabel p: showOutPokerLabels) {
            if (p.getId() == pickedPoker.getId()) {
                myPanel.remove(p);
            }
        }
        this.repaint();
    }

    // remove out poker from current player's poker list
    public void removeOutPokerFromPokerList(int typeId) {
        // 1. remove from current poker list
        pokerLabels.removeAll(selectedPokerLabels);
        // 2. remove from label
        reRenderPokerLabels(selectedPokerLabels);
        if (typeId != 5 && typeId != 6) {
            msgLabel.setVisible(false);
        }
        this.repaint();
        // check if all pokers are out
        if (pokerLabels.size() == 0) {
            try {
                Thread.sleep(100);
                sendMsgToServer(new Message(10, currentPlayer.getId(), uname, "出完", null));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (pokerLabels.size() <= 3) {
            try {
                Thread.sleep(100);
                sendMsgToServer(new Message(15, currentPlayer.getId(), uname, "报片", null));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void addGongPokerFromGongList() {
        for (PokerLabel p: selectedGongPokerLabels) {
            PokerLabel pokerLabel = new PokerLabel(p);
            pokerLabel.turnUp();
            pokerLabels.add(pokerLabel);
            myPanel.add(pokerLabel);
        }
        reRenderPokerLabels(selectedGongPokerLabels);
        this.repaint();
        System.out.println("add Gong to PokerList");
    }

    private void reRenderPokerLabels(List<PokerLabel> list) {
        for (PokerLabel p: list) {
            myPanel.remove(p);
        }
        Collections.sort(pokerLabels, Collections.reverseOrder());
        for (int i = 0; i < pokerLabels.size(); i++) {
            this.myPanel.setComponentZOrder(pokerLabels.get(i), 0);
            GameUtil.move(pokerLabels.get(i), 180 + 30 * i, 450, false);
        }
        list.clear();
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
            Poker poker = new Poker(pokerLabel.getId(), pokerLabel.getName(), pokerLabel.getColor(), pokerLabel.getNum(), pokerLabel.isHun());
            list.add(poker);
        }
        return list;
    }

    public void sendMsgToServer(Message msg) {
        String msgJSONString = JSON.toJSONString(msg);
        sendThread.setMsg(msgJSONString);
    }

    public void resetGame() {
        // reset the game
        readyJButton.setVisible(true);
        readyJButton.setText("Ready");
        isReady = false;
        msgLabel.setVisible(false);
        chuPaiJButton.setVisible(false);
        buChuJButton.setVisible(false);
        chaJButton.setVisible(false);
        gouJButton.setVisible(false);
        timeLabel.setVisible(false);
        hunJTextField.setVisible(false);
        hunSubmitJButton.setVisible(false);
        shangGongJButton.setVisible(false);
        huiGongJButton.setVisible(false);
        cancelGongJButton.setVisible(false);
        pokerLabels.clear();
        chuPaiCountThread = new ChuPaiCountThread(30, this);
        selectedPokerLabels.clear();
        isOut = false;
        prevPlayerId = -1;
        donePlayerIds.clear();
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
                // remove all pokers from last match
                for (PokerLabel p: showOutPokerLabels) {
                    myPanel.remove(p);
                }
                repaint();
                showOutPokerLabels.clear();
                // currently, player does not know his playerId, use -1 if first initialize
                int playerId = currentPlayer == null ? -1 : currentPlayer.getId();
                sendMsgToServer(new Message(1, playerId, uname, numOfPlayers+"", null));
            } else if (e.getSource().equals(chuPaiJButton)) {
                // need to check if the out poker is valid
//                if (selectedPokerLabels.size() == 0) {
//                    JOptionPane.showMessageDialog(null, "请出牌");
//                } else {
//                    PokerType pokerType = PokerRule.checkPokerType(selectedPokerLabels);
//                    if (!pokerType.equals(PokerType.p_error)) {
//                        if (prevPlayerId == -1 || prevPlayerId == currentPlayer.getId() || PokerRule.isBigger(showOutPokerLabels, selectedPokerLabels)) {
//                            // stop the counter
//                            isOut = true;
//                            chuPaiCountThread.setRun(false);
//                            chuPaiJButton.setVisible(false);
//                            buChuJButton.setVisible(false);
//                            timeLabel.setVisible(false);
//                            chaJButton.setVisible(false);
//                            gouJButton.setVisible(false);
//                            guanShangJButton.setVisible(false);
//                        } else {
//                            JOptionPane.showMessageDialog(null, "请按规则出牌");
//                        }
//                    } else {
//                        JOptionPane.showMessageDialog(null, "不符合牌型");
//                    }
//                }
                isOut = true;
                chuPaiCountThread.setRun(false);
                chuPaiJButton.setVisible(false);
                buChuJButton.setVisible(false);
                timeLabel.setVisible(false);
                chaJButton.setVisible(false);
                gouJButton.setVisible(false);
                guanShangJButton.setVisible(false);
            } else if (e.getSource().equals(buChuJButton)) {
                isOut = false;
                chuPaiCountThread.setRun(false);
                chuPaiJButton.setVisible(false);
                buChuJButton.setVisible(false);
                chaJButton.setVisible(false);
                gouJButton.setVisible(false);
                timeLabel.setVisible(false);
                guanShangJButton.setVisible(false);
            } else if (e.getSource().equals(chaJButton)) {
                try {
                    if (PokerRule.isCha(showOutPokerLabels, selectedPokerLabels)) {
                        isOut = true;
                        chaJButton.setVisible(false);
                        // send message to server
                        Message msg = new Message(5, currentPlayer.getId(), uname, "叉", changePokerLabelToPoker(selectedPokerLabels));
                        sendMsgToServer(msg);
                        removeOutPokerFromPokerList(5);
                    } else {
                        JOptionPane.showMessageDialog(null, "请按规则出牌");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (e.getSource().equals(gouJButton)) {
                try {
                    if (PokerRule.isGou(showOutPokerLabels, selectedPokerLabels)) {
                        isOut = true;
                        gouJButton.setVisible(false);
                        // send message to server
                        Message msg = new Message(6, currentPlayer.getId(), uname, "勾", changePokerLabelToPoker(selectedPokerLabels));
                        sendMsgToServer(msg);
                        removeOutPokerFromPokerList(6);
                    } else {
                        JOptionPane.showMessageDialog(null, "请按规则出牌");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (e.getSource().equals(hunSubmitJButton)) {
                // first check if user type in correct keyword
                String hun = hunJTextField.getText();
                if (hun.length() != 1 && !hun.equals("10")) {
                    JOptionPane.showMessageDialog(null, "请输入正确\"混\"(A, 2, ..., K)");
                } else {
                    char c = hun.charAt(0);
                    if (c >= '2' && c <= '9' || c == 'J' || c == 'Q' || c == 'K' || c == 'A' || hun.equals("10")) {
                        hunJTextField.setText("");
                        hunJTextField.setVisible(false);
                        hunSubmitJButton.setVisible(false);
                        sendMsgToServer(new Message(9, 0, uname, hun, null));
                    } else {
                        JOptionPane.showMessageDialog(null, "请输入正确\"混\"(A, 2, ..., K)");
                    }
                }
            } else if (e.getSource().equals(shangGongJButton)) {
                if (selectedPokerLabels.size() != 1) {
                    JOptionPane.showMessageDialog(null, "只需要上供一张牌");
                } else {
                    if (selectedPokerLabels.get(0).getNum() != pokerLabels.get(0).getNum()) {
                        JOptionPane.showMessageDialog(null, "必须上供最大的牌");
                    } else {
                        shangGongJButton.setVisible(false);
                        cancelGongJButton.setVisible(false);
                        sendMsgToServer(new Message(11, currentPlayer.getId(), uname, "上供", changePokerLabelToPoker(selectedPokerLabels)));
                        removeOutPokerFromPokerList(11);
                    }
                }
            } else if (e.getSource().equals(huiGongJButton)) {
                if (selectedPokerLabels.size() != 1) {
                    JOptionPane.showMessageDialog(null, "只需要回供一张牌");
                } else {
                    huiGongJButton.setVisible(false);
                    cancelGongJButton.setVisible(false);
                    sendMsgToServer(new Message(12, currentPlayer.getId(), uname, "回供", changePokerLabelToPoker(selectedPokerLabels)));
                    removeOutPokerFromPokerList(12);
                }
            } else if (e.getSource().equals(cancelGongJButton)) {
                shangGongJButton.setVisible(false);
                huiGongJButton.setVisible(false);
                selectGongJButton.setVisible(false);
                cancelGongJButton.setVisible(false);
                sendMsgToServer(new Message(13, currentPlayer.getId(), uname, "取消上供/回供/选供", new ArrayList<>()));
            } else if (e.getSource().equals(firstPlayerJButton)) {
                firstPlayerJButton.setVisible(false);
                sendMsgToServer(new Message(7, currentPlayer.getId(), uname, "第一人出牌", null));
            } else if (e.getSource().equals(guanShangJButton)) {
                if (PokerRule.checkPokerType(selectedPokerLabels).equals(PokerType.p_414)
                        || PokerRule.checkPokerType(selectedPokerLabels).equals(PokerType.p_3plus) && selectedPokerLabels.size() > lowerCount
                        || PokerRule.checkPokerType(selectedPokerLabels).equals(PokerType.p_joker) && selectedPokerLabels.size() * 2 > lowerCount) {
                    sendMsgToServer(new Message(4, currentPlayer.getId(), uname, "出牌", changePokerLabelToPoker(selectedPokerLabels)));
                    removeOutPokerFromPokerList(4);
                    guanShangJButton.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(null, "必须大过上家");
                }
            } else if (e.getSource().equals(selectGongJButton)) {
                // at most one gong selected
                if (selectedGongPokerLabels.size() != 1) {
                    JOptionPane.showMessageDialog(null, "只能选择一张供牌");
                } else {
                    selectGongJButton.setVisible(false);
                    cancelGongJButton.setVisible(false);
                    sendMsgToServer(new Message(14, currentPlayer.getId(), uname, "选供", changePokerLabelToPoker(selectedGongPokerLabels)));
                    addGongPokerFromGongList();
                }
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

    private void pokerMouseClicked(MouseEvent e, List<PokerLabel> list) {
        PokerLabel pokerLabel = (PokerLabel) e.getSource();
        if (pokerLabel.isSelected()) {
            pokerLabel.setLocation(pokerLabel.getX(), pokerLabel.getY() + 30);
            list.remove(pokerLabel);
            pokerLabel.setSelected(false);
        } else {
            pokerLabel.setLocation(pokerLabel.getX(), pokerLabel.getY() - 30);
            list.add(pokerLabel);
            pokerLabel.setSelected(true);
        }
    }


    // implement mouse events
    class PokerEvent implements MouseListener {
        private final List<PokerLabel> list;
        public PokerEvent(List<PokerLabel> list) {
            this.list = list;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // choose the poker or un-choose the poker
            pokerMouseClicked(e, list);
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
