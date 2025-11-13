package com.river.quartz;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("task")
@RequiredArgsConstructor
public class TaskQuartz {

    public void runMultipleParams(String s, Boolean b, Long l, Double d, Integer i) {
          System.out.println(String.format("执行多参方法： 字符串类型%s，布尔类型%s，长整型%s，浮点型%s，整形%s", s, b, l, d, i));
    }
    public void runParams(String params) {
        System.out.println("执行有参方法：" + params);
    }
    public void runNoParams() {
        System.out.println("执行无参方法");
    }
}
