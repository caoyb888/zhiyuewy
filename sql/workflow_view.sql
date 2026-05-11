-- ----------------------------
-- View structure for ACT_ID_GROUP
-- ----------------------------
DROP VIEW IF EXISTS `ACT_ID_GROUP`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `ACT_ID_GROUP` AS
SELECT `r`.`role_key` AS `ID_`, NULL AS `REV_`, `r`.`role_name` AS `NAME_`, 'assignment' AS `TYPE_` FROM `sys_role` `r`;

-- ----------------------------
-- View structure for ACT_ID_MEMBERSHIP
-- ----------------------------
DROP VIEW IF EXISTS `ACT_ID_MEMBERSHIP`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `ACT_ID_MEMBERSHIP` AS
SELECT (SELECT `u`.`user_name` FROM `sys_user` `u` WHERE (`u`.`user_id` = `ur`.`user_id`)) AS `USER_ID_`,
       (SELECT `r`.`role_key` FROM `sys_role` `r` WHERE (`r`.`role_id` = `ur`.`role_id`)) AS `GROUP_ID_`
FROM `sys_user_role` `ur`;

-- ----------------------------
-- View structure for ACT_ID_USER
-- ----------------------------
DROP VIEW IF EXISTS `ACT_ID_USER`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `ACT_ID_USER` AS
SELECT `u`.`user_name` AS `ID_`, 0 AS `REV_`, `u`.`nick_name` AS `FIRST_`, '' AS `LAST_`,
       `u`.`email` AS `EMAIL_`, `u`.`password` AS `PWD_`, '' AS `PICTURE_ID_`
FROM `sys_user` `u`;
