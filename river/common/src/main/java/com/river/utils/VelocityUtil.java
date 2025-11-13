package com.river.utils;

import com.river.entity.GenTable;
import com.river.entity.GenTableColumn;
import org.apache.velocity.VelocityContext;

import java.util.List;

/**
 * Velocity模板工具类
 *
 * @author neat
 */
public class VelocityUtil {

    public static String packageName = "com.river";


    public static VelocityContext prepareContext(GenTable genTable, List<GenTableColumn> columns) {
        VelocityContext context = new VelocityContext();

        String moduleName = genTable.getModuleName();
        String businessName = getBusinessName(genTable.getTableName());
        String className = getClassName(genTable.getTableName());

        context.put("tableName", genTable.getTableName());
        context.put("tableComment", genTable.getTableComment());
        context.put("primaryKey", genTable.getPkColumn());
        context.put("className", className);
        context.put("classname", uncapitalize(className));
        context.put("moduleName", moduleName);
        context.put("BusinessName", capitalize(businessName));
        context.put("businessName", businessName);
        context.put("basePackage", packageName);
        context.put("packageName", packageName);
        context.put("author", "neat");
        context.put("datetime", DateUtil.getDate());
        context.put("columns", columns);

        return context;
    }

    public static String getBusinessName(String tableName) {
        String businessName = convertToCamelCase(tableName);
        return uncapitalize(businessName);
    }

    /**
     * 获取类名
     */
    public static String getClassName(String tableName) {
        String className = convertToCamelCase(tableName);
        return capitalize(className);
    }

    /**
     * 将表名转换为Java类名
     */
    public static String convertClassName(String tableName) {
        return convertToCamelCase(tableName);
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式。
     * 如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。
     * 例如：HELLO_WORLD->HelloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String convertToCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        // 快速检查
        if (name == null || name.isEmpty()) {
            // 没必要转换
            return "";
        } else if (!name.contains("_")) {
            // 不含下划线，仅将首字母大写
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }

        // 用下划线将原始字符串分割
        String[] camels = name.split("_");
        for (String camel : camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty()) {
                continue;
            }
            // 处理真正的驼峰片段
            result.append(camel.substring(0, 1).toUpperCase());
            result.append(camel.substring(1).toLowerCase());
        }
        return result.toString();
    }



    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String uncapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

}
