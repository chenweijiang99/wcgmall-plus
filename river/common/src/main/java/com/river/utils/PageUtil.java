package com.river.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.river.common.PageQuery;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;

public class PageUtil {
    private static final String PAGE_NUM = "pageNum";
    private static final String PAGE_SIZE = "pageSize";

    /**
     * 获取分页参数
     */
    public static <T> Page<T> getPage() {
        PageQuery query = getPageQuery();
        return new Page<>(query.getPageNum(), query.getPageSize());
    }

    /**
     * 获取分页查询参数
     */
    public static PageQuery getPageQuery() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        PageQuery pageQuery = new PageQuery();

        // 获取页码
        String pageNum = request.getParameter(PAGE_NUM);
        if (pageNum != null && !pageNum.isEmpty()) {
            pageQuery.setPageNum(Integer.parseInt(pageNum));
        }

        // 获取每页数量
        String pageSize = request.getParameter(PAGE_SIZE);
        if (pageSize != null && !pageSize.isEmpty()) {
            pageQuery.setPageSize(Integer.parseInt(pageSize));
        }

        return pageQuery;
    }
}