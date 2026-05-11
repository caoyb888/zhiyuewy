package com.zhaoxinms.resi.common.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhaoxinms.resi.common.entity.ResiUserProject;

/**
 * 用户项目权限关联 Mapper
 *
 * @author zhaoxinms
 */
public interface ResiUserProjectMapper extends BaseMapper<ResiUserProject> {

    /**
     * 查询用户可访问的项目ID列表
     */
    @Select("SELECT project_id FROM resi_user_project WHERE user_id = #{userId}")
    List<Long> selectProjectIdsByUserId(@Param("userId") Long userId);
}
