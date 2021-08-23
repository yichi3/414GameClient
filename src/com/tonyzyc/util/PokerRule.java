package com.tonyzyc.util;

import com.tonyzyc.model.PokerLabel;

import java.util.Collections;
import java.util.List;

public class PokerRule {
    public static PokerType checkPokerType(List<PokerLabel> list) {
        Collections.sort(list);
        int size = list.size();
        if (size == 1) {
            return PokerType.p_1;
        } else if (size == 2) {
            if (isSame(list, size)) {
                return PokerType.p_2;
            } else {
                return PokerType.p_error;
            }
        }
        return PokerType.p_1;
    }

    private static boolean isSame(List<PokerLabel> list, int size) {
        // check if all poker in the list are the same
        return true;
    }

    public static boolean isBigger(List<PokerLabel> prev, List<PokerLabel> cur) {
        return true;
    }
}
