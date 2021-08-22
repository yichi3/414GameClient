package com.tonyzyc.model;

public class Poker {
    private int id;
    private String name;
    private int num;
    private boolean isOut;
    private boolean isHun;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public boolean isOut() {
        return isOut;
    }

    public void setOut(boolean out) {
        isOut = out;
    }

    public boolean isHun() {
        return isHun;
    }

    public void setHun(boolean hun) {
        isHun = hun;
    }

    public Poker() {
    }

    public Poker(int id, String name, int num) {
        this.id = id;
        this.name = name;
        this.num = num;
        // default set poker 3 to be the hun
        this.isHun = name.split(" ")[1].equals("3");
    }

    public Poker(int id, String name, int num, boolean isOut) {
        this.id = id;
        this.name = name;
        this.num = num;
        this.isOut = isOut;
        // default set poker 3 to be the hun
        this.isHun = name.split(" ")[1].equals("3");
    }
}
