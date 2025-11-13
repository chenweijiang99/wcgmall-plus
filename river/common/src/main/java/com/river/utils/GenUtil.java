package com.river.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class GenUtil {
    private static final Map<String, String> javaTypeMap = new HashMap<>();

    static {
        javaTypeMap.put("tinyint", "Integer");
        javaTypeMap.put("smallint", "Integer");
        javaTypeMap.put("mediumint", "Integer");
        javaTypeMap.put("int", "Integer");
        javaTypeMap.put("integer", "Integer");
        javaTypeMap.put("bigint", "Long");
        javaTypeMap.put("float", "Float");
        javaTypeMap.put("double", "Double");
        javaTypeMap.put("decimal", "BigDecimal");
        javaTypeMap.put("char", "String");
        javaTypeMap.put("varchar", "String");
        javaTypeMap.put("text", "String");
        javaTypeMap.put("datetime", "LocalDateTime");
        javaTypeMap.put("date", "LocalDateTime");
    }

    public static String getJavaType(String dbType) {
        int index = dbType.indexOf("(");
        if (index > 0) {
            dbType = dbType.substring(0, index);
        }
        return javaTypeMap.getOrDefault(dbType.toLowerCase(), "String");
    }

    public static String toCamelCase(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        str = str.toLowerCase();
        StringBuilder sb = new StringBuilder();
        boolean upperCase = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
