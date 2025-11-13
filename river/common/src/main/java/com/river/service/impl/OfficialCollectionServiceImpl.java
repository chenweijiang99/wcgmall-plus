package com.river.service.impl;

import java.util.List;

import com.river.common.RedisConstants;
import com.river.utils.RedisUtil;
import org.springframework.stereotype.Service;
import com.river.mapper.OfficialCollectionMapper;
import com.river.entity.OfficialCollection;
import com.river.service.OfficialCollectionService;
import com.river.utils.PageUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;

/**
 *  服务实现类
 */
@Service
@RequiredArgsConstructor
public class OfficialCollectionServiceImpl extends ServiceImpl<OfficialCollectionMapper, OfficialCollection> implements OfficialCollectionService {
        private final RedisUtil redisUtil;

    /**
     * 查询分页列表
     */
    @Override
    public IPage<OfficialCollection> selectPage(OfficialCollection officialCollection) {
        LambdaQueryWrapper<OfficialCollection> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(officialCollection.getId() != null, OfficialCollection::getId, officialCollection.getId());
        wrapper.eq(officialCollection.getProductId() != null, OfficialCollection::getProductId, officialCollection.getProductId());
        wrapper.eq(officialCollection.getCreateTime() != null, OfficialCollection::getCreateTime, officialCollection.getCreateTime());
        wrapper.eq(officialCollection.getUpdateTime() != null, OfficialCollection::getUpdateTime, officialCollection.getUpdateTime());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询列表
     */
    @Override
    public List<OfficialCollection> selectList(OfficialCollection officialCollection) {
        LambdaQueryWrapper<OfficialCollection> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(officialCollection.getId() != null, OfficialCollection::getId, officialCollection.getId());
        wrapper.eq(officialCollection.getProductId() != null, OfficialCollection::getProductId, officialCollection.getProductId());
        wrapper.eq(officialCollection.getCreateTime() != null, OfficialCollection::getCreateTime, officialCollection.getCreateTime());
        wrapper.eq(officialCollection.getUpdateTime() != null, OfficialCollection::getUpdateTime, officialCollection.getUpdateTime());
        return list(wrapper);
    }

    /**
     * 新增
     */
    @Override
    public boolean insert(Long productId) {
        OfficialCollection officialCollection = new OfficialCollection();
        officialCollection.setProductId(productId);
        if(redisUtil.hasKey(RedisConstants.OFFICIAL_COLLECTION_LIST)){
            redisUtil.delete(RedisConstants.OFFICIAL_COLLECTION_LIST);
        }
        save(officialCollection);
        redisUtil.set(RedisConstants.OFFICIAL_COLLECTION_LIST, baseMapper.list());
        return true;
    }

    /**
     * 修改
     */
    @Override
    public boolean update(OfficialCollection officialCollection) {
        if(redisUtil.hasKey(RedisConstants.OFFICIAL_COLLECTION_LIST)){
            redisUtil.delete(RedisConstants.OFFICIAL_COLLECTION_LIST);
        }
        updateById(officialCollection);
        redisUtil.set(RedisConstants.OFFICIAL_COLLECTION_LIST, baseMapper.list());
        return true;
    }

    /**
     * 批量删除
     */
    @Override
    public boolean deleteByIds(Long productId) {
        LambdaQueryWrapper<OfficialCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OfficialCollection::getProductId, productId);
        if(redisUtil.hasKey(RedisConstants.OFFICIAL_COLLECTION_LIST)){
            redisUtil.delete(RedisConstants.OFFICIAL_COLLECTION_LIST);
        }
        remove(wrapper);
        if(!list().isEmpty()){
            redisUtil.set(RedisConstants.OFFICIAL_COLLECTION_LIST, baseMapper.list());
        }
        return true;
    }

    @Override
    public List<OfficialCollection> getAll() {
        if(redisUtil.hasKey(RedisConstants.OFFICIAL_COLLECTION_LIST)){
            return (List<OfficialCollection>) redisUtil.get(RedisConstants.OFFICIAL_COLLECTION_LIST);
        }
        return baseMapper.list();
    }
}
