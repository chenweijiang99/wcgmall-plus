package com.river.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.river.entity.FileDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件记录表 Mapper接口
 */
@Mapper
public interface FileDetailMapper extends BaseMapper<FileDetail> {
}
