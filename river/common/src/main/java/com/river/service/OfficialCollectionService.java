package com.river.service;

import com.river.entity.OfficialCollection;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 *  服务接口
 */
public interface OfficialCollectionService extends IService<OfficialCollection> {
    /**
     * 查询分页列表
     */
    IPage<OfficialCollection> selectPage(OfficialCollection officialCollection);

    /**
     * 查询列表
     */
    List<OfficialCollection> selectList(OfficialCollection officialCollection);

    /**
     * 新增
     */
    boolean insert(Long productId);

    /**
     * 修改
     */
    boolean update(OfficialCollection officialCollection);

    /**
     * 批量删除
     */
    boolean deleteByIds(Long productId);
    /**
     * 获取所有官方收藏
     */
    List<OfficialCollection> getAll();
}
