package com.river.quartz;

import jakarta.annotation.PostConstruct;
import org.apache.velocity.app.Velocity;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class VelocityInitializer {

    @PostConstruct
    public void init() {
        Properties p = new Properties();
        try {
            // 加载classpath目录下的vm文件
            p.setProperty("resource.loaders", "class");
            p.setProperty("resource.loader.class.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            // 定义字符集
            p.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
            p.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
            p.setProperty("output.encoding", "UTF-8");
            // 初始化Velocity引擎，指定配置Properties
            Velocity.init(p);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 