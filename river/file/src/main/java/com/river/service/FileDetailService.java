package com.river.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.river.entity.FileDetail;
import com.river.entity.SysFileOss;
import org.dromara.x.file.storage.core.recorder.FileRecorder;

import java.util.List;

/**
 * @author: river
 * @description: 文件详情服务
 */
public interface FileDetailService extends FileRecorder,IService<FileDetail> {

    /**
     * 查询文件记录表分页列表
     */
    IPage<FileDetail> selectPage(FileDetail fileDetail);

    /**
     * 删除文件
     * @param url
     * @return
     */
    boolean delete(String url);

    /**
     * 获取存储平台配置
     * @return
     */
    List<SysFileOss> getOssConfig();

    /**
     * 添加存储平台配置
     * @param sysFileOss
     */
    void addOss(SysFileOss sysFileOss);

    /**
     * 修改存储平台配置
     * @param sysFileOss
     */
    void updateOss(SysFileOss sysFileOss);

    boolean deleteBatch(String[] urls);
}
