-- S1-08 档案层功能测试数据
-- 用途：为测试环境准备基础档案数据，支持 CRUD / 权限隔离 / 唯一约束验证

-- 测试项目（2个）
INSERT INTO resi_project (id, code, name, address, enabled_mark, create_by, create_time) VALUES
(1, 'TEST-PRJ-01', 'Test Project Alpha', 'Beijing Chaoyang', 1, 'admin', NOW()),
(2, 'TEST-PRJ-02', 'Test Project Beta', 'Beijing Haidian', 1, 'admin', NOW());

-- 测试楼栋（每个项目2栋）
INSERT INTO resi_building (id, project_id, name, number, floors, enabled_mark, create_by, create_time) VALUES
(1, 1, 'Building A', 'B-A', 20, 1, 'admin', NOW()),
(2, 1, 'Building B', 'B-B', 18, 1, 'admin', NOW()),
(3, 2, 'Building C', 'B-C', 25, 1, 'admin', NOW()),
(4, 2, 'Building D', 'B-D', 22, 1, 'admin', NOW());

-- 测试房间（每栋2个房间）
INSERT INTO resi_room (id, project_id, building_id, unit_no, floor_no, room_no, room_alias, building_area, state, enabled_mark, create_by, create_time) VALUES
(1, 1, 1, '1', 1, '101', 'Building A Unit 1 101', 128.50, 'NORMAL', 1, 'admin', NOW()),
(2, 1, 1, '1', 2, '201', 'Building A Unit 1 201', 95.00, 'NORMAL', 1, 'admin', NOW()),
(3, 1, 2, '2', 1, '102', 'Building B Unit 2 102', 110.00, 'VACANT', 1, 'admin', NOW()),
(4, 2, 3, '1', 5, '501', 'Building C Unit 1 501', 150.00, 'NORMAL', 1, 'admin', NOW()),
(5, 2, 4, '3', 10, '1001', 'Building D Unit 3 1001', 200.00, 'RENTED', 1, 'admin', NOW());

-- 测试客户
INSERT INTO resi_customer (id, project_id, customer_name, phone, id_card, gender, customer_type, enabled_mark, create_by, create_time) VALUES
(1, 1, 'Test Customer A', '13800180001', NULL, 1, 1, 1, 'admin', NOW()),
(2, 1, 'Test Customer B', '13900190002', NULL, 2, 2, 1, 'admin', NOW()),
(3, 2, 'Test Customer C', '13700170003', NULL, 1, 1, 1, 'admin', NOW());

-- 测试仪表
INSERT INTO resi_meter_device (id, project_id, room_id, meter_code, meter_type, install_date, init_reading, multiplier, is_public, public_group, enabled_mark, create_by, create_time) VALUES
(1, 1, 1, 'WM-101-A', 1, NOW(), 0.00, 1.00, 0, NULL, 1, 'admin', NOW()),
(2, 1, NULL, 'WM-PUB-01', 1, NOW(), 0.00, 1.00, 1, 'G01', 1, 'admin', NOW()),
(3, 2, 4, 'EM-501-C', 2, NOW(), 0.00, 1.00, 0, NULL, 1, 'admin', NOW());

-- 测试用户项目权限（用户2仅能访问项目1）
INSERT INTO resi_user_project (id, user_id, project_id, create_time) VALUES
(1, 2, 1, NOW()),
(2, 3, 2, NOW());
