-- ------------------------------------------------------------
-- S1-07 项目权限隔离：新增用户项目关联表
-- 版本：0.11.1
-- 日期：2026-05-20
-- ------------------------------------------------------------

CREATE TABLE `resi_user_project` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '关联ID',
  `user_id`     BIGINT      NOT NULL                 COMMENT '用户ID，关联 sys_user.user_id',
  `project_id`  BIGINT      NOT NULL                 COMMENT '项目ID，关联 resi_project.id',
  `create_by`   VARCHAR(64) NOT NULL DEFAULT ''      COMMENT '创建者账号',
  `create_time` DATETIME    NOT NULL                 COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_project` (`user_id`, `project_id`) COMMENT '同一用户同一项目仅一条记录'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='用户项目权限关联（住宅收费模块数据隔离）';
