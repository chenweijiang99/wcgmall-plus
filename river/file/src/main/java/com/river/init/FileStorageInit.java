package com.river.init;

import com.river.entity.SysFileOss;
import com.river.enums.FileOssEnum;
import com.river.mapper.SysFileOssMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.dromara.x.file.storage.core.FileStorageProperties;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.FileStorageServiceBuilder;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

/**
 * @author: river
 * @description: 文件存储初始化
 */
@Component
@RequiredArgsConstructor
public class FileStorageInit {

    private final FileStorageService service;

    private final SysFileOssMapper sysFileOssMapper;



    @PostConstruct
    private void init(){
        List<SysFileOss> sysFileOssList = sysFileOssMapper.selectList(null);

        for (SysFileOss sysFileOss : sysFileOssList) {
            if (sysFileOss.getPlatform().equals(FileOssEnum.ALI.getValue())) {
                FileStorageProperties.AliyunOssConfig config = new FileStorageProperties.AliyunOssConfig();
                config.setPlatform(sysFileOss.getPlatform());
                config.setAccessKey(sysFileOss.getAccessKey());
                config.setSecretKey(sysFileOss.getSecretKey());
                config.setDomain(sysFileOss.getDomain());
                config.setBucketName(sysFileOss.getBucket());
                config.setBasePath(sysFileOss.getBasePath());
                config.setEndPoint(sysFileOss.getEndpoint());
                service.getFileStorageList().addAll(FileStorageServiceBuilder
                        .buildAliyunOssFileStorage(Collections.singletonList(config),null));
            } else if (sysFileOss.getPlatform().equals(FileOssEnum.QINIU.getValue())) {
                FileStorageProperties.QiniuKodoConfig config = new FileStorageProperties.QiniuKodoConfig();
                config.setPlatform(sysFileOss.getPlatform());
                config.setAccessKey(sysFileOss.getAccessKey());
                config.setSecretKey(sysFileOss.getSecretKey());
                config.setDomain(sysFileOss.getDomain());
                config.setBucketName(sysFileOss.getBucket());
                config.setBasePath(sysFileOss.getBasePath());
                service.getFileStorageList().addAll(FileStorageServiceBuilder
                        .buildQiniuKodoFileStorage(Collections.singletonList(config),null));

            } else if (sysFileOss.getPlatform().equals(FileOssEnum.TENCENT.getValue())) {
                FileStorageProperties.TencentCosConfig config = new FileStorageProperties.TencentCosConfig();
                config.setPlatform(sysFileOss.getPlatform());
                config.setSecretId(sysFileOss.getAccessKey());
                config.setSecretKey(sysFileOss.getSecretKey());
                config.setDomain(sysFileOss.getDomain());
                config.setBucketName(sysFileOss.getBucket());
                config.setBasePath(sysFileOss.getBasePath());
                config.setRegion(sysFileOss.getRegion());
                service.getFileStorageList().addAll(FileStorageServiceBuilder
                        .buildTencentCosFileStorage(Collections.singletonList(config),null));

            }else if (sysFileOss.getPlatform().equals(FileOssEnum.MINIO.getValue())) {
                FileStorageProperties.MinioConfig config = new FileStorageProperties.MinioConfig();
                config.setPlatform(sysFileOss.getPlatform());
                config.setAccessKey(sysFileOss.getAccessKey());
                config.setSecretKey(sysFileOss.getSecretKey());
                config.setDomain(sysFileOss.getDomain());
                config.setBucketName(sysFileOss.getBucket());
                config.setBasePath(sysFileOss.getBasePath());
                config.setEndPoint(sysFileOss.getDomain());
                service.getFileStorageList().addAll(FileStorageServiceBuilder
                        .buildMinioFileStorage(Collections.singletonList(config),null));

            }else {
                FileStorageProperties.LocalPlusConfig config = new FileStorageProperties.LocalPlusConfig();
                config.setPlatform(sysFileOss.getPlatform());
                config.setBasePath(sysFileOss.getBasePath());
                config.setStoragePath(sysFileOss.getStoragePath());
                config.setDomain(sysFileOss.getDomain());
                service.getFileStorageList().addAll(FileStorageServiceBuilder
                        .buildLocalPlusFileStorage(Collections.singletonList(config)));
                service.getProperties().setDefaultPlatform(sysFileOss.getPlatform());
            }

            if (sysFileOss.getIsEnable() == 1){
                service.getProperties().setDefaultPlatform(sysFileOss.getPlatform());
            }
        }
    }

}
