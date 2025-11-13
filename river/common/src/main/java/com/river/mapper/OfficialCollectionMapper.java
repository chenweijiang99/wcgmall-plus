package com.river.mapper;

import com.river.entity.OfficialCollection;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 *  Mapper接口
 */
@Mapper
public interface OfficialCollectionMapper extends BaseMapper<OfficialCollection> {
     List<OfficialCollection> list();
}