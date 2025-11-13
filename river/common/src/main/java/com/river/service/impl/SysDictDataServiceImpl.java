package com.river.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.river.common.Constants;
import com.river.entity.SysDict;
import com.river.entity.SysDictData;
import com.river.mapper.SysDictDataMapper;
import com.river.mapper.SysDictMapper;
import com.river.service.SysDictDataService;
import com.river.utils.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字典数据表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    private final SysDictMapper dictMapper;

    @Override
    public IPage<SysDictData> listDictData(Long dictId) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(SysDictData::getDictId,dictId)
                .orderByAsc(SysDictData::getSort);
        return page(PageUtil.getPage(), wrapper);
    }

    @Override
    public void addDictData(SysDictData sysDictData) {
        save(sysDictData);
    }

    @Override
    public void updateDictData(SysDictData sysDictData) {
        updateById(sysDictData);
    }

    @Override
    public Map<String, Map<String, Object>> getDictDataByDictType(List<String> dictTypes) {
        Map<String, Map<String, Object>> map = new HashMap<>();

        List<SysDict> dictList = dictMapper.selectList(new LambdaQueryWrapper<SysDict>().in(SysDict::getType,dictTypes)
                .eq(SysDict::getStatus, Constants.YES));
        dictList.forEach(item ->{
            LambdaQueryWrapper<SysDictData> sysDictDataQueryWrapper = new LambdaQueryWrapper<SysDictData>();
            sysDictDataQueryWrapper.eq(SysDictData::getStatus,Constants.YES);
            sysDictDataQueryWrapper.eq(SysDictData::getDictId, item.getId());
            sysDictDataQueryWrapper.orderByAsc(SysDictData::getSort);
            List<SysDictData> dataList = baseMapper.selectList(sysDictDataQueryWrapper);
            String defaultValue = null;
            for (SysDictData dictData : dataList) {
                //选取默认值
                if (Constants.YES == dictData.getIsDefault()){
                    defaultValue = dictData.getValue();
                    break;
                }
            }
            Map<String, Object> result = new HashMap<>();
            result.put("defaultValue",defaultValue);
            result.put("list",dataList);
            map.put(item.getType(),result);
        });
        return map;
    }

    @Override
    @Cacheable(cacheNames = "sys_dict", key = "#dictType")
    public List<SysDictData> selectDataByDictTypeCache(String dictType) {
        return baseMapper.selectDataByDictType(dictType);
    }
}
