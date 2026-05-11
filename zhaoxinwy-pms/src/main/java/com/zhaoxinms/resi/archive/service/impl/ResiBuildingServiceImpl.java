package com.zhaoxinms.resi.archive.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.common.core.domain.TreeSelect;
import com.zhaoxinms.resi.archive.entity.ResiBuilding;
import com.zhaoxinms.resi.archive.entity.ResiProject;
import com.zhaoxinms.resi.archive.mapper.ResiBuildingMapper;
import com.zhaoxinms.resi.archive.service.IResiBuildingService;
import com.zhaoxinms.resi.archive.service.IResiProjectService;

/**
 * 楼栋档案 Service实现
 *
 * @author zhaoxinms
 */
@Service
public class ResiBuildingServiceImpl extends ServiceImpl<ResiBuildingMapper, ResiBuilding>
        implements IResiBuildingService {

    @Autowired
    private IResiProjectService projectService;

    @Override
    public List<ResiBuilding> selectResiBuildingList(ResiBuilding building) {
        return baseMapper.selectResiBuildingList(building);
    }

    @Override
    public List<TreeSelect> selectBuildingTreeSelect(Long projectId) {
        List<TreeSelect> tree = new ArrayList<>();

        if (projectId != null) {
            // 返回指定项目下的楼栋（单层）
            QueryWrapper<ResiBuilding> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("project_id", projectId);
            queryWrapper.eq("enabled_mark", 1);
            queryWrapper.orderByAsc("name");
            List<ResiBuilding> buildings = baseMapper.selectList(queryWrapper);
            for (ResiBuilding b : buildings) {
                TreeSelect node = new TreeSelect();
                node.setId(b.getId());
                node.setLabel(b.getName());
                tree.add(node);
            }
        } else {
            // 返回项目→楼栋的完整树
            List<ResiProject> projects = projectService.list();
            for (ResiProject p : projects) {
                TreeSelect projectNode = new TreeSelect();
                projectNode.setId(p.getId());
                projectNode.setLabel(p.getName());

                QueryWrapper<ResiBuilding> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("project_id", p.getId());
                queryWrapper.eq("enabled_mark", 1);
                queryWrapper.orderByAsc("name");
                List<ResiBuilding> buildings = baseMapper.selectList(queryWrapper);

                List<TreeSelect> children = new ArrayList<>();
                for (ResiBuilding b : buildings) {
                    TreeSelect node = new TreeSelect();
                    node.setId(b.getId());
                    node.setLabel(b.getName());
                    children.add(node);
                }
                projectNode.setChildren(children);
                tree.add(projectNode);
            }
        }

        return tree;
    }
}
