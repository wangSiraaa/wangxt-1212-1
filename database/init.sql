-- 机场冬季除冰液管理系统数据库

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 航班表
-- ----------------------------
DROP TABLE IF EXISTS `flight`;
CREATE TABLE `flight` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `flight_no` varchar(32) NOT NULL COMMENT '航班号',
  `airline` varchar(64) DEFAULT NULL COMMENT '航空公司',
  `aircraft_type` varchar(32) DEFAULT NULL COMMENT '机型',
  `departure_airport` varchar(64) DEFAULT NULL COMMENT '出发机场',
  `arrival_airport` varchar(64) DEFAULT NULL COMMENT '到达机场',
  `scheduled_departure_time` datetime DEFAULT NULL COMMENT '计划起飞时间',
  `actual_departure_time` datetime DEFAULT NULL COMMENT '实际起飞时间',
  `stand_no` varchar(32) DEFAULT NULL COMMENT '停机位',
  `flight_status` varchar(32) DEFAULT 'SCHEDULED' COMMENT '航班状态：SCHEDULED-计划中，DEICING-除冰中，DEICED-已除冰，DEPARTED-已起飞，CANCELLED-已取消',
  `deicing_required` tinyint(1) DEFAULT '1' COMMENT '是否需要除冰',
  `deicing_completed` tinyint(1) DEFAULT '0' COMMENT '除冰是否完成',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `has_risk_remark` tinyint(1) DEFAULT '0' COMMENT '是否有风险备注',
  `risk_reason` varchar(500) DEFAULT NULL COMMENT '风险原因',
  `risk_marked_by` varchar(64) DEFAULT NULL COMMENT '风险标记人',
  `risk_mark_time` datetime DEFAULT NULL COMMENT '风险标记时间',
  `risk_clear_by` varchar(64) DEFAULT NULL COMMENT '风险清除人',
  `risk_clear_time` datetime DEFAULT NULL COMMENT '风险清除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flight_no` (`flight_no`),
  KEY `idx_scheduled_departure` (`scheduled_departure_time`),
  KEY `idx_flight_status` (`flight_status`),
  KEY `idx_has_risk_remark` (`has_risk_remark`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='航班表';

-- ----------------------------
-- 风险备注历史表
-- ----------------------------
DROP TABLE IF EXISTS `risk_remark_history`;
CREATE TABLE `risk_remark_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `flight_id` bigint NOT NULL COMMENT '航班ID',
  `flight_no` varchar(32) DEFAULT NULL COMMENT '航班号',
  `risk_reason` varchar(500) DEFAULT NULL COMMENT '风险原因',
  `marked_by` varchar(64) DEFAULT NULL COMMENT '标记人',
  `mark_time` datetime DEFAULT NULL COMMENT '标记时间',
  `cleared_by` varchar(64) DEFAULT NULL COMMENT '清除人',
  `clear_time` datetime DEFAULT NULL COMMENT '清除时间',
  `clear_type` varchar(32) DEFAULT NULL COMMENT '清除类型：MANUAL-手动清除，DEICING_COMPLETED-除冰完成自动清除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_flight_id` (`flight_id`),
  KEY `idx_flight_no` (`flight_no`),
  KEY `idx_mark_time` (`mark_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风险备注历史表';

-- ----------------------------
-- 车辆表（除冰车）
-- ----------------------------
DROP TABLE IF EXISTS `vehicle`;
CREATE TABLE `vehicle` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `vehicle_no` varchar(32) NOT NULL COMMENT '车辆编号',
  `vehicle_name` varchar(64) DEFAULT NULL COMMENT '车辆名称',
  `vehicle_type` varchar(32) DEFAULT NULL COMMENT '车辆类型：TYPE1-除冰车1型，TYPE2-除冰车2型',
  `driver_name` varchar(32) DEFAULT NULL COMMENT '驾驶员姓名',
  `driver_phone` varchar(32) DEFAULT NULL COMMENT '驾驶员电话',
  `current_batch_id` bigint DEFAULT NULL COMMENT '当前装载批次ID',
  `current_fluid_volume` decimal(10,2) DEFAULT '0.00' COMMENT '当前液体量（升）',
  `vehicle_status` varchar(32) DEFAULT 'IDLE' COMMENT '车辆状态：IDLE-空闲，DISPATCHED-已调度，SPRAYING-作业中，MAINTENANCE-维护中',
  `current_stand` varchar(32) DEFAULT NULL COMMENT '当前位置',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vehicle_no` (`vehicle_no`),
  KEY `idx_vehicle_status` (`vehicle_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车辆表';

-- ----------------------------
-- 除冰液批次表
-- ----------------------------
DROP TABLE IF EXISTS `deicing_fluid_batch`;
CREATE TABLE `deicing_fluid_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` varchar(64) NOT NULL COMMENT '批次编号',
  `fluid_type` varchar(32) NOT NULL COMMENT '液体类型：TYPE1-除冰液，TYPE2-防冰液',
  `fluid_name` varchar(64) DEFAULT NULL COMMENT '液体名称',
  `manufacturer` varchar(64) DEFAULT NULL COMMENT '生产厂家',
  `production_date` date DEFAULT NULL COMMENT '生产日期',
  `expiry_date` date DEFAULT NULL COMMENT '有效期至',
  `total_volume` decimal(10,2) DEFAULT '0.00' COMMENT '总容量（升）',
  `used_volume` decimal(10,2) DEFAULT '0.00' COMMENT '已使用量（升）',
  `remaining_volume` decimal(10,2) DEFAULT '0.00' COMMENT '剩余量（升）',
  `min_valid_temperature` decimal(5,2) DEFAULT NULL COMMENT '最低有效温度（℃）',
  `max_valid_temperature` decimal(5,2) DEFAULT NULL COMMENT '最高有效温度（℃）',
  `concentration` decimal(5,2) DEFAULT NULL COMMENT '原液浓度（%）',
  `batch_status` varchar(32) DEFAULT 'AVAILABLE' COMMENT '批次状态：AVAILABLE-可用，IN_USE-使用中，EXPIRED-已过期，EMPTY-已用完',
  `storage_location` varchar(64) DEFAULT NULL COMMENT '存储位置',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`),
  KEY `idx_batch_status` (`batch_status`),
  KEY `idx_fluid_type` (`fluid_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='除冰液批次表';

-- ----------------------------
-- 调度记录表
-- ----------------------------
DROP TABLE IF EXISTS `dispatch_record`;
CREATE TABLE `dispatch_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dispatch_no` varchar(64) NOT NULL COMMENT '调度单号',
  `flight_id` bigint NOT NULL COMMENT '航班ID',
  `flight_no` varchar(32) NOT NULL COMMENT '航班号',
  `vehicle_id` bigint NOT NULL COMMENT '车辆ID',
  `vehicle_no` varchar(32) NOT NULL COMMENT '车辆编号',
  `batch_id` bigint NOT NULL COMMENT '除冰液批次ID',
  `batch_no` varchar(64) NOT NULL COMMENT '批次编号',
  `dispatcher_name` varchar(32) DEFAULT NULL COMMENT '调度员姓名',
  `dispatch_time` datetime DEFAULT NULL COMMENT '调度时间',
  `estimated_spray_volume` decimal(10,2) DEFAULT NULL COMMENT '预估喷洒量（升）',
  `dispatch_status` varchar(32) DEFAULT 'PENDING' COMMENT '调度状态：PENDING-待执行，IN_PROGRESS-进行中，COMPLETED-已完成，CANCELLED-已取消',
  `stand_no` varchar(32) DEFAULT NULL COMMENT '停机位',
  `ambient_temperature` decimal(5,2) DEFAULT NULL COMMENT '环境温度（℃）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dispatch_no` (`dispatch_no`),
  KEY `idx_flight_id` (`flight_id`),
  KEY `idx_vehicle_id` (`vehicle_id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_dispatch_status` (`dispatch_status`),
  KEY `idx_dispatch_time` (`dispatch_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='调度记录表';

-- ----------------------------
-- 喷洒记录表
-- ----------------------------
DROP TABLE IF EXISTS `spray_record`;
CREATE TABLE `spray_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spray_no` varchar(64) NOT NULL COMMENT '喷洒记录单号',
  `dispatch_id` bigint NOT NULL COMMENT '调度记录ID',
  `dispatch_no` varchar(64) NOT NULL COMMENT '调度单号',
  `flight_id` bigint NOT NULL COMMENT '航班ID',
  `flight_no` varchar(32) NOT NULL COMMENT '航班号',
  `vehicle_id` bigint NOT NULL COMMENT '车辆ID',
  `vehicle_no` varchar(32) NOT NULL COMMENT '车辆编号',
  `batch_id` bigint NOT NULL COMMENT '除冰液批次ID',
  `batch_no` varchar(64) NOT NULL COMMENT '批次编号',
  `driver_name` varchar(32) DEFAULT NULL COMMENT '驾驶员姓名',
  `start_time` datetime DEFAULT NULL COMMENT '开始喷洒时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束喷洒时间',
  `spray_volume` decimal(10,2) DEFAULT '0.00' COMMENT '实际喷洒量（升）',
  `spray_type` varchar(32) DEFAULT NULL COMMENT '喷洒方式：FIRST_STEP-一步法，TWO_STEP-两步法',
  `wing_sprayed` tinyint(1) DEFAULT '1' COMMENT '机翼是否喷洒',
  `fuselage_sprayed` tinyint(1) DEFAULT '1' COMMENT '机身是否喷洒',
  `tail_sprayed` tinyint(1) DEFAULT '1' COMMENT '尾翼是否喷洒',
  `fluid_temperature` decimal(5,2) DEFAULT NULL COMMENT '液体温度（℃）',
  `ambient_temperature` decimal(5,2) DEFAULT NULL COMMENT '环境温度（℃）',
  `spray_status` varchar(32) DEFAULT 'IN_PROGRESS' COMMENT '喷洒状态：IN_PROGRESS-进行中，COMPLETED-已完成，CANCELLED-已取消',
  `deicing_effect` varchar(32) DEFAULT NULL COMMENT '除冰效果：GOOD-良好，AVERAGE-一般，POOR-较差',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spray_no` (`spray_no`),
  KEY `idx_dispatch_id` (`dispatch_id`),
  KEY `idx_flight_id` (`flight_id`),
  KEY `idx_vehicle_id` (`vehicle_id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_spray_status` (`spray_status`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='喷洒记录表';

-- ----------------------------
-- 废液回收表
-- ----------------------------
DROP TABLE IF EXISTS `waste_recovery`;
CREATE TABLE `waste_recovery` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `recovery_no` varchar(64) NOT NULL COMMENT '回收记录单号',
  `recovery_pool_no` varchar(32) DEFAULT NULL COMMENT '回收池编号',
  `spray_id` bigint DEFAULT NULL COMMENT '喷洒记录ID',
  `flight_id` bigint DEFAULT NULL COMMENT '航班ID',
  `flight_no` varchar(32) DEFAULT NULL COMMENT '航班号',
  `recycler_name` varchar(32) DEFAULT NULL COMMENT '回收员姓名',
  `recovery_time` datetime DEFAULT NULL COMMENT '回收时间',
  `recovery_volume` decimal(10,2) DEFAULT '0.00' COMMENT '回收量（升）',
  `recovery_method` varchar(32) DEFAULT NULL COMMENT '回收方式：VACUUM-真空回收，SUMP-集液坑，MANUAL-人工收集',
  `concentration` decimal(5,2) DEFAULT NULL COMMENT '回收液浓度（%）',
  `ph_value` decimal(4,2) DEFAULT NULL COMMENT 'pH值',
  `contamination_level` varchar(32) DEFAULT NULL COMMENT '污染等级：LOW-低，MEDIUM-中，HIGH-高',
  `recovery_status` varchar(32) DEFAULT 'PENDING' COMMENT '回收状态：PENDING-待检测，TESTED-已检测，QUALIFIED-合格，UNQUALIFIED-不合格',
  `disposal_method` varchar(64) DEFAULT NULL COMMENT '处理方式：RECYCLE-回收再利用，TREATMENT-污水处理，STORAGE-存储待处理',
  `disposal_time` datetime DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recovery_no` (`recovery_no`),
  KEY `idx_flight_id` (`flight_id`),
  KEY `idx_spray_id` (`spray_id`),
  KEY `idx_recovery_status` (`recovery_status`),
  KEY `idx_recovery_time` (`recovery_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='废液回收表';

-- ----------------------------
-- 环保检查表
-- ----------------------------
DROP TABLE IF EXISTS `environmental_check`;
CREATE TABLE `environmental_check` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `check_no` varchar(64) NOT NULL COMMENT '检查单号',
  `recovery_id` bigint DEFAULT NULL COMMENT '废液回收ID',
  `recovery_no` varchar(64) DEFAULT NULL COMMENT '回收记录单号',
  `inspector_name` varchar(32) DEFAULT NULL COMMENT '环保专员姓名',
  `check_time` datetime DEFAULT NULL COMMENT '检查时间',
  `pool_no` varchar(32) DEFAULT NULL COMMENT '回收池编号',
  `concentration` decimal(5,2) DEFAULT NULL COMMENT '检测浓度（%）',
  `standard_concentration` decimal(5,2) DEFAULT NULL COMMENT '标准浓度（%）',
  `ph_value` decimal(4,2) DEFAULT NULL COMMENT 'pH值',
  `cod_value` decimal(10,2) DEFAULT NULL COMMENT 'COD值（mg/L）',
  `bod_value` decimal(10,2) DEFAULT NULL COMMENT 'BOD值（mg/L）',
  `check_result` varchar(32) DEFAULT 'PENDING' COMMENT '检查结果：PENDING-待检查，QUALIFIED-合格，UNQUALIFIED-不合格',
  `check_status` varchar(32) DEFAULT 'OPEN' COMMENT '检查状态：OPEN-进行中，CLOSED-已关闭',
  `recheck_required` tinyint(1) DEFAULT '0' COMMENT '是否需要复检',
  `recheck_time` datetime DEFAULT NULL COMMENT '复检时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_no` (`check_no`),
  KEY `idx_recovery_id` (`recovery_id`),
  KEY `idx_check_result` (`check_result`),
  KEY `idx_check_status` (`check_status`),
  KEY `idx_check_time` (`check_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='环保检查表';

-- ----------------------------
-- 实时看板缓存表（用于Redis持久化）
-- ----------------------------
DROP TABLE IF EXISTS `dashboard_snapshot`;
CREATE TABLE `dashboard_snapshot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `snapshot_time` datetime NOT NULL COMMENT '快照时间',
  `total_flights` int DEFAULT '0' COMMENT '今日航班总数',
  `pending_deicing` int DEFAULT '0' COMMENT '待除冰航班数',
  `deicing_in_progress` int DEFAULT '0' COMMENT '除冰中航班数',
  `completed_deicing` int DEFAULT '0' COMMENT '已完成除冰航班数',
  `available_vehicles` int DEFAULT '0' COMMENT '可用车辆数',
  `busy_vehicles` int DEFAULT '0' COMMENT '作业中车辆数',
  `available_batches` int DEFAULT '0' COMMENT '可用批次数',
  `total_waste_recovered` decimal(10,2) DEFAULT '0.00' COMMENT '今日废液回收总量',
  `qualified_checks` int DEFAULT '0' COMMENT '合格环保检查数',
  `unqualified_checks` int DEFAULT '0' COMMENT '不合格环保检查数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_snapshot_time` (`snapshot_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='看板快照表';

-- ----------------------------
-- 初始化数据
-- ----------------------------

-- 初始化航班数据
INSERT INTO `flight` (`flight_no`, `airline`, `aircraft_type`, `departure_airport`, `arrival_airport`, `scheduled_departure_time`, `stand_no`, `flight_status`, `deicing_required`, `deicing_completed`) VALUES
('CA1234', '中国国际航空', 'B737-800', 'PEK', 'SHA', DATE_ADD(NOW(), INTERVAL 30 MINUTE), 'T1-101', 'SCHEDULED', 1, 0),
('MU5678', '东方航空', 'A320neo', 'PEK', 'CAN', DATE_ADD(NOW(), INTERVAL 60 MINUTE), 'T1-102', 'SCHEDULED', 1, 0),
('CZ9012', '南方航空', 'B787-9', 'PEK', 'SZX', DATE_ADD(NOW(), INTERVAL 90 MINUTE), 'T2-201', 'DEICING', 1, 0),
('HU3456', '海南航空', 'A330-300', 'PEK', 'CTU', DATE_ADD(NOW(), INTERVAL 120 MINUTE), 'T2-202', 'SCHEDULED', 1, 0),
('CA8899', '山东航空', 'B737-MAX', 'PEK', 'TAO', DATE_ADD(NOW(), INTERVAL 150 MINUTE), 'T1-103', 'DEICED', 1, 1);

-- 初始化车辆数据
INSERT INTO `vehicle` (`vehicle_no`, `vehicle_name`, `vehicle_type`, `driver_name`, `driver_phone`, `current_fluid_volume`, `vehicle_status`, `current_stand`) VALUES
('V-001', '除冰车1号', 'TYPE1', '张师傅', '13800138001', 2500.00, 'IDLE', 'T1-停机坪A'),
('V-002', '除冰车2号', 'TYPE1', '李师傅', '13800138002', 3000.00, 'SPRAYING', 'T2-201'),
('V-003', '防冰车1号', 'TYPE2', '王师傅', '13800138003', 2000.00, 'IDLE', 'T1-停机坪B'),
('V-004', '除冰车3号', 'TYPE1', '赵师傅', '13800138004', 1800.00, 'IDLE', 'T2-停机坪C'),
('V-005', '防冰车2号', 'TYPE2', '孙师傅', '13800138005', 2200.00, 'MAINTENANCE', '维修车间');

-- 初始化除冰液批次数据
INSERT INTO `deicing_fluid_batch` (`batch_no`, `fluid_type`, `fluid_name`, `manufacturer`, `production_date`, `expiry_date`, `total_volume`, `used_volume`, `remaining_volume`, `min_valid_temperature`, `max_valid_temperature`, `concentration`, `batch_status`, `storage_location`) VALUES
('BATCH-2024-001', 'TYPE1', '飞机除冰液I型', '中化蓝天', '2024-10-01', '2025-10-01', 10000.00, 3500.00, 6500.00, -25.00, 10.00, 100.00, 'AVAILABLE', '储罐A区1号罐'),
('BATCH-2024-002', 'TYPE1', '飞机除冰液I型稀释液', '中化蓝天', '2024-11-01', '2025-11-01', 8000.00, 2000.00, 6000.00, -15.00, 5.00, 75.00, 'AVAILABLE', '储罐A区2号罐'),
('BATCH-2024-003', 'TYPE2', '飞机防冰液II型', '阿克苏诺贝尔', '2024-09-15', '2025-09-15', 5000.00, 1200.00, 3800.00, -30.00, 15.00, 100.00, 'AVAILABLE', '储罐B区1号罐'),
('BATCH-2024-004', 'TYPE1', '飞机除冰液I型', '陶氏化学', '2024-08-01', '2025-08-01', 6000.00, 5800.00, 200.00, -20.00, 8.00, 100.00, 'IN_USE', '储罐A区3号罐'),
('BATCH-2023-005', 'TYPE2', '飞机防冰液II型', '中化蓝天', '2023-10-01', '2024-10-01', 4000.00, 0.00, 4000.00, -25.00, 10.00, 100.00, 'EXPIRED', '储罐C区废弃罐');

-- 初始化调度记录
INSERT INTO `dispatch_record` (`dispatch_no`, `flight_id`, `flight_no`, `vehicle_id`, `vehicle_no`, `batch_id`, `batch_no`, `dispatcher_name`, `dispatch_time`, `estimated_spray_volume`, `dispatch_status`, `stand_no`, `ambient_temperature`) VALUES
('DP-20240101-001', 1, 'CA1234', 1, 'V-001', 1, 'BATCH-2024-001', '调度员小王', DATE_SUB(NOW(), INTERVAL 5 MINUTE), 800.00, 'PENDING', 'T1-101', -8.5),
('DP-20240101-002', 3, 'CZ9012', 2, 'V-002', 2, 'BATCH-2024-002', '调度员小李', DATE_SUB(NOW(), INTERVAL 15 MINUTE), 1200.00, 'IN_PROGRESS', 'T2-201', -7.0);

-- 初始化喷洒记录
INSERT INTO `spray_record` (`spray_no`, `dispatch_id`, `dispatch_no`, `flight_id`, `flight_no`, `vehicle_id`, `vehicle_no`, `batch_id`, `batch_no`, `driver_name`, `start_time`, `end_time`, `spray_volume`, `spray_type`, `fluid_temperature`, `ambient_temperature`, `spray_status`, `deicing_effect`) VALUES
('SP-20240101-001', 2, 'DP-20240101-002', 3, 'CZ9012', 2, 'V-002', 2, 'BATCH-2024-002', '李师傅', DATE_SUB(NOW(), INTERVAL 10 MINUTE), NULL, 600.00, 'TWO_STEP', -5.0, -7.0, 'IN_PROGRESS', NULL),
('SP-20240101-002', 0, '', 5, 'CA8899', 3, 'V-003', 3, 'BATCH-2024-003', '王师傅', DATE_SUB(NOW(), INTERVAL 60 MINUTE), DATE_SUB(NOW(), INTERVAL 45 MINUTE), 450.00, 'FIRST_STEP', -3.0, -6.5, 'COMPLETED', 'GOOD');

-- 初始化废液回收记录
INSERT INTO `waste_recovery` (`recovery_no`, `recovery_pool_no`, `spray_id`, `flight_id`, `flight_no`, `recycler_name`, `recovery_time`, `recovery_volume`, `recovery_method`, `concentration`, `ph_value`, `contamination_level`, `recovery_status`, `disposal_method`) VALUES
('WR-20240101-001', 'POOL-01', 2, 5, 'CA8899', '回收员小陈', DATE_SUB(NOW(), INTERVAL 30 MINUTE), 380.00, 'VACUUM', 35.00, 7.2, 'MEDIUM', 'TESTED', 'TREATMENT'),
('WR-20240101-002', 'POOL-02', NULL, NULL, NULL, '回收员小刘', DATE_SUB(NOW(), INTERVAL 2 HOUR), 1200.00, 'SUMP', 28.50, 7.8, 'LOW', 'PENDING', NULL);

-- 初始化环保检查记录
INSERT INTO `environmental_check` (`check_no`, `recovery_id`, `recovery_no`, `inspector_name`, `check_time`, `pool_no`, `concentration`, `standard_concentration`, `ph_value`, `cod_value`, `bod_value`, `check_result`, `check_status`, `recheck_required`) VALUES
('EC-20240101-001', 1, 'WR-20240101-001', '环保专员周工', DATE_SUB(NOW(), INTERVAL 20 MINUTE), 'POOL-01', 35.00, 30.00, 7.2, 850.00, 320.00, 'UNQUALIFIED', 'OPEN', 1),
('EC-20240101-002', NULL, '', '环保专员吴工', DATE_SUB(NOW(), INTERVAL 3 HOUR), 'POOL-03', 25.00, 30.00, 7.5, 450.00, 180.00, 'QUALIFIED', 'CLOSED', 0);

SET FOREIGN_KEY_CHECKS = 1;
