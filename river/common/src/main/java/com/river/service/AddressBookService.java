package com.river.service;

import com.river.entity.AddressBook;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

/**
 *  服务接口
 */
public interface AddressBookService extends IService<AddressBook> {
    /**
     * 查询分页列表
     */
    IPage<AddressBook> selectPage(AddressBook addressBook);

    /**
     * 查询列表
     */
    List<AddressBook> selectList(AddressBook addressBook);

    /**
     * 新增
     */
    boolean insert(AddressBook addressBook);

    /**
     * 修改
     */
    boolean update(AddressBook addressBook);

    /**
     * 批量删除
     */
    boolean deleteByIds(List<Long> ids);

    List<AddressBook> userSelectList();

    boolean userInsert(AddressBook addressBook);

    boolean userUpdate(AddressBook addressBook);

    boolean userDeleteById(Long id);
}
