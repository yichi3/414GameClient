package com.tonyzyc.model;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class PokerLabel extends JLabel implements Comparable {
    private int id;
    private String name;
    private String color;
    private int num;
    private boolean isOut;
    private boolean isUp;
    // is selected by player
    private boolean isSelected;
    private boolean isHun;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
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

    public boolean isUp() {
        return isUp;
    }

    public void setUp(boolean up) {
        isUp = up;
    }

    public boolean isHun() {
        return isHun;
    }

    public void setHun(boolean hun) {
        isHun = hun;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "PokerLabel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", num=" + num +
                ", isOut=" + isOut +
                ", isUp=" + isUp +
                ", isSelected=" + isSelected +
                ", isHun=" + isHun +
                '}';
    }

    public PokerLabel() {
        this.setSize(105, 150);
    }

    public PokerLabel(PokerLabel other) {
        this.id = other.id;
        this.name = other.name;
        this.color = other.color;
        this.num = other.num;
        this.isHun = other.isHun;
        this.isOut = other.isOut;
        this.isUp = other.isUp;
        if (isUp) {
            turnUp();
        } else {
            turnDown();
        }
        this.setSize(105, 150);
    }

    public PokerLabel(int id, String name, String color, int num, boolean isHun) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.num = num;
        this.isHun = isHun;
        this.setSize(105, 150);
    }

    public PokerLabel(int id, String name, String color, int num, boolean isOut, boolean isUp, boolean isHun) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.num = num;
        this.isOut = isOut;
        this.isUp = isUp;
        this.isHun = isHun;
        if (isUp) {
            turnUp();
        } else {
            turnDown();
        }
        this.setSize(105, 150);
    }

    public void turnUp() {
        this.setIcon(new ImageIcon("images/poker/" + id + ".jpg"));
    }

    public void turnDown() {
        this.setIcon(new ImageIcon("images/poker/down.png"));
    }

    @Override
    public int compareTo(Object o) {
        Map<String, Integer> suitMap = new HashMap<>();
        suitMap.put("Red", 5);
        suitMap.put("Black", 4);
        suitMap.put("Hearts", 4);
        suitMap.put("Diamonds", 3);
        suitMap.put("Spades", 2);
        suitMap.put("Clubs", 1);
        PokerLabel other = (PokerLabel) o;
        if (this.num != other.num) {
            if (!this.isHun && !other.isHun) {
                return this.num - other.num;
            } else if (this.isHun) {
                return other.num >= 16 ? -1 : 1;
            } else {
                return this.num < 16 ? -1 : 1;
            }
        } else {
            return suitMap.get(this.name.split(" ")[0]) - suitMap.get(other.name.split(" ")[0]);
        }
    }
}
