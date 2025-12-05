package com.river;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@MapperScan("com.river.mapper")
@EnableFileStorage
//@EnableScheduling
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
        System.out.println("River 管理系统启动成功~~");
        System.out.println("Swagger 文档地址: http://localhost:8080/swagger-ui/index.html");
        System.out.println("接口文档地址: http://localhost:8080/doc.html");
    }

}