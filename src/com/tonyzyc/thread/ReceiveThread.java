package com.tonyzyc.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.tonyzyc.model.Player;
import com.tonyzyc.model.Poker;
import com.tonyzyc.model.PokerLabel;
import com.tonyzyc.util.GameState;
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

    private GameState step = GameState.Start;

    public ReceiveThread() {}

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

    public List<PokerLabel> getPokerLabelFromPoker(List<Poker> pokers) {
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
                System.out.println(jsonString);
                // first step, set hun
                if (step.equals(GameState.Start)) {
                    if (jsonString.equals(mainFrame.uname)) {
                        mainFrame.hunJTextField.setVisible(true);
                        mainFrame.hunSubmitJButton.setVisible(true);
                        mainFrame.readyJButton.setVisible(false);
                    }
                    step = GameState.SetHun;
                } else if (step.equals(GameState.SetHun)) {
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
                        step = GameState.SetFirstPlayer;
                        mainFrame.addClickEventToPoker();
                        mainFrame.firstPlayerJButton.setVisible(true);
                        // TODO: move this step later
//                        if (mainFrame.currentPlayer.isFirst()) {
//                            // first player have the 出牌
//                            mainFrame.showChuPaiJButton(true);
//                        }
                    }
                } else if (step == GameState.ShangGong) {
                    // all players need to decide whether he needs to shanggong or huigong
                    System.out.println("上供");
                    mainFrame.shangGongJButton.setVisible(true);
                    mainFrame.cancelGongJButton.setVisible(true);
                    step = GameState.PickShangGong;
                } else if (step == GameState.PickShangGong) {
                    JSONObject msgJSONObject = JSONObject.parseObject(jsonString);
                    List<Poker> outPokerList = getPokerListFromJSON(msgJSONObject);
                    if (outPokerList.size() != 0) {
                        mainFrame.showOutPokerList(outPokerList);
                    }

                    step = GameState.HuiGong;
                } else if (step == GameState.HuiGong) {
                    mainFrame.huiGongJButton.setVisible(true);
                    mainFrame.cancelGongJButton.setVisible(true);
                    step = GameState.PickHuiGong;
                } else if (step == GameState.PickHuiGong) {

                    step = GameState.Playing;
                } else if (step.equals(GameState.SetFirstPlayer)) {
                    JSONObject msgJSONObject = JSONObject.parseObject(jsonString);
                    int playerId = msgJSONObject.getInteger("playerId");
                    int typeId = msgJSONObject.getInteger("typeId");
                    if (typeId != 7) {
                        throw new Exception("typeId should be 7");
                    } else {
                        mainFrame.firstPlayerJButton.setVisible(false);
                        if (playerId == mainFrame.currentPlayer.getId()) {
                            // current player start the game
                            mainFrame.showChuPaiJButton(true);
                        }
                        step = GameState.Playing;
                    }
                } else if (step.equals(GameState.Playing)) {
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
                        if (mainFrame.isNextPlayer(playerId)) {
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
                        if (mainFrame.isNextPlayer(playerId)) {
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
                    } else if (typeId == 10) {
                        // some player played all pokers
                        mainFrame.showMsg(10, playerUname);
                        mainFrame.donePlayerIds.add(playerId);
                    } else if (typeId == 100) {
                        // end game, reset everything
                        mainFrame.resetGame();
                        step = GameState.Start;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
