package com.mojian.utils;

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: quequnlong
 * @date: 2025/2/17
 * @description:
 */
@Component
public class AiUtil {

    @Value("${ai.apiKey}")
    private String apiKey;

    @Value("${ai.baseUrl}")
    private String baseUrl;

    @Value("${ai.model}")
    private String model;

    private ArkService service = null;

    //这里可以初始化AI的角色
    public final List<ChatMessage> messages = new ArrayList<>(Collections.singletonList(ChatMessage.builder()
            .role(ChatMessageRole.SYSTEM).content("你是小韦，是一个AI助手，可以帮助用户生成文本，代码等").build()));


    private void initService(){
        if (service == null){
            service = ArkService.builder()
                    .apiKey(apiKey)
                    .timeout(Duration.ofMinutes(30)) //因为deep-seek深度搜索时间较长，所以设置较长的等待时间
                    .baseUrl(baseUrl)
                    .build();
        }
    }



    public String send(String content) {
        initService();
        // 初始化消息列表
//        List<ChatMessage> messages = new ArrayList<>();
        // 创建用户消息
        ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER) // 设置消息角色为用户
                .content(content) // 设置消息内容
                .build();
        // 将用户消息添加到消息列表
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .build();

        // 获取响应并收集每个选择的消息内容
        List<Object> responses = new ArrayList<>();

        // 发送聊天完成请求并打印响应
        try {
            // 获取响应并打印每个选择的消息内容
            service.createChatCompletion(chatCompletionRequest)
                    .getChoices()
                    .forEach(choice -> {
                        // 校验是否触发了深度思考，打印思维链内容
                        if (choice.getMessage().getReasoningContent() != null) {
                            System.out.println("推理内容: " + choice.getMessage().getReasoningContent());
                        } else {
                            System.out.println("推理内容为空");
                        }
                        // 打印消息内容
                        System.out.println("消息内容: " + choice.getMessage().getContent());
                        responses.add(choice.getMessage().getContent());
                    });
        } catch (Exception e) {
            System.out.println("请求失败: " + e.getMessage());
        } finally {
            // 关闭服务执行器
            service.shutdownExecutor();
        }
        return responses.get(0).toString();
    }
}
