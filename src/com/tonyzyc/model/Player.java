package com.tonyzyc.model;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int id;
    private String playerUname;
    private Socket socket;
    private boolean isFirst;
    private List<Poker> pokers = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public List<Poker> getPokers() {
        return pokers;
    }

    public void setPokers(List<Poker> pokers) {
        this.pokers = pokers;
    }

    public String getPlayerUname() {
        return playerUname;
    }

    public void setPlayerUname(String playerUname) {
        this.playerUname = playerUname;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", playerUname='" + playerUname + '\'' +
                ", isFirst=" + isFirst +
                '}';
    }

    public Player() {}

    public Player(int id, String playerUname, Socket socket, List<Poker> pokers, boolean isFirst) {
        this.id = id;
        this.playerUname = playerUname;
        this.socket = socket;
        this.pokers = pokers;
        this.isFirst = isFirst;
    }

    public Player(int id, String playerUname, List<Poker> pokers, boolean isFirst) {
        this.id = id;
        this.playerUname = playerUname;
        this.pokers = pokers;
        this.isFirst = isFirst;
    }

}
