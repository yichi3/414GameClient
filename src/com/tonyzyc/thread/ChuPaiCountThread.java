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

    public ChuPaiCountThread(int time, MainFrame mainFrame) {
        this.time = time;
        this.mainFrame = mainFrame;
        this.isRun = true;
    }

    public boolean isRun() {
        return isRun;
    }

    public void setRun(boolean run) {
        isRun = run;
    }

    public void run() {
        while (time >= 0 && isRun) {
            mainFrame.timeLabel.setText(time+"");
            time--;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Message msg = null;
        if (time < 0 || !mainFrame.isOut) {
            // if time is up or choose to 不出
            msg = new Message(3, mainFrame.currentPlayer.getId(), mainFrame.currentPlayer.getPlayerUname(), "不出", null);

        } else {
            msg = new Message(4, mainFrame.currentPlayer.getId(), mainFrame.currentPlayer.getPlayerUname(), "出牌", changePokerLabelToPoker(mainFrame.selectedPokerLabels));
            // remove outPoker from my deck
            mainFrame.removeOutPokerFromPokerList();
        }
        String msgJSONString = JSON.toJSONString(msg);
        mainFrame.sendThread.setMsg(msgJSONString);
    }

    private List<Poker> changePokerLabelToPoker(List<PokerLabel> pokerLabelList) {
        List<Poker> list = new ArrayList<>();
        for (PokerLabel pokerLabel: pokerLabelList) {
            Poker poker = new Poker(pokerLabel.getId(), pokerLabel.getName(), pokerLabel.getNum());
            list.add(poker);
        }
        return list;
    }
}
