# 公共本地 MySQL

- 容器：`mysql`
- 地址：`127.0.0.1:3306`
- 通用业务库：`common`
- 用户名：`root`

## 两种连接方式

1. 容器内部使用本地 socket 登录：`docker exec -it mysql mysql -uroot -p`，密码使用你本机设置的值，对应 `root@localhost`。
2. 从 IDEA / Navicat / Java 通过宿主机 3306 端口连接：使用 `backend/src/main/resources/application.properties` 中的密码，对应 `root@%`，按要求保留原密码。

所有后端连接配置统一维护在 `backend/src/main/resources/application.properties`，无需 `.env` 或 Compose。

```sh
docker start mysql
docker stop mysql
```

数据沿用原有 Docker 数据卷，重启容器不会清空。`mysql` 是系统数据库；`common` 才是通用业务数据库。原 `contact_demo` 已删除。

联系人 CRUD 已改为使用 JdbcTemplate 读写 `common.contacts`，表结构见 `backend/src/main/resources/schema.sql`。后端启动时自动建表，不删除或重置已有数据。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT，自增主键 | 联系人编号 |
| name | VARCHAR(30) | 姓名，必填 |
| email | VARCHAR(100) | 邮箱，必填 |
| phone | VARCHAR(11) | 手机号，必填 |
| note | VARCHAR(100) | 备注，默认为空 |
| created_at | DATETIME(6) | 创建时间，数据库生成 |
| updated_at | DATETIME(6) | 最后更新时间，数据库维护 |

邮箱、手机号允许重复，方便保存共享联系方式；校验由后端执行。小型练习使用字面子串搜索，列表按编号倒序返回，前端继续进行分页。
