package com.river.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.vo.cache.*;

public interface CacheService {
    
    /**
     * 获取缓存基本信息
     */
    CacheInfoVo getCacheInfo();
    
    /**
     * 获取内存信息
     */
    CacheMemoryVo getMemoryInfo();
    
    /**
     * 获取缓存键列表
     */
    IPage<CacheKeyVo> getKeyList(CacheKeyQuery query);
    
    /**
     * 清空缓存
     */
    void clearCache();

    /**
     * 获取缓存详情
     */
    Object getCacheValue(String key);

    /**
     * 删除单个缓存键
     */
    void deleteKey(String key);
} 