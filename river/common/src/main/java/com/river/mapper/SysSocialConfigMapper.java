package com.river.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.river.entity.SysSocialConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 第三方登录配置表 Mapper接口
 */
@Mapper
public interface SysSocialConfigMapper extends BaseMapper<SysSocialConfig> {
}
