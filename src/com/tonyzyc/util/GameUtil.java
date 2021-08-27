package com.tonyzyc.util;

import com.tonyzyc.model.PokerLabel;

public class GameUtil {
    public static void move(PokerLabel pokerLabel, int x, int y, boolean isAnimated) {
        pokerLabel.setLocation(x, y);
        try {
            // used for animation
            if (isAnimated){
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
