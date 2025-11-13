package com.river.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.river.common.Constants;
import com.river.common.RedisConstants;
import com.river.dto.user.SysUserAddAndUpdateDto;
import com.river.dto.user.UpdatePwdDTO;
import com.river.entity.SysUser;
import com.river.exception.ServiceException;
import com.river.mapper.SysRoleMapper;
import com.river.mapper.SysUserMapper;
import com.river.service.SysUserService;
import com.river.utils.PageUtil;
import com.river.utils.RedisUtil;
import com.river.vo.user.OnlineUserVo;
import com.river.vo.user.SysUserProfileVo;
import com.river.vo.user.SysUserVo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysRoleMapper roleMapper;
    private final RedisUtil redisUtil;
    private final SysUserMapper sysUserMapper;

    @Override
    public IPage<SysUserVo> listUsers(SysUser sysUser) {
        return baseMapper.selectUserPage(PageUtil.getPage(),sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysUserAddAndUpdateDto SysUserAddAndUpdateDto) {
        // 检查用户名是否已存在
        SysUser user = SysUserAddAndUpdateDto.getUser();
        if (baseMapper.selectByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(),BCrypt.gensalt()));
        if(user.getAvatar() == null) {
            user.setAvatar(Constants.DEFAULT_AVATAR);
        }

        save(user);

        //保存角色信息
        roleMapper.addRoleUser(user.getId(), SysUserAddAndUpdateDto.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUserAddAndUpdateDto user) {
        // 检查用户是否存在
        if (getById(user.getUser().getId()) == null) {
            throw new RuntimeException("用户不存在");
        }
        updateById(user.getUser());

        //修改角色 先删除角色再新增
        roleMapper.deleteRoleByUserId(Collections.singletonList(user.getUser().getId()));
        roleMapper.addRoleUser(user.getUser().getId(), user.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        removeBatchByIds(ids);
        roleMapper.deleteRoleByUserId(ids);
    }


    @Override
    public void updatePwd(UpdatePwdDTO updatePwdDTO) {

        SysUser user = this.getById(StpUtil.getLoginIdAsInt());
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        if(!StpUtil.hasRole(Constants.ADMIN) && user.getId() != StpUtil.getLoginIdAsLong()) {
            throw new ServiceException("只能修改自己的密码！");
        }

        if (!BCrypt.checkpw(updatePwdDTO.getOldPassword(), user.getPassword())) {
            throw new ServiceException("旧密码错误");
        }

        user.setPassword(BCrypt.hashpw(updatePwdDTO.getNewPassword(),BCrypt.gensalt()));
        this.updateById(user);
    }

    @Override
    public SysUserProfileVo profile() {

        SysUser sysUser = baseMapper.selectById(StpUtil.getLoginIdAsInt());
        sysUser.setPassword(null);
        //获取角色
        List<String> roles = roleMapper.selectRolesByUserId(sysUser.getId());

        return SysUserProfileVo.builder().sysUser(sysUser).roles(roles).build();
    }

    @Override
    public void updateProfile(SysUser user) {
        baseMapper.updateById(user);
    }

    @Override
    public Boolean verifyPassword(String password) {
        SysUser user = baseMapper.selectById(StpUtil.getLoginIdAsInt());
        return BCrypt.checkpw(password, user.getPassword());
    }

    @Override
    public Boolean resetPassword(SysUser user) {
        user.setPassword(BCrypt.hashpw(user.getPassword(),BCrypt.gensalt()));
        baseMapper.updateById(user);
        return true;
    }

    @Override
    public IPage<OnlineUserVo> getOnlineUserList(String username) {
        Integer pageNum = PageUtil.getPageQuery().getPageNum();
        Integer pageSize = PageUtil.getPageQuery().getPageSize();

        // 返回数据对象
        Collection<String> keys = redisUtil.keys(RedisConstants.LOGIN_TOKEN.concat( "*"));

        List<OnlineUserVo> totalList = new ArrayList<>();
        for (String key : keys) {
            Object userObj = redisUtil.get(key);
            OnlineUserVo onlineUser = JSONUtil.toBean(userObj.toString(), OnlineUserVo.class);
            if (StringUtils.isNotBlank(username)) {
                if (onlineUser.getUsername().contains(username)) {
                    totalList.add(onlineUser);
                }
                continue;
            }
            totalList.add(onlineUser);
        }

        //根据时间排序
        totalList.sort((o1, o2) -> o2.getLastLoginTime().compareTo(o1.getLastLoginTime()));

        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = totalList.size() - fromIndex > pageSize ? fromIndex + pageSize : totalList.size();
        List<OnlineUserVo> records = totalList.subList(fromIndex, toIndex);

        IPage<OnlineUserVo> page = new Page<>(pageNum, pageSize);
        page.setRecords(records);
        page.setTotal(totalList.size());
        return page;
    }

    @Override
    public SysUser userGetInfo() {
        SysUser sysUser = baseMapper.selectById(StpUtil.getLoginIdAsLong());
        sysUser.setPassword(null);
        return sysUser;
    }

    @Override
    public boolean userUpdate(SysUser user) {
        user.setId(StpUtil.getLoginIdAsLong());
        return updateById(user);
    }

}
