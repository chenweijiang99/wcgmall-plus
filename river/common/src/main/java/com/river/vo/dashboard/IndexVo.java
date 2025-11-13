package com.river.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndexVo {

    @Schema(description = "用户数量")
    private Long userCount;

    @Schema(description = "角色数量")
    private Long roleCount;





}
