package com.tonyzyc.thread;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tonyzyc.model.Player;
import com.tonyzyc.model.Poker;
import com.tonyzyc.view.MainFrame;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ReceiveThread extends Thread {
    private Socket socket;
    private MainFrame mainFrame;
    private int numOfPlayers;

    private int step = 0;

    public ReceiveThread(Socket socket, MainFrame mainFrame, int numOfPlayers) {
        this.socket = socket;
        this.mainFrame = mainFrame;
        this.numOfPlayers = numOfPlayers;
    }

    private List<Poker> getPokerListFromJSON(JSONObject obj) {
        List<Poker> list = new ArrayList<>();
        JSONArray pokerListJSONArray = obj.getJSONArray("pokers");
        for (Object o : pokerListJSONArray) {
            JSONObject pokerJSONObject = (JSONObject) o;
            int pokerId = pokerJSONObject.getInteger("id");
            String pokerName = pokerJSONObject.getString("name");
            int pokerNum = pokerJSONObject.getInteger("num");
            Poker poker = new Poker(pokerId, pokerName, pokerNum);
            list.add(poker);
        }
        return list;

    }

    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            while (true) {
                String jsonString = dataInputStream.readUTF();
                // first step , get all players' information
                if (step == 0) {
                    List<Player> players = new ArrayList<>();
                    JSONArray playersJSONArray = JSONArray.parseArray(jsonString);
                    // System.out.println(playersJSONArray);
                    for (int i = 0; i < playersJSONArray.size(); i++) {
                        // get every player json object
                        JSONObject playerJSONObject = (JSONObject) playersJSONArray.get(i);
                        int playerId = playerJSONObject.getInteger("id");
                        String playerUname = playerJSONObject.getString("playerUname");
                        boolean isFirst = playerJSONObject.getBoolean("first");
                        // store the poker list for each player
                        List<Poker> pokers = getPokerListFromJSON(playerJSONObject);
                        Player player = new Player(playerId, playerUname, pokers, isFirst);
                        players.add(player);
                    }
                    // if you get all players' info, display the info on the screen
                    if (players.size() == numOfPlayers) {
                        mainFrame.readyJButton.setVisible(false);
                        mainFrame.showAllPlayersInfo(players);
                        // all players have arrived, get to the second step
                        step = 1;
                        if (mainFrame.currentPlayer.isFirst()) {
                            // first player have the 出牌
                            mainFrame.showChuPaiJButton();
                        }
                    }
                } else if (step == 1) {
                    // 开始打牌
                    JSONObject msgJSONObject = JSONObject.parseObject(jsonString);
                    int typeId = msgJSONObject.getInteger("typeId");
                    int playerId = msgJSONObject.getInteger("playerId");
                    String playerUname = msgJSONObject.getString("playerUname");
                    String contentString = msgJSONObject.getString("contentString");
                    if (typeId == 3) {
                        // 不出牌消息
                        mainFrame.showMsg(typeId, playerUname);
                        // 判断现在是不是自己出牌
                        if ((playerId + 1) % mainFrame.numOfPlayers == mainFrame.currentPlayer.getId()) {
                            mainFrame.showChuPaiJButton();
                        }

                    } else if (typeId == 4) {
                        // 出牌, get the outPokerList
                        List<Poker> outPokerList = getPokerListFromJSON(msgJSONObject);
                        mainFrame.showOutPokerList(outPokerList);
                        // 判断现在是不是自己出牌
                        if ((playerId + 1) % mainFrame.numOfPlayers == mainFrame.currentPlayer.getId()) {
                            mainFrame.showChuPaiJButton();
                        }
                        mainFrame.prevPlayerId = playerId;
                    }
                }
                mainFrame.addClickEventToPoker();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
