package com.river.service.impl;

import java.util.List;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.river.vo.comments.CommentsVo;
import org.springframework.stereotype.Service;
import com.river.mapper.CommentsMapper;
import com.river.entity.Comments;
import com.river.service.CommentsService;
import com.river.utils.PageUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;

/**
 *  服务实现类
 */
@Service
@RequiredArgsConstructor
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments> implements CommentsService {

    /**
     * 查询分页列表
     */
    @Override
    public IPage<Comments> selectPage(Comments comments) {
//        LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
//        // 构建查询条件
//        wrapper.eq(comments.getModule() != null, Comments::getModule, comments.getModule());
//        wrapper.eq(comments.getUserId() != null, Comments::getUserId, comments.getUserId());
//        wrapper.like(comments.getComment() != null, Comments::getComment, comments.getComment());
//        return page(PageUtil.getPage(), wrapper);
        Page<Comments> page = PageUtil.getPage();
        return baseMapper.selectCommentsWithUser(page, comments);
    }

    /**
     * 查询列表
     */
    @Override
    public List<Comments> selectList(Comments comments) {
        LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(comments.getModule() != null, Comments::getModule, comments.getModule());
        wrapper.eq(comments.getUserId() != null, Comments::getUserId, comments.getUserId());
        wrapper.like(comments.getComment() != null, Comments::getComment, comments.getComment());
        return list(wrapper);
    }

    /**
     * 新增
     */
    @Override
    public boolean insert(Comments comments) {
        comments.setUserId(StpUtil.getLoginIdAsLong());
        return save(comments);
    }

    /**
     * 修改
     */
    @Override
    public boolean update(Comments comments) {
        return updateById(comments);
    }

    /**
     * 批量删除
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }

    public IPage<CommentsVo> selectTree(Comments comments) {
        Page<CommentsVo> page = PageUtil.getPage();
        List<CommentsVo> list = baseMapper.selectRoot(comments);
        for (CommentsVo root : list) {
            List<CommentsVo> children = baseMapper.selectByRootId(root.getId());
            root.setChildren(children);
        }
        return page.setRecords(list);
    }

    @Override
    public Long selectCount(Integer fid, String module) {
        LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comments::getFid, fid);
        wrapper.eq(Comments::getModule, module);
        return baseMapper.selectCount(wrapper);
    }

    @Override
    public boolean addComments(Comments comments) {
        comments.setUserId(StpUtil.getLoginIdAsLong());
        // 插入到数据库的数据会出现自增id，这个id就可以作为自己的root_id
        baseMapper.addComments(comments);
        if(comments.getPid() != null){
            // 有pid说明它是子评论  子评论必须有root_id
            Comments parent = baseMapper.selectById(comments.getPid());
            comments.setRootId(parent.getRootId());
        }else {
            // 根节点，给根节点设置root_id
            comments.setRootId(comments.getId());
        }
        baseMapper.updateById(comments);
        return true;
    }
}
