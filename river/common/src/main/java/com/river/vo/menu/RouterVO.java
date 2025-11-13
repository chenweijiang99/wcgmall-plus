package com.river.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "路由对象")
public class RouterVO {

    @Schema(description = "菜单ID")
    private Integer id;

    @Schema(description = "组件")
    private String component;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "路由名称")
    private String name;

    @Schema(description = "跳转地址")
    private String redirect;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "菜单属性")
    private MetaVO meta;

    private List<RouterVO> children;

    @Data
public static class MetaVO{

        @Schema(description = "菜单标题")
        private String title;

        @Schema(description = "菜单图标")
        private String icon;

        @Schema(description = "是否隐藏")
        private Boolean hidden;

        @Schema(description = "是否外链")
        private Boolean isExternal;

        public MetaVO(String title, String icon, Integer hidden, Integer isExternal) {
            this.title = title;
            this.icon = icon;
            this.hidden = hidden == 1;
            this.isExternal = isExternal == 1;
        }
    }
}
