-- 创建auth数据库
CREATE DATABASE IF NOT EXISTS auth DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
use auth;

CREATE TABLE `user` (
    `id` varchar(32) NOT NULL,
    `username` varchar(128) NOT NULL COMMENT '用户名',
    `password` varchar(256) NOT NULL COMMENT '用户密码',
    `nickname` varchar(128) NOT NULL COMMENT '用户昵称',
    `email` varchar(128) NOT NULL DEFAULT '' COMMENT '邮箱',
    `avatar` varchar(1024) NOT NULL DEFAULT '' COMMENT '头像',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态，0:禁用 1:正常',
    `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否已删除，0:否 1:是',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(32) NOT NULL COMMENT '创建人',
    `update_by` varchar(32) NOT NULL COMMENT '更新人',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

CREATE TABLE `role` (
    `id` varchar(32) NOT NULL,
    `name` varchar(128) NOT NULL COMMENT '角色名',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(32) NOT NULL COMMENT '创建人',
    `update_by` varchar(32) NOT NULL COMMENT '更新人',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

CREATE TABLE `role_authority` (
    `id` varchar(32) NOT NULL,
    `role_id` varchar(32) NOT NULL,
    `authority_name` varchar(32) NOT NULL,
    `authority_value` varchar(32) NOT NULL,
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(32) NOT NULL COMMENT '创建人',
    `update_by` varchar(32) NOT NULL COMMENT '更新人',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_role_id_authority_name` (`role_id`,`authority_name`) USING BTREE,
    UNIQUE KEY `uk_role_id_authority_value` (`role_id`,`authority_value`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限';

CREATE TABLE `user_role` (
    `id` varchar(32) NOT NULL,
    `user_id` varchar(32) NOT NULL COMMENT '用户id',
    `role_id` varchar(32) NOT NULL COMMENT '角色id',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(32) NOT NULL COMMENT '创建人',
    `update_by` varchar(32) NOT NULL COMMENT '更新人',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_id_role_id` (`user_id`, `role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色';

-- admin/admin
INSERT INTO `user` ( `id`, `username`, `password`, `nickname`, `email`, `create_by`, `update_by` )
VALUES
    ( '1', 'admin', '$2a$12$v1ERBqyxhU/ocHRPywOjvOAMkhmZGJB3hRoNjr4bWO3HLWZSIlnne', '超级管理员', '283052497@qq.com', '1', '1' );
INSERT INTO `role`(`id`, `name`, `create_by`, `update_by`) VALUES ('1', '超级管理员', '1', '1');
INSERT INTO `user_role`(`id`, `user_id`, `role_id`, `create_by`, `update_by`) VALUES ('1', '1', '1', '1', '1');
INSERT INTO `role_authority`(`id`, `role_id`, `authority_name`, `authority_value`, `create_by`, `update_by`)
VALUES
    ( '1', '1', '超级管理员', 'admin', '1', '1');

-- 创建console数据库
CREATE DATABASE IF NOT EXISTS console DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
use console;

CREATE TABLE `device` (
  `id` varchar(128) NOT NULL,
  `platform` tinyint(4) NOT NULL COMMENT '1:Android 2:iOS',
  `type` tinyint(4) NOT NULL,
  `manufacturer` varchar(128) NOT NULL DEFAULT '' COMMENT '制造商',
  `brand` varchar(128) NOT NULL DEFAULT '' COMMENT '品牌',
  `model` varchar(128) NOT NULL DEFAULT '' COMMENT '型号',
  `cpu` varchar(128) NOT NULL DEFAULT '',
  `mem_size` bigint (20) NOT NULL DEFAULT '-1' COMMENT '内存(kB)',
  `img_url` varchar(1024) NOT NULL DEFAULT '',
  `system_version` varchar(16) NOT NULL DEFAULT '' COMMENT '系统版本',
  `screen_width` int(11) NOT NULL DEFAULT '-1' COMMENT '屏幕宽',
  `screen_height` int(11) NOT NULL DEFAULT '-1' COMMENT '屏幕高',
  `description` varchar(256) NOT NULL DEFAULT '' COMMENT '描述',
  `extra` json COMMENT '扩展信息',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) NOT NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(32) NOT NULL DEFAULT '' COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备';

CREATE TABLE `user_project` (
  `id` varchar(32) NOT NULL,
  `user_id` varchar(32) NOT NULL COMMENT '用户id',
  `project_id` varchar(32) NOT NULL COMMENT '项目id',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) NOT NULL COMMENT '创建人',
  `update_by` varchar(32) NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_id_project_id` (`user_id`, `project_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户项目';

CREATE TABLE `project` (
  `id` varchar(32) NOT NULL,
  `name` varchar(128) NOT NULL COMMENT '项目名',
  `description` varchar(256) NOT NULL DEFAULT '' COMMENT '项目描述',
  `extra` json COMMENT '扩展信息',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否已删除，0:否 1:是',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) NOT NULL COMMENT '创建人',
  `update_by` varchar(32) NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目';

CREATE TABLE `plugin` (
  `id` varchar(32) NOT NULL,
  `name` varchar(128) NOT NULL,
  `description` varchar(256) NOT NULL DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) NOT NULL COMMENT '创建人',
  `update_by` varchar(32) NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件';

CREATE TABLE `plugin_file` (
  `id` varchar(32) NOT NULL,
  `plugin_id` varchar(32) NOT NULL,
  `name` varchar(128) NOT NULL,
  `url` varchar(1024) NOT NULL,
  `size` bigint(20) NOT NULL DEFAULT '-1',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) NOT NULL COMMENT '创建人',
  `update_by` varchar(32) NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_plugin_id_name` (`plugin_id`, `name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `project_plugin` (
  `id` varchar(32) NOT NULL,
  `project_id` varchar(32) NOT NULL,
  `plugin_id` varchar(32) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) NOT NULL COMMENT '创建人',
  `update_by` varchar(32) NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_project_id_plugin_id` (`project_id`, `plugin_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目插件';

CREATE TABLE `pkg` (
  `id` varchar(32) NOT NULL,
  `project_id` varchar(32) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `parent_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '父节点，0为根节点',
  `weight` int(11) NOT NULL DEFAULT '0',
  `name` varchar(128) NOT NULL,
  `description` varchar(256) NOT NULL DEFAULT '' COMMENT '描述',
  `flags` int(11) NOT NULL DEFAULT '1',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) NOT NULL COMMENT '创建人',
  `update_by` varchar(32) NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_project_id_type_parent_id_name` (`project_id`,`type`,`parent_id`,`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目录';

CREATE TABLE `doc` (
  `id` varchar(32) NOT NULL,
  `project_id` varchar(32) NOT NULL,
  `pkg_id` varchar(32) NOT NULL DEFAULT '0',
  `weight` int(11) NOT NULL DEFAULT '0',
  `kind` tinyint(4) NOT NULL,
  `name` varchar(128) NOT NULL,
  `description` varchar(256) NOT NULL DEFAULT '' COMMENT '描述',
  `content` longtext,
  `status` tinyint(4) NOT NULL DEFAULT '1',
  `flags` int(11) NOT NULL DEFAULT '1',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) NOT NULL COMMENT '创建人',
  `update_by` varchar(32) NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_project_id_pkg_id_name` (`project_id`,`pkg_id`,`name`) USING BTREE,
  KEY `idx_pkg_id` (`pkg_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档';

CREATE TABLE `plan` (
  `id` varchar(32) NOT NULL,
  `project_id` varchar(32) NOT NULL,
  `name` varchar(128) NOT NULL,
  `description` varchar(256) NOT NULL DEFAULT '',
  `run_mode` tinyint(4) NOT NULL DEFAULT '1',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) NOT NULL COMMENT '创建人',
  `update_by` varchar(32) NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_project_id_name` (`project_id`,`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `plan_device` (
    `id` varchar(32) NOT NULL,
    `plan_id` varchar(128) NOT NULL,
    `device_id` varchar(128) NOT NULL,
    `weight` int(11) NOT NULL DEFAULT '0',
    `enabled` tinyint(4) NOT NULL DEFAULT '1',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(32) NOT NULL COMMENT '创建人',
    `update_by` varchar(32) NOT NULL COMMENT '更新人',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_plan_id_device_id` (`plan_id`, `device_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `plan_doc` (
    `id` varchar(32) NOT NULL,
    `plan_id` varchar(128) NOT NULL,
    `doc_id` varchar(128) NOT NULL,
    `weight` int(11) NOT NULL DEFAULT '0',
    `enabled` tinyint(4) NOT NULL DEFAULT '1',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(32) NOT NULL COMMENT '创建人',
    `update_by` varchar(32) NOT NULL COMMENT '更新人',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_plan_id_doc_id` (`plan_id`, `doc_id`) USING BTREE,
    KEY `idx_doc_id` (`doc_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `execution_record` (
  `id` varchar(32) NOT NULL,
  `project_id` varchar(32) NOT NULL,
  `plan_id` varchar(32) NOT NULL,
  `plan` json,
  `status` tinyint(4) NOT NULL DEFAULT '0',
  `start_time` bigint(20) NOT NULL DEFAULT '0',
  `end_time` bigint(20) NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) NOT NULL COMMENT '创建人',
  `update_by` varchar(32) NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_plan_id` (`plan_id`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `plugin_execution_record` (
  `id` varchar(32) NOT NULL,
  `project_id` varchar(32) NOT NULL,
  `plan_id` varchar(32) NOT NULL,
  `execution_record_id` varchar(32) NOT NULL,
  `device_id` varchar(128) NOT NULL,
  `plugin_id` varchar(128) NOT NULL,
  `plugin` json,
  `status` tinyint(4) NOT NULL DEFAULT '0',
  `start_time` bigint(20) NOT NULL DEFAULT '0',
  `end_time` bigint(20) NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) NOT NULL COMMENT '创建人',
  `update_by` varchar(32) NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_execution_record_id` (`execution_record_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `doc_execution_record` (
  `id` varchar(32) NOT NULL,
  `project_id` varchar(32) NOT NULL,
  `plan_id` varchar(32) NOT NULL,
  `execution_record_id` varchar(32) NOT NULL,
  `device_id` varchar(128) NOT NULL,
  `doc_id` varchar(128) NOT NULL,
  `doc_kind` tinyint(4) NOT NULL,
  `doc` json,
  `status` tinyint(4) NOT NULL DEFAULT '0',
  `start_time` bigint(20) NOT NULL DEFAULT '0',
  `end_time` bigint(20) NOT NULL DEFAULT '0',
  `results` json,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) NOT NULL COMMENT '创建人',
  `update_by` varchar(32) NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_execution_record_id` (`execution_record_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `view` (
  `id` varchar(32) NOT NULL,
  `doc_id` varchar(32) NOT NULL,
  `device_id` varchar(128) NOT NULL DEFAULT '',
  `type` tinyint(4) NOT NULL,
  `source` longtext,
  `img_url` varchar(1024) NOT NULL DEFAULT '',
  `height` int(11) NOT NULL DEFAULT '-1',
  `width` int(11) NOT NULL DEFAULT '-1',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) NOT NULL COMMENT '创建人',
  `update_by` varchar(32) NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_doc_id` (`doc_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视图';