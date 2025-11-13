package com.river.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.river.utils.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("file_detail")
@Schema(description = "文件记录表对象")
public class FileDetail implements Serializable {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "文件id")
    private String id;

    @Schema(description = "文件访问地址")
    private String url;

    @Schema(description = "文件大小，单位字节")
    private Long size;

    @Schema(description = "文件名称")
    private String filename;

    @Schema(description = "原始文件名")
    private String originalFilename;

    @Schema(description = "基础存储路径")
    private String basePath;

    @Schema(description = "存储路径")
    private String path;

    @Schema(description = "文件扩展名")
    private String ext;

    @Schema(description = "MIME类型")
    private String contentType;

    @Schema(description = "存储平台")
    private String platform;

    @Schema(description = "缩略图访问路径")
    private String thUrl;

    @Schema(description = "缩略图名称")
    private String thFilename;

    @Schema(description = "缩略图大小，单位字节")
    private Long thSize;

    @Schema(description = "缩略图MIME类型")
    private String thContentType;

    @Schema(description = "文件所属对象id")
    private String objectId;

    @Schema(description = "文件所属对象类型，例如用户头像，评价图片")
    private String objectType;

    @Schema(description = "文件元数据")
    private String metadata;

    @Schema(description = "文件用户元数据")
    private String userMetadata;

    @Schema(description = "缩略图元数据")
    private String thMetadata;

    @Schema(description = "缩略图用户元数据")
    private String thUserMetadata;

    @Schema(description = "附加属性")
    private String attr;

    @Schema(description = "文件ACL")
    private String fileAcl;

    @Schema(description = "缩略图文件ACL")
    private String thFileAcl;

    @Schema(description = "哈希信息")
    private String hashInfo;

    @Schema(description = "上传ID，仅在手动分片上传时使用")
    private String uploadId;

    @Schema(description = "上传状态，仅在手动分片上传时使用，1：初始化完成，2：上传完成")
    private Integer uploadStatus;

    @Schema(description = "图片来源")
    private String source;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    private LocalDateTime createTime;
}
