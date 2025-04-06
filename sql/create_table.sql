# 数据库初始化
# @author <a href="https://github.com/liyupi">程序员鱼皮</a>
# @from <a href="https://yupi.icu">编程导航知识星球</a>

-- 创建库
create database if not exists yun_docker;

-- 切换库
use yun_docker;

-- 用户表
create table if not exists user
(
    id            bigint auto_increment comment 'id' primary key,
    user_account  varchar(256)                           not null comment '账号',
    user_password varchar(512)                           not null comment '密码',
    user_name     varchar(256)                           null comment '用户昵称',
    user_avatar   varchar(1024)                          null comment '用户头像',
    balance       int          default 1000              not null comment '余额',
    user_role     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint      default 0                 not null comment '是否删除'
) comment '用户' collate = utf8mb4_unicode_ci;

-- 镜像表
create table if not exists yun_image
(
    id          bigint auto_increment comment 'id' primary key,
    repository  varchar(256)                           not null comment '镜像名称',
    tag         varchar(256) default 'latest'          not null comment 'tag',
    image_id    varchar(256)                           null comment '镜像id',
    image_size  int                                    null comment '镜像大小',
    image_type  tinyint      default 0                 not null comment '镜像类型：0-public,1-private',
    user_id     bigint                                 null comment '创建用户 id',
    create_time datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_ime  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint      default 0                 not null comment '是否删除',
    index idx_userId (user_id)
) comment '镜像' collate = utf8mb4_unicode_ci;

-- 容器表（硬删除）
create table if not exists yun_container
(
    id             bigint auto_increment comment 'id' primary key,
    image_id       bigint                             not null comment '镜像 id（主键）',
    user_id        bigint                             not null comment '创建用户 id',
    container_id   varchar(256)                       not null comment '容器 id',
    command        varchar(256)                       null comment '命令',
    status         varchar(256)                       null comment '状态',
    ports          varchar(256)                       null comment '端口',
    container_name varchar(256)                       null comment '容器名称',
    create_time    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (image_id),
    index idx_userId (user_id)
) comment '容器' collate = utf8mb4_unicode_ci;

-- 端口表（硬删除）
create table if not exists yun_port
(
    id          bigint auto_increment comment 'id' primary key,
    port        int unique                         not null comment '端口',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '端口' collate = utf8mb4_unicode_ci;

