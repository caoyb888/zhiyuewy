package com.zhaoxinms.resi.common;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.zhaoxinms.common.core.mybatisplus.BaseEntity;

/**
 * 住宅物业档案类实体基类
 * <p>
 * 对应数据库档案类表公共字段：
 * enabled_mark, create_by, create_time, update_by, update_time
 * <p>
 * 主键策略：BIGINT AUTO_INCREMENT
 * 软删除：enabled_mark = 0
 * <p>
 * 继承 mybatisplus.BaseEntity 以获得 create_by/create_time/update_by/update_time 的自动填充能力
 *
 * @author zhaoxinms
 */
public class ResiBaseEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 有效标志 1有效 0无效
     */
    @TableLogic(value = "1", delval = "0")
    private Integer enabledMark;

    /**
     * 项目权限过滤列表（非数据库字段，由 AOP 注入）
     */
    @TableField(exist = false)
    private List<Long> projectIds;

    public Integer getEnabledMark() {
        return enabledMark;
    }

    public void setEnabledMark(Integer enabledMark) {
        this.enabledMark = enabledMark;
    }

    public List<Long> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<Long> projectIds) {
        this.projectIds = projectIds;
    }
}
