package com.river.service;

import com.river.entity.GenTable;

import java.io.IOException;
import java.util.Map;

public interface GenTableService {

    Map<String, Object> selectGenTableList(GenTable genTable);

    Map<String, String> previewCode(Long tableId);

    int deleteGenTableByIds(Long[] tableIds);

    String syncDb(String tableName);

    Map<String, Object> selectDbTableList(GenTable genTable);

    void importGenTable(String[] tableNames);

    byte[] downloadCode(String[] tableNames) throws IOException;

}
