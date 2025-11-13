package com.river.controller;

import com.river.service.AddressBookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.river.entity.AddressBook;
import com.river.common.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/user/address")
@Slf4j
@Tag(name = "用户地址相关接口")
@RequiredArgsConstructor
public class AddressBookController {
    private final AddressBookService addressBookService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Result<List<AddressBook>> list() {
        return Result.success(addressBookService.userSelectList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取详情")
    public Result<AddressBook> getInfo(@PathVariable("id") Long id) {
        return Result.success(addressBookService.getById(id));
    }

    @PostMapping("/add")
    @Operation(summary = "添加")
    public Result<Object> add(@RequestBody AddressBook addressBook) {
        return Result.success(addressBookService.userInsert(addressBook));
    }

    @PutMapping("/update")
    @Operation(summary = "修改")
    public Result<Object> edit(@RequestBody AddressBook addressBook) {
        return Result.success(addressBookService.userUpdate(addressBook));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除")
    public Result<Object> remove(@PathVariable Long id) {
        return Result.success(addressBookService.userDeleteById(id));
    }
}
