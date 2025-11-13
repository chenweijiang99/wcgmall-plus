package com.river.service.impl;

import java.util.List;

import cn.dev33.satoken.stp.StpUtil;
import com.river.exception.ServiceException;
import org.springframework.stereotype.Service;
import com.river.mapper.AddressBookMapper;
import com.river.entity.AddressBook;
import com.river.service.AddressBookService;
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
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    /**
     * 查询分页列表
     */
    @Override
    public IPage<AddressBook> selectPage(AddressBook addressBook) {
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(addressBook.getId() != null, AddressBook::getId, addressBook.getId());
        wrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId());
        wrapper.eq(addressBook.getConsignee() != null, AddressBook::getConsignee, addressBook.getConsignee());
        wrapper.eq(addressBook.getConsigneeAddress() != null, AddressBook::getConsigneeAddress, addressBook.getConsigneeAddress());
        wrapper.eq(addressBook.getConsigneePhone() != null, AddressBook::getConsigneePhone, addressBook.getConsigneePhone());
        wrapper.eq(addressBook.getIsDefault() != null, AddressBook::getIsDefault, addressBook.getIsDefault());
        wrapper.eq(addressBook.getCreateTime() != null, AddressBook::getCreateTime, addressBook.getCreateTime());
        wrapper.eq(addressBook.getUpdateTime() != null, AddressBook::getUpdateTime, addressBook.getUpdateTime());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询列表
     */
    @Override
    public List<AddressBook> selectList(AddressBook addressBook) {
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(addressBook.getId() != null, AddressBook::getId, addressBook.getId());
        wrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId());
        wrapper.eq(addressBook.getConsignee() != null, AddressBook::getConsignee, addressBook.getConsignee());
        wrapper.eq(addressBook.getConsigneeAddress() != null, AddressBook::getConsigneeAddress, addressBook.getConsigneeAddress());
        wrapper.eq(addressBook.getConsigneePhone() != null, AddressBook::getConsigneePhone, addressBook.getConsigneePhone());
        wrapper.eq(addressBook.getIsDefault() != null, AddressBook::getIsDefault, addressBook.getIsDefault());
        wrapper.eq(addressBook.getCreateTime() != null, AddressBook::getCreateTime, addressBook.getCreateTime());
        wrapper.eq(addressBook.getUpdateTime() != null, AddressBook::getUpdateTime, addressBook.getUpdateTime());
        return list(wrapper);
    }

    /**
     * 新增
     */
    @Override
    public boolean insert(AddressBook addressBook) {
        return save(addressBook);
    }

    /**
     * 修改
     */
    @Override
    public boolean update(AddressBook addressBook) {
        return updateById(addressBook);
    }

    /**
     * 批量删除
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public List<AddressBook> userSelectList() {
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId, StpUtil.getLoginIdAsLong());
        return list(wrapper);
    }

    @Override
    public boolean userInsert(AddressBook addressBook) {
        addressBook.setUserId(StpUtil.getLoginIdAsLong());
        return save(addressBook);
    }

    @Override
    public boolean userUpdate(AddressBook addressBook) {
        AddressBook oldAddressBook = getById(addressBook.getId());
        if (oldAddressBook == null) {
            throw new ServiceException("地址不存在");
        }
        if (!oldAddressBook.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            throw new ServiceException("无权限");
        }
        return updateById(addressBook);
    }

    @Override
    public boolean userDeleteById(Long id) {
        AddressBook oldAddressBook = getById(id);
        if (oldAddressBook == null) {
            throw new ServiceException("地址不存在");
        }
        if (!oldAddressBook.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            throw new ServiceException("无权限");
        }
        return removeById(id);
    }
}
