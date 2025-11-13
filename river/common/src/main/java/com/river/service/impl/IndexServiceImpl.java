package com.river.service.impl;

import com.river.mapper.SysRoleMapper;
import com.river.mapper.SysUserMapper;
import com.river.service.IndexService;
import com.river.vo.dashboard.IndexVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public IndexVo index() {
        Long userCount = sysUserMapper.selectCount(null);
        Long roleCount = sysRoleMapper.selectCount( null);

        return IndexVo.builder()
                .userCount(userCount)
                .roleCount(roleCount)
                .build();
    }

}
