package com.river.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.river.service.CacheService;
import com.river.vo.cache.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public CacheInfoVo getCacheInfo() {
        Properties info = redisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);

        CacheInfoVo vo = new CacheInfoVo();
        vo.setVersion(info.getProperty("redis_version"));
        vo.setMode(info.getProperty("redis_mode"));
        vo.setPort(info.getProperty("tcp_port"));

        String uptimeInSeconds = info.getProperty("uptime_in_seconds");
        long days = Long.parseLong(uptimeInSeconds) / (24 * 3600);
        vo.setUptime(String.valueOf(days));

        // 客户端连接数
        vo.setClients(info.getProperty("connected_clients"));

        // 内存信息
        vo.setUsedMemory(formatBytes(Long.parseLong(info.getProperty("used_memory"))));
        String maxMemory = info.getProperty("maxmemory");
        // 修复空指针异常：添加空值检查
        vo.setMaxmemory(maxMemory == null || maxMemory.equals("0") ? "不限制" : formatBytes(Long.parseLong(maxMemory)));


        // 持久化信息
        vo.setAofEnabled("1".equals(info.getProperty("aof_enabled")) ? "是" : "否");
        vo.setRdbLastSaveStatus("ok".equals(info.getProperty("rdb_last_bgsave_status")) ? "成功" : "失败");

        // 获取所有数据库的key总数
        Long dbSize = redisTemplate.execute(RedisServerCommands::dbSize);
        vo.setKeys(String.valueOf(dbSize));

        // 网络信息
        vo.setInstantaneousInputKbps(info.getProperty("instantaneous_input_kbps") + " kbps");
        vo.setInstantaneousOutputKbps(info.getProperty("instantaneous_output_kbps") + " kbps");

        return vo;
    }

    /**
     * 格式化字节大小
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int unit = 1024;
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
    }

    @Override
    public CacheMemoryVo getMemoryInfo() {
        Properties info = redisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);

        CacheMemoryVo vo = new CacheMemoryVo();
        vo.setUsed(Long.parseLong(info.getProperty("used_memory")));
        vo.setTotal(Runtime.getRuntime().maxMemory());
        return vo;
    }

    @Override
    public IPage<CacheKeyVo> getKeyList(CacheKeyQuery query) {
        IPage<CacheKeyVo> page = new Page<>();
        Set<String> keys = redisTemplate.keys(query.getKey() == null ? "*" : "*" + query.getKey() + "*");
        if (keys == null || keys.isEmpty()) {
            page.setTotal(0);
            page.setRecords(Collections.emptyList());
            return page;
        }

        List<CacheKeyVo> list = keys.stream().map(key -> {
            CacheKeyVo vo = new CacheKeyVo();
            vo.setKey(key);
            vo.setType(Objects.requireNonNull(redisTemplate.type(key)).name());
            vo.setSize(getKeySize(key));
            vo.setTtl(redisTemplate.getExpire(key));
            return vo;
        }).collect(Collectors.toList());

        // 分页处理
        int start = (query.getPageNum() - 1) * query.getPageSize();
        int end = Math.min(start + query.getPageSize(), list.size());
        page.setRecords(list.subList(start, end));
        page.setTotal(list.size());
        return page;
    }

    @Override
    public void clearCache() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public Object getCacheValue(String key) {
        try {
            // 尝试直接获取值
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            // 如果反序列化失败，尝试获取原始字节并转换为字符串
            try {
                byte[] rawValue = redisTemplate.execute((RedisCallback<byte[]>) connection ->
                    connection.stringCommands().get(key.getBytes())
                );
                if (rawValue != null) {
                    // 尝试转换为UTF-8字符串
                    String strValue = new String(rawValue, "UTF-8");
                    // 如果包含不可打印字符，返回提示信息
                    if (strValue.chars().anyMatch(c -> c < 32 && c != 9 && c != 10 && c != 13)) {
                        return "[二进制数据，无法直接显示。数据长度: " + rawValue.length + " 字节]";
                    }
                    return strValue;
                }
            } catch (Exception ex) {
                return "[无法读取缓存内容: " + ex.getMessage() + "]";
            }
            return "[缓存值为空或无法解析]";
        }
    }

    @Override
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    private Long getKeySize(String key) {
        try {
            return redisTemplate.execute((RedisCallback<Long>) connection ->
                    connection.stringCommands().strLen(key.getBytes()));
        } catch (Exception e) {
            return 0L;
        }
    }
}