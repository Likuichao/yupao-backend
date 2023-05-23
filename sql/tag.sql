create table if not exists lkc.tag
(
    id bigint auto_increment
    primary key,
    tagName varchar(256) null comment '标签名称',
    userId bigint null comment '用户 id',
    parentId bigint null comment '父标签 id',
    isParentId tinyint null comment '是否为父标签 0-不是 1-父标签',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    idDelete tinyint default 0 not null comment '是否删除',
    constraint unique_tagName
    unique (tagName)
    )
    comment '标签';

create index idx_userId
	on lkc.tag (userId);

