package com.tonyzyc.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.tonyzyc.model.Player;
import com.tonyzyc.model.Poker;
import com.tonyzyc.model.PokerLabel;
import com.tonyzyc.view.MainFrame;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            String pokerColor = pokerJSONObject.getString("color");
            int pokerNum = pokerJSONObject.getInteger("num");
            boolean isHun = pokerJSONObject.getBoolean("hun");
            Poker poker = new Poker(pokerId, pokerName, pokerColor, pokerNum, isHun);
            list.add(poker);
        }
        return list;

    }


    private List<PokerLabel> getPokerLabelFromPoker(List<Poker> pokers) {
        List<PokerLabel> list = new ArrayList<>();
        for (Poker p: pokers) {
            PokerLabel pokerLabel = new PokerLabel(p.getId(), p.getName(), p.getColor(), p.getNum(), p.isHun());
            list.add(pokerLabel);
        }
        return list;
    }

    private void checkCha(int playerId, List<Poker> outPokerList) {
        if (playerId != mainFrame.currentPlayer.getId() && outPokerList.size() == 1 && outPokerList.get(0).getNum() <= 15) {
            // 叉!
            int num = outPokerList.get(0).getNum();
            int count = 0;
            for (PokerLabel p: mainFrame.pokerLabels) {
                if (p.getNum() == num) {
                    count++;
                }
                if (count >= 2) {
                    mainFrame.showChaJButton();
                    break;
                }
            }
        }
    }

    private void checkGou(List<Poker> outPokerList) {
        int num = outPokerList.get(0).getNum();
        for (PokerLabel p: mainFrame.pokerLabels) {
            if (p.getNum() == num) {
                mainFrame.showGouJButton();
                break;
            }
        }
    }

    private static <K, V> Map<K, V> parseToMap(String json) {
        return JSON.parseObject(json,
                new TypeReference<>(Integer.class, Player.class) {
                });
    }

    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            while (true) {
                String jsonString = dataInputStream.readUTF();
                // first step , get all players' information
                if (step == 0) {
                    List<Player> players = new ArrayList<>();
                    Map<Integer, Player> playerMap = parseToMap(jsonString);
                    for (int i = 0; i < playerMap.size(); i++) {
                        // get every player json object
                        Player player = playerMap.get(i);
                        players.add(player);
                    }
                    // if you get all players' info, display the info on the screen
                    if (players.size() == numOfPlayers) {
                        mainFrame.readyJButton.setVisible(false);
                        mainFrame.showAllPlayersInfo(players);
                        // all players have arrived, get to the second step
                        step = 1;
                        mainFrame.addClickEventToPoker();
                        if (mainFrame.currentPlayer.isFirst()) {
                            // first player have the 出牌
                            mainFrame.showChuPaiJButton(true);
                        }
                    }
                } else if (step == 1) {
                    // 开始打牌
                    JSONObject msgJSONObject = JSONObject.parseObject(jsonString);
                    int typeId = msgJSONObject.getInteger("typeId");
                    int playerId = msgJSONObject.getInteger("playerId");
                    String playerUname = msgJSONObject.getString("playerUname");

                    if (typeId == 3) {
                        // 不出牌消息
                        mainFrame.showMsg(typeId, playerUname);
                        mainFrame.chaJButton.setVisible(false);
                        mainFrame.gouJButton.setVisible(false);
                        // 判断现在是不是自己出牌
                        if ((playerId + 1) % mainFrame.numOfPlayers == mainFrame.currentPlayer.getId()) {
                            mainFrame.showChuPaiJButton(mainFrame.currentPlayer.getId() == mainFrame.prevPlayerId);
                        }
                    } else if (typeId == 4) {
                        // 出牌, get the outPokerList
                        mainFrame.msgLabel.setVisible(false);
                        mainFrame.chaJButton.setVisible(false);
                        mainFrame.chaJButton.setVisible(false);
                        mainFrame.gouJButton.setVisible(false);
                        List<Poker> outPokerList = getPokerListFromJSON(msgJSONObject);
                        mainFrame.showOutPokerList(outPokerList);
                        mainFrame.prevPlayerId = playerId;
                        // 判断现在是不是自己出牌
                        if ((playerId + 1) % mainFrame.numOfPlayers == mainFrame.currentPlayer.getId()) {
                            mainFrame.showChuPaiJButton(mainFrame.currentPlayer.getId() == mainFrame.prevPlayerId);
                        }
                        checkCha(playerId, outPokerList);
                    } else if (typeId == 5) {
                        // someone 叉
                        mainFrame.chuPaiCountThread.setCha(true);
                        mainFrame.chuPaiCountThread.setChaPlayerId(playerId);
                        mainFrame.showMsg(typeId, playerUname);
                        List<Poker> outPokerList = getPokerListFromJSON(msgJSONObject);
                        mainFrame.showOutPokerList(outPokerList);
                        mainFrame.prevPlayerId = playerId;
                        // check if current player 叉
                        if (playerId == mainFrame.currentPlayer.getId()) {
                            mainFrame.showChuPaiJButton(mainFrame.currentPlayer.getId() == mainFrame.prevPlayerId);
                        } else {
                            mainFrame.chaJButton.setVisible(false);
                            // check if other player want to 勾
                            checkGou(outPokerList);
                        }
                    } else if (typeId == 6) {
                        // someone 勾
                        mainFrame.chuPaiCountThread.setGou(true);
                        mainFrame.chuPaiCountThread.setGouPlayerId(playerId);
                        mainFrame.showMsg(typeId, playerUname);
                        List<Poker> outPokerList = getPokerListFromJSON(msgJSONObject);
                        mainFrame.showOutPokerList(outPokerList);
                        mainFrame.prevPlayerId = playerId;
                        // check if current player 勾
                        if (playerId == mainFrame.currentPlayer.getId()) {
                            mainFrame.showChuPaiJButton(mainFrame.currentPlayer.getId() == mainFrame.prevPlayerId);
                        } else {
                            mainFrame.gouJButton.setVisible(false);
                            // check if other player want to 叉
                            checkCha(playerId, outPokerList);
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
