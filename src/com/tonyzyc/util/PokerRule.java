package com.tonyzyc.util;

import com.tonyzyc.model.PokerLabel;

import java.util.*;

public class PokerRule {
    public static PokerType checkPokerType(List<PokerLabel> list) {
        Collections.sort(list, Comparator.comparingInt(PokerLabel::getNum));
        int size = list.size();
        if (size == 1) {
            return PokerType.p_1;
        } else if (size == 2) {
            if (isSame(list, size)) {
                return PokerType.p_2;
            } else if (isWangzha(list)) {
                return PokerType.p_joker;
            } else {
                return PokerType.p_error;
            }
        } else if (size >= 3) {
            if (isSame(list, size)) {
                return PokerType.p_3plus;
            } else if (isDanShun(list)) {
                return PokerType.p_dan;
            } else if (isShuangShun(list)) {
                return PokerType.p_shuang;
            } else if (is414(list)) {
                return PokerType.p_414;
            } else if (isWangzha(list)) {
                return PokerType.p_joker;
            }
        }
        return PokerType.p_error;
    }

    private static boolean isSame(List<PokerLabel> list, int size) {
        // check if all poker in the list are the same
        PokerLabel firstPokerLabel = list.get(0);
        int num = firstPokerLabel.getNum();
        for (PokerLabel p: list) {
            if (num != p.getNum()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWangzha(List<PokerLabel> list) {
        // check if all poker is Joker
        for (PokerLabel p: list) {
            if (p.getNum() < 16) {
                return false;
            }
        }
        return true;
    }

    private static boolean isDanShun(List<PokerLabel> list) {
        int num = list.get(0).getNum();
        for (PokerLabel p: list) {
            System.out.println(p.getName() + " " + p.getNum());
            int curNum = p.getNum();
            if (curNum != num || curNum >= 15) {
                return false;
            }
            num++;
        }
        return true;
    }

    private static boolean isShuangShun(List<PokerLabel> list) {
        if (list.size() % 2 != 0 || list.size() < 6) {
            return false;
        }
        int num = list.get(0).getNum();
        for (int i = 0; i < list.size(); i++) {
            int curNum = list.get(i).getNum();
            if (curNum != num || curNum >= 15) {
                return false;
            }
            if (i % 2 == 1) {
                num++;
            }
        }
        return true;
    }

    private static boolean is414(List<PokerLabel> list) {
        if (list.size() != 3) {
            return false;
        }
        if (list.get(0).getNum() != 4 || list.get(1).getNum() != 4 || list.get(2).getNum() != 14) {
            return false;
        }
        return true;
    }

    private static boolean bigger414(List<PokerLabel> prev, List<PokerLabel> cur) {
        Map<String, Integer> suitMap = new HashMap<>();
        suitMap.put("Hearts", 4);
        suitMap.put("Diamonds", 3);
        suitMap.put("Spades", 2);
        suitMap.put("Clubs", 1);
        Set<String> prevSuit = new HashSet<>();
        Set<String> prevColor = new HashSet<>();
        Set<String> curSuit = new HashSet<>();
        Set<String> curColor = new HashSet<>();
        for (PokerLabel p: prev) {
            prevSuit.add(p.getName().split(" ")[0]);
            prevColor.add(p.getColor());
        }
        for (PokerLabel p: cur) {
            curSuit.add(p.getName().split(" ")[0]);
            curColor.add(p.getColor());
        }
        if (curSuit.size() == 1) {
            return prevSuit.size() >= 2 || suitMap.get(curSuit.iterator().next()) > suitMap.get(prevSuit.iterator().next());
        } else if (curColor.size() == 1 && curColor.iterator().next().equals("Red")) {
            return prevColor.size() >= 2 || prevColor.iterator().next().equals("Black");
        }
        return false;
    }

    public static boolean isBigger(List<PokerLabel> prev, List<PokerLabel> cur) {
        PokerType prevType = checkPokerType(prev);
        PokerType curType = checkPokerType(cur);
        if (prevType.equals(curType)) {
            if (curType.equals(PokerType.p_1) || curType.equals(PokerType.p_2)) {
                PokerLabel prevPoker = prev.get(0);
                PokerLabel curPoker = cur.get(0);
                if (!prevPoker.isHun() && !curPoker.isHun()) {
                    return curPoker.getNum() > prevPoker.getNum();
                } else if (prevPoker.isHun() && curPoker.isHun()) {
                    return false;
                } else {
                    return curPoker.isHun();
                }
            } else if (curType.equals(PokerType.p_joker)) {
                int prevSum = 0;
                int curSum = 0;
                for (PokerLabel p: prev) {
                    prevSum += p.getNum();
                }
                for (PokerLabel p: cur) {
                    curSum += p.getNum();
                }
                return curSum > prevSum;
            } else if (curType.equals(PokerType.p_3plus)) {
                if (prev.size() != cur.size()) {
                    return cur.size() > prev.size();
                } else {
                    if (!prev.get(0).isHun() && !cur.get(0).isHun()) {
                        return prev.get(0).getNum() > cur.get(0).getNum();
                    } else if (prev.get(0).isHun() && cur.get(0).isHun()) {
                        return false;
                    } else {
                        return cur.get(0).isHun();
                    }
                }
            } else if (curType.equals(PokerType.p_dan) || curType.equals(PokerType.p_shuang)) {
                if (prev.size() != cur.size()) {
                    return false;
                } else {
                    return cur.get(0).getNum() > prev.get(0).getNum();
                }
            } else {
                // prev and cur are both 414
                return bigger414(prev, cur);
            }
        } else {
            if (prevType.equals(PokerType.p_414) || curType.equals(PokerType.p_1) || curType.equals(PokerType.p_2) || curType.equals(PokerType.p_dan) || curType.equals(PokerType.p_shuang)) {
                return false;
            } else if (curType.equals(PokerType.p_joker)) {
                if (prevType.equals(PokerType.p_3plus)) {
                    return cur.size() * 2 >= prev.size();
                } else {
                    return true;
                }
            } else if (curType.equals(PokerType.p_3plus)) {
                if (prevType.equals(PokerType.p_joker)) {
                    return cur.size() > prev.size() * 2;
                } else if (prevType.equals(PokerType.p_shuang)) {
                    return cur.size() >= 4;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
    }

    public static boolean isCha(List<PokerLabel> prev, List<PokerLabel> cur) throws Exception {
        if (prev.size() != 1) {
            throw new Exception("Cannot 叉 here!");
        }
        if (cur.size() != 2) {
            return false;
        }
        return prev.get(0).getNum() == cur.get(0).getNum() && prev.get(0).getNum() == cur.get(1).getNum();
    }
}
