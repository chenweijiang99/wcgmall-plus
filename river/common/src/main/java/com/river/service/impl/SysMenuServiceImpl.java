package com.river.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.river.common.Constants;
import com.river.entity.SysMenu;
import com.river.enums.MenuTypeEnum;
import com.river.mapper.SysMenuMapper;
import com.river.service.SysMenuService;
import com.river.vo.menu.RouterVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public List<SysMenu> getMenuTree() {
        // 获取所有菜单
        List<SysMenu> menus = list(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort));
        
        // 构建树形结构
        Map<Integer, List<SysMenu>> childrenMap = menus.stream()
                .filter(menu -> menu.getParentId() != 0)
                .collect(Collectors.groupingBy(SysMenu::getParentId));

        menus.forEach(menu -> menu.setChildren(childrenMap.get(menu.getId())));

        return menus.stream()
                .filter(menu -> menu.getParentId() == 0)
                .collect(Collectors.toList());
    }


    @Override
    public void addMenu(SysMenu menu) {
        if (menu.getType().equals(MenuTypeEnum.CATALOG)) {
            menu.setComponent("Layout");
        }
        save(menu);
    }

    @Override
    public void updateMenu(SysMenu menu) {
        if (menu.getType().equals(MenuTypeEnum.CATALOG)) {
            menu.setComponent("Layout");
        }
        updateById(menu);
    }

    @Override
    public void deleteMenu(Integer id) {
        // 递归删除所有子菜单
        baseMapper.selectList(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id))
                .forEach(childMenu -> deleteMenu(childMenu.getId()));

        // 删除当前菜单
        removeById(id);
    }

    @Override
    public List<RouterVO> getCurrentUserMenu() {

        List<SysMenu> menus;
        if (StpUtil.hasRole(Constants.ADMIN)) {
            menus = baseMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                    .ne(SysMenu::getType,MenuTypeEnum.BUTTON.getCode()));
        }else {
            menus = baseMapper.getMenusByUserId(StpUtil.getLoginIdAsInt(),MenuTypeEnum.BUTTON.getCode());
        }

        return this.buildRouterTree(menus);
    }


    public List<RouterVO> buildRouterTree(List<SysMenu> menus) {
        List<RouterVO> resultList = new ArrayList<>();
        for (SysMenu menu : menus) {
            Integer parentId = menu.getParentId();
            if ( parentId == null || parentId == 0) {
                RouterVO.MetaVO metaVO = new RouterVO.MetaVO(menu.getTitle(),menu.getIcon(),menu.getHidden(),menu.getIsExternal());
                RouterVO build = RouterVO.builder().id(menu.getId()).path(menu.getPath()).redirect(menu.getRedirect()).name(menu.getName()).component(menu.getComponent())
                        .meta(metaVO).sort(menu.getSort()).build();
                resultList.add(build);
            }
        }
        resultList.sort(Comparator.comparingInt(RouterVO::getSort));

        for (RouterVO routerVO : resultList) {
            routerVO.setChildren(getRouterChild(routerVO.getId(),menus));
        }
        return resultList;
    }

    private List<RouterVO> getRouterChild(Integer pid , List<SysMenu> menus){
        if (menus == null) {
            return Collections.emptyList();
        }
        Map<Integer, RouterVO> routerMap = new HashMap<>();
        for (SysMenu e: menus) {
            Integer parentId = e.getParentId();
            if(parentId != null && parentId.equals(pid)){
                // 子菜单的下级菜单
                RouterVO.MetaVO metaVO = new RouterVO.MetaVO(e.getTitle(),e.getIcon(),e.getHidden(),e.getIsExternal());
                RouterVO routerVO = RouterVO.builder()
                        .id(e.getId())
                        .path(e.getPath())
                        .redirect(e.getRedirect())
                        .name(e.getName())
                        .component(e.getComponent())
                        .meta(metaVO).sort(e.getSort()).build();

                if (StringUtils.isEmpty(e.getComponent()) && isParentView(e)){
                    routerVO.setComponent(Constants.PARENT_VIEW);
                }
                routerMap.put(e.getId(), routerVO);
            }
        }

        List<RouterVO> childrens = new ArrayList<>(routerMap.values());
        childrens.sort(Comparator.comparingInt(RouterVO::getSort));

        for (RouterVO e : childrens) {
            e.setChildren(getRouterChild(e.getId(), menus));
        }

        return childrens.isEmpty() ? Collections.emptyList() : childrens;
    }

    /**
     * 是否为parent_view组件
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isParentView(SysMenu menu)
    {
        return menu.getParentId() != 0 && MenuTypeEnum.MENU.equals(menu.getType());
    }
} 