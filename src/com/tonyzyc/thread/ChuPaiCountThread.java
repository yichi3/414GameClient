package com.tonyzyc.thread;

import com.alibaba.fastjson.JSON;
import com.tonyzyc.model.Message;
import com.tonyzyc.model.Poker;
import com.tonyzyc.model.PokerLabel;
import com.tonyzyc.view.MainFrame;

import java.util.ArrayList;
import java.util.List;

public class ChuPaiCountThread extends Thread {
    private int time;
    private MainFrame mainFrame;
    private boolean isRun;
    private boolean isCha;
    private boolean isGou;
    private int chaPlayerId = -1;
    private int gouPlayerId = -1;

    public ChuPaiCountThread(int time, MainFrame mainFrame) {
        this.time = time;
        this.mainFrame = mainFrame;
        this.isRun = true;
        this.isCha = false;
        this.isGou = false;
    }

    public boolean isRun() {
        return isRun;
    }

    public void setRun(boolean run) {
        isRun = run;
    }

    public boolean isCha() {
        return isCha;
    }

    public void setCha(boolean cha) {
        isCha = cha;
    }

    public int getChaPlayerId() {
        return chaPlayerId;
    }

    public void setChaPlayerId(int chaPlayerId) {
        this.chaPlayerId = chaPlayerId;
    }

    public boolean isGou() {
        return isGou;
    }

    public void setGou(boolean gou) {
        isGou = gou;
    }

    public int getGouPlayerId() {
        return gouPlayerId;
    }

    public void setGouPlayerId(int gouPlayerId) {
        this.gouPlayerId = gouPlayerId;
    }

    private void gouChaManipulate() {
        isRun = false;
        mainFrame.chuPaiJButton.setVisible(false);
        mainFrame.buChuJButton.setVisible(false);
        mainFrame.timeLabel.setVisible(false);
    }

    public void run() {
        while (time >= 0 && isRun && !isCha && !isGou) {
            mainFrame.timeLabel.setText(time+"");
            time--;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Message msg;
        if (!isCha && !isGou) {
            if (time < 0 || !mainFrame.isOut) {
                // if time is up or choose to 不出
                msg = new Message(3, mainFrame.currentPlayer.getId(), mainFrame.currentPlayer.getPlayerUname(), "不出", null);
                mainFrame.timeOutRemoveButton();
            } else {
                msg = new Message(4, mainFrame.currentPlayer.getId(), mainFrame.currentPlayer.getPlayerUname(), "出牌", mainFrame.changePokerLabelToPoker(mainFrame.selectedPokerLabels));
                // remove outPoker from my deck
                mainFrame.removeOutPokerFromPokerList(4);
            }
            String msgJSONString = JSON.toJSONString(msg);
            mainFrame.sendThread.setMsg(msgJSONString);
        } else if (isCha) {
            // someone 叉, need to stop the count and stop the round
            if (mainFrame.currentPlayer.getId() != chaPlayerId) {
                gouChaManipulate();
            }
            chaPlayerId = -1;
        } else {
            // someone 勾, need to stop the count and stop the round
            if (mainFrame.currentPlayer.getId() != gouPlayerId) {
                gouChaManipulate();
            }
            gouPlayerId = -1;
        }
        // check if current player has no poker
        if (mainFrame.pokerLabels.size() == 0) {
            msg = new Message(10, mainFrame.currentPlayer.getId(), mainFrame.currentPlayer.getPlayerUname(), "出完", null);
            String msgJSONString = JSON.toJSONString(msg);
            try {
                // wait for 100 ms to avoid problem
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mainFrame.sendThread.setMsg(msgJSONString);
        }
    }
}
