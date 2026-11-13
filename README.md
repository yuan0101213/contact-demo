# 联系人管理：Java + Vue 全栈练习

一个方便阅读的 CRUD 示例：Vue 3 + JavaScript + Element Plus + Axios，后端使用 Java Spring Boot。

## 启动

需要 JDK 17+、Maven 3.6.3+、Node.js 22 和 npm。

首次克隆后，先准备本机配置（密码不会提交到仓库）：

```sh
cp backend/src/main/resources/application.properties.example backend/src/main/resources/application.properties
```

编辑 `application.properties`，填入你的 MySQL 地址、库名和密码。数据库 `common` 需提前存在。

终端一：

```sh
cd backend
mvn spring-boot:run
```

终端二：

```sh
cd frontend
npm install
npm run dev
```

浏览器访问 http://127.0.0.1:5173。后端运行在 127.0.0.1:8080，前端通过 Vite 的 `/api` 代理调用真实后端。

## 功能与接口

| 操作 | 方法 | 地址 |
| --- | --- | --- |
| 列表 / 搜索 | GET | `/api/contacts?keyword=林` |
| 新增 | POST | `/api/contacts` |
| 编辑 | PUT | `/api/contacts/{id}` |
| 删除 | DELETE | `/api/contacts/{id}` |

新增、编辑 JSON 示例：

```json
{"name":"张三","email":"zhangsan@example.com","phone":"13800138000","note":"朋友"}
```

包含前后端表单校验、删除确认、搜索、本地分页、加载与错误提示。手机号规则为中国大陆 11 位手机号。

数据保存在公共 MySQL 的 `common.contacts` 表中，重启后端后仍然保留。先执行 `docker start mysql`，再启动后端。后端首次启动会执行 `schema.sql` 自动建表，不会清空现有数据或重复插入示例数据。所有后端配置统一在 `application.properties`，数据库连接方式见 `DATABASE.md`。此项目未包含登录和权限。

## 先看哪几个文件

1. `frontend/src/App.vue`：页面、表单、按钮事件。
2. `frontend/src/api.js`：前端如何发送 GET / POST / PUT / DELETE 请求。
3. `frontend/vite.config.js`：请求如何从前端转发到 Java。
4. `backend/src/main/java/com/example/contacts/ContactController.java`：Java 如何接收请求并处理数据。

点击“保存联系人”时：`App.vue save()` → `api.js` → Vite 代理 → `ContactController` → JSON 响应 → 刷新列表。

数据库读写实现见 `backend/src/main/java/com/example/contacts/ContactRepository.java`，使用 JdbcTemplate 和参数化 SQL。接口响应格式保持不变。新增 `GET /api/contacts/{id}` 可查询单条记录。

## 接口验证

后端启动后，运行 `python3 backend/tests/smoke.py`，验证真实的增删改查、搜索、非法输入和不存在记录；仅清理测试创建的联系人。

## 构建

```sh
cd frontend
npm run build
```

```sh
cd backend
mvn package
```

前端构建结果在 `frontend/dist`。本示例的 API 代理仅配置在开发服务器中，单独使用静态预览或部署时需另外设置 `/api` 转发。
