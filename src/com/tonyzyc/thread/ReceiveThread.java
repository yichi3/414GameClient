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
    private int gongCount = 0;

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

    private void checkOverChaGou(List<Poker> outPokerList, String content) {
        int lowerCount = Integer.parseInt(content), count = 0;
        int num = outPokerList.get(0).getNum();
        boolean flag = false;
        for (PokerLabel p: mainFrame.pokerLabels) {
            if (p.getNum() == num) {
                count++;
            }
            if (count > lowerCount) {
                flag = true;
                break;
            }
        }
        if (flag || contain414()) {
            mainFrame.lowerCount = lowerCount;
            mainFrame.guanShangJButton.setVisible(true);
        }
    }

    private boolean contain414() {
        boolean one4 = false, two4 = false, oneA = false;
        for (PokerLabel p: mainFrame.pokerLabels) {
            if (p.getNum() == 4) {
                if (one4) {
                    two4 = true;
                } else {
                    one4 = true;
                }
            } else if (p.getNum() == 14) {
                oneA = true;
            }
        }
        return two4 && oneA;
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
                        // all players have arrived, decide Shanggong
                        // TODO: move the following two lines to the end of huiGong
//                        step = GameState.SetFirstPlayer;
//                        mainFrame.firstPlayerJButton.setVisible(true);
                        mainFrame.addClickEventToPoker();
                        step = GameState.ShangGong;
                        mainFrame.shangGongJButton.setVisible(true);
                        mainFrame.cancelGongJButton.setVisible(true);
                    }
                } else if (step == GameState.ShangGong) {
                    JSONObject msgJSONObject = JSONObject.parseObject(jsonString);
                    // first display all gong
                    System.out.println("get all Gong from server");
                    List<Poker> outPokerList = getPokerListFromJSON(msgJSONObject);
                    if (outPokerList.size() != 0) {
                        step = GameState.PickShangGong;
                        mainFrame.showOutPokerList(outPokerList);
                        mainFrame.addClickEventToGong();
                        mainFrame.selectGongJButton.setVisible(true);
                        mainFrame.cancelGongJButton.setVisible(true);
                    } else {
                        // nobody shanggong, choose the start player and start the game
                        step = GameState.SetFirstPlayer;
                        mainFrame.firstPlayerJButton.setVisible(true);
                    }
                } else if (step == GameState.PickShangGong) {
                    // if someone submit gong, others need to pick the card from list
                    // show receive numOfPlayers message
                    System.out.println("选供");
                    gongCount++;
                    JSONObject msgJSONObject = JSONObject.parseObject(jsonString);
                    int typeId = msgJSONObject.getInteger("typeId");
                    if (typeId == 14) {
                        List<Poker> pickedGongPoker = getPokerListFromJSON(msgJSONObject);
                        // now we need to remove the gong from the showOutPokerLabels
                        mainFrame.showOutGongList(pickedGongPoker);
                    }
                    if (gongCount == numOfPlayers) {
                        step = GameState.HuiGong;
                        gongCount = 0;
                        mainFrame.huiGongJButton.setVisible(true);
                        mainFrame.cancelGongJButton.setVisible(true);
                    }
                } else if (step == GameState.HuiGong) {
                    JSONObject msgJSONObject = JSONObject.parseObject(jsonString);
                    // first display all gong
                    System.out.println("get all hui Gong from server");
                    List<Poker> outPokerList = getPokerListFromJSON(msgJSONObject);
                    if (outPokerList.size() == 0) {
                        throw new Exception("Must 回供");
                    }
                    step = GameState.PickHuiGong;
                    mainFrame.showOutPokerList(outPokerList);
                    mainFrame.addClickEventToGong();
                    mainFrame.selectGongJButton.setVisible(true);
                    mainFrame.cancelGongJButton.setVisible(true);
                } else if (step == GameState.PickHuiGong) {
                    System.out.println("选回供");
                    gongCount++;
                    JSONObject msgJSONObject = JSONObject.parseObject(jsonString);
                    int typeId = msgJSONObject.getInteger("typeId");
                    if (typeId == 14) {
                        List<Poker> pickedGongPoker = getPokerListFromJSON(msgJSONObject);
                        // now we need to remove the gong from the showOutPokerLabels
                        mainFrame.showOutGongList(pickedGongPoker);
                    }
                    if (gongCount == numOfPlayers) {
                        step = GameState.SetFirstPlayer;
                        gongCount = 0;
                        mainFrame.firstPlayerJButton.setVisible(true);
                    }
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
                    String contentString = msgJSONObject.getString("contentString");

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
                        System.out.println("叉: " + msgJSONObject);
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
                            checkOverChaGou(outPokerList, contentString);
                        }
                    } else if (typeId == 6) {
                        // someone 勾
                        System.out.println("勾: " + msgJSONObject);
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
                            checkOverChaGou(outPokerList, contentString);
                        }
                    } else if (typeId == 7) {
                        // set the player to go
                        if (playerId == mainFrame.currentPlayer.getId()) {
                            mainFrame.showChuPaiJButton(true);
                            mainFrame.prevPlayerId = -1;
                        } else {
                            mainFrame.chuPaiJButton.setVisible(false);
                            mainFrame.buChuJButton.setVisible(false);
                            mainFrame.chaJButton.setVisible(false);
                            mainFrame.gouJButton.setVisible(false);
                            mainFrame.guanShangJButton.setVisible(false);
                            mainFrame.timeLabel.setVisible(false);
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
