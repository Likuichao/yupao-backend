CREATE TABLE `user` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT,
                        `username` varchar(256) DEFAULT NULL COMMENT '用户名',
                        `userAccount` varchar(256) DEFAULT NULL COMMENT '账号',
                        `avatarUrl` varchar(1024) DEFAULT NULL COMMENT '用户头像',
                        `gender` tinyint(4) DEFAULT NULL COMMENT '性别',
                        `userPassword` varchar(512) NOT NULL COMMENT '密码',
                        `email` varchar(512) DEFAULT NULL COMMENT '密码',
                        `userStatus` int(11) DEFAULT '0' COMMENT '状态 0-正常',
                        `phone` varchar(128) DEFAULT NULL COMMENT '电话',
                        `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `idDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
                        `userRole` int(11) NOT NULL DEFAULT '0' COMMENT '用户角色 0-普通用户  1-管理员',
                        `planetCode` varchar(512) DEFAULT NULL COMMENT '星球编号',
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COMMENT='用户'

