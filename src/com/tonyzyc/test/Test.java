package com.tonyzyc.test;

import com.tonyzyc.model.Poker;
import com.tonyzyc.model.PokerLabel;
import com.tonyzyc.thread.ReceiveThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<Poker> allPokers = createPokers(3);
        List<PokerLabel> labels = new ReceiveThread().getPokerLabelFromPoker(allPokers);
//        List<PokerLabel> labels = new ArrayList<>();
//        labels.add(new PokerLabel(30, "Clubs 4", "Black", 4, false));
//        labels.add(new PokerLabel(16, "Hearts 3", "Red", 3, true));
        Collections.sort(labels, Collections.reverseOrder());
        for (PokerLabel p: labels) {
            System.out.println(p);
        }
    }

    public static List<Poker> createPokers(int hunNum) {
        List<Poker> allPokers = new ArrayList<>();
        String[] names = new String[] {"3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2"};
        String[] suits = new String[] {"Spades", "Hearts", "Clubs", "Diamonds"};
        // red and black poker
        for (int i = 0; i < 1; i++) {
            Poker redPoker = new Poker(1, "Red Joker", "Red", 17, false);
            Poker blackPoker = new Poker(2, "Black Joker", "Black", 16, false);
            allPokers.add(redPoker);
            allPokers.add(blackPoker);
            int id = 3;
            for (String suit : suits) {
                int num = 3;
                for (String name : names) {
                    String color = (suit.equals("Hearts") || suit.equals("Diamonds")) ? "Red" : "Black";
                    Poker poker = new Poker(id++, suit + " " + name, color, num, num == hunNum);
                    num++;
                    allPokers.add(poker);
                }
            }
        }
        Collections.shuffle(allPokers);
        return allPokers;
    }
}
