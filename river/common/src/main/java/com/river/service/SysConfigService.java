package com.river.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.river.entity.SysConfig;

import java.util.List;

/**
 * 参数配置表 服务接口
 */
public interface SysConfigService extends IService<SysConfig> {
    /**
     * 查询参数配置表分页列表
     */
    IPage<SysConfig> selectPage(SysConfig sysConfig);

    /**
     * 查询参数配置表列表
     */
    List<SysConfig> selectList(SysConfig sysConfig);

    /**
     * 新增参数配置表
     */
    boolean insert(SysConfig sysConfig);

    /**
     * 修改参数配置表
     */
    SysConfig update(SysConfig sysConfig);

    /**
     * 批量删除参数配置表
     */
    boolean deleteByIds(List<Long> ids);

    /**
     * 根据key获取参数配置详情
     * @param key
     * @return
     */
    SysConfig selectConfigByKey(String key);
}
