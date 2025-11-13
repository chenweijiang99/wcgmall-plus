package com.river.controller.dashboard;


import com.river.common.Result;
import com.river.service.IndexService;
import com.river.vo.dashboard.IndexVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/dashboard")
@RequiredArgsConstructor
@Tag(name = "后台首页相关接口")
public class DashboardController {

    private final IndexService indexService;

    @GetMapping
    @Operation(summary = "首页")
    public Result<IndexVo> index() {
        return Result.success(indexService.index());
    }


}
