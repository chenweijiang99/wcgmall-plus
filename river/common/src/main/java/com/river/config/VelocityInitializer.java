package com.river.config;

import com.river.common.Constants;
import org.apache.velocity.app.Velocity;

import java.util.Properties;

public class VelocityInitializer {
    
    public static void initVelocity() {
        Properties p = new Properties();
        try {
            // 设置velocity资源加载方式为class
            p.setProperty("resource.loaders", "class");
            // 设置velocity资源加载方式为file时的处理类
            p.setProperty("resource.loader.class.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            // 定义字符集
            p.setProperty(Velocity.ENCODING_DEFAULT, Constants.UTF8);
            p.setProperty(Velocity.INPUT_ENCODING, Constants.UTF8);
            // 初始化Velocity引擎，指定配置Properties
            Velocity.init(p);
        } catch (Exception e) {
            throw new RuntimeException("初始化Velocity引擎失败", e);
        }
    }
} 