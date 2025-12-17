package com.river.utils;

import java.util.*;

/**
 * 敏感词过滤工具类
 * 使用DFA算法实现高效敏感词过滤
 */
public class SensitiveWordFilter {

    // 敏感词库
    private static final Set<String> SENSITIVE_WORDS = new HashSet<>();

    // DFA算法的敏感词树
    private static Map<Character, Object> sensitiveWordMap = new HashMap<>();

    // 初始化敏感词库
    static {
        // 添加一些常见敏感词（实际使用时可从数据库或配置文件加载）
        String[] words = {
                "傻逼", "操你", "妈的", "草泥马", "狗日的", "王八蛋",
                "去死", "滚蛋", "白痴", "智障", "脑残",
                "骗子", "诈骗", "传销", "赌博",
                "色情", "黄色", "裸体",
                "毒品", "吸毒", "贩毒"
        };
        Collections.addAll(SENSITIVE_WORDS, words);
        initSensitiveWordMap();
    }

    /**
     * 初始化敏感词树
     */
    @SuppressWarnings("unchecked")
    private static void initSensitiveWordMap() {
        sensitiveWordMap = new HashMap<>();
        for (String word : SENSITIVE_WORDS) {
            Map<Character, Object> currentMap = sensitiveWordMap;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                Object obj = currentMap.get(c);
                if (obj == null) {
                    Map<Character, Object> newMap = new HashMap<>();
                    newMap.put('$', false); // 标记是否为词尾
                    currentMap.put(c, newMap);
                    currentMap = newMap;
                } else {
                    currentMap = (Map<Character, Object>) obj;
                }
                if (i == word.length() - 1) {
                    currentMap.put('$', true); // 标记词尾
                }
            }
        }
    }

    /**
     * 检查文本是否包含敏感词
     */
    public static boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文本中的敏感词列表
     */
    public static List<String> getSensitiveWords(String text) {
        List<String> sensitiveWords = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return sensitiveWords;
        }
        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                sensitiveWords.add(text.substring(i, i + length));
                i = i + length - 1;
            }
        }
        return sensitiveWords;
    }

    /**
     * 替换敏感词为指定字符
     */
    public static String replaceSensitiveWord(String text, char replaceChar) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder result = new StringBuilder(text);
        for (int i = 0; i < result.length(); i++) {
            int length = checkSensitiveWord(result.toString(), i);
            if (length > 0) {
                for (int j = i; j < i + length; j++) {
                    result.setCharAt(j, replaceChar);
                }
                i = i + length - 1;
            }
        }
        return result.toString();
    }

    /**
     * 替换敏感词为星号
     */
    public static String replaceSensitiveWord(String text) {
        return replaceSensitiveWord(text, '*');
    }

    /**
     * 检查敏感词，返回敏感词长度
     */
    @SuppressWarnings("unchecked")
    private static int checkSensitiveWord(String text, int beginIndex) {
        Map<Character, Object> currentMap = sensitiveWordMap;
        int matchLength = 0;
        int tempLength = 0;

        for (int i = beginIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            currentMap = (Map<Character, Object>) currentMap.get(c);
            if (currentMap == null) {
                break;
            }
            tempLength++;
            if ((Boolean) currentMap.get('$')) {
                matchLength = tempLength;
            }
        }
        return matchLength;
    }

    /**
     * 动态添加敏感词
     */
    @SuppressWarnings("unchecked")
    public static void addSensitiveWord(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        SENSITIVE_WORDS.add(word);
        Map<Character, Object> currentMap = sensitiveWordMap;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Object obj = currentMap.get(c);
            if (obj == null) {
                Map<Character, Object> newMap = new HashMap<>();
                newMap.put('$', false);
                currentMap.put(c, newMap);
                currentMap = newMap;
            } else {
                currentMap = (Map<Character, Object>) obj;
            }
            if (i == word.length() - 1) {
                currentMap.put('$', true);
            }
        }
    }

    /**
     * 批量添加敏感词
     */
    public static void addSensitiveWords(Collection<String> words) {
        for (String word : words) {
            addSensitiveWord(word);
        }
    }
}
