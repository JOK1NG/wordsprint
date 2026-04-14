# WordSprint

一个轻量、完整、适合 Java 学习的单词卡 / 记忆训练平台，采用 `Vue 3 + Spring Boot + Redis + MySQL` 的前后端分离架构。

---

## 项目简介

WordSprint 聚焦“背单词”这一条主线，覆盖单词卡管理、学习训练、错题复习、打卡积分、排行榜、学习统计和公共词库导入等能力。项目目标不是做成大而全平台，而是做成一个主流程完整、能稳定跑通、便于继续学习和维护的练手项目。

---

## 当前状态

当前仓库已经完成 MVP，并补齐了主要增强能力。当前可直接使用和验证的模块包括：

- 用户注册、登录、JWT 鉴权
- 个人资料更新、学习计划设置
- 单词卡 CRUD、分页筛选、CSV 导入
- 学习训练四种模式、答题提交、结果反馈
- 错题本、错题重练、状态恢复
- 每日打卡、连续学习天数、积分累计
- 积分榜、连续打卡榜
- 学习统计页、打卡日历、熟练度分布
- 公共词库浏览、一键导入、管理员 CSV 导入

补充说明：

- 排行榜返回项已包含 `avatar` 字段
- 有头像的用户会在排行榜中正常显示头像
- 无头像的用户会继续回退默认头像

---

## 技术栈

### 前端

- `Vue 3`
- `Vite`
- `Vue Router`
- `Pinia`
- `Axios`
- `Element Plus`
- `ECharts`

### 后端

- `Java 17`
- `Spring Boot 3.x`
- `Spring Security + JWT`
- `MyBatis-Plus`
- `MySQL 8`
- `Redis 7`

### 开发环境

- `Node.js 20+`
- `Maven 3.9+`
- `MySQL 8+`
- `Redis 7+`

---

## 目录结构

```text
WordSprint/
├─ frontend/
│  ├─ src/
│  │  ├─ api/
│  │  ├─ components/
│  │  ├─ layout/
│  │  ├─ router/
│  │  ├─ stores/
│  │  ├─ styles/
│  │  ├─ utils/
│  │  └─ views/
│  └─ package.json
├─ backend/
│  ├─ src/main/java/com/example/wordsprint/
│  │  ├─ common/
│  │  ├─ config/
│  │  ├─ controller/
│  │  ├─ dto/
│  │  ├─ entity/
│  │  ├─ mapper/
│  │  ├─ redis/
│  │  ├─ security/
│  │  ├─ service/
│  │  ├─ service/impl/
│  │  └─ vo/
│  └─ pom.xml
├─ sql/
│  ├─ init.sql
│  └─ seed_data.sql
├─ README.md
├─ FRONTEND_PAGE_DESIGN.md
├─ TECH_SPEC.md
├─ DATABASE_DESIGN.md
├─ API_LIST.md
├─ ITERATION_TODO.md
└─ HANDOFF.md
```

---

## 本地启动

### 1. 准备环境

确认本机已安装并可使用：

- `Java 17`
- `Maven 3.9+`
- `Node.js 20+`
- `MySQL 8+`
- `Redis 7+`

可先执行：

```bash
java -version
mvn -version
node -v
npm -v
```

### 2. 启动 MySQL 和 Redis

确认本地服务已运行：

- MySQL：`127.0.0.1:3306`
- Redis：`127.0.0.1:6379`

Windows 本机开发可直接使用 MySQL 服务和 Memurai。

### 3. 初始化数据库

当前默认开发配置：

- 数据库：`wordsprint`
- 用户名：`root`
- 密码：`123456`

执行初始化脚本：

```bash
mysql --host=127.0.0.1 --user=root --password=123456 --default-character-set=utf8mb4 < sql/init.sql
```

如需导入演示数据：

```bash
mysql --host=127.0.0.1 --user=root --password=123456 --default-character-set=utf8mb4 < sql/seed_data.sql
```

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认后端地址：

- `http://127.0.0.1:8080`
- 健康检查：`http://127.0.0.1:8080/api/health`

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认前端地址：

- `http://127.0.0.1:5173`

前端默认对接：

```env
VITE_API_BASE_URL=http://127.0.0.1:8080
```

---

## 演示账号

执行 `sql/seed_data.sql` 后可使用：

- 普通用户：`demo / 123456`
- 管理员：`admin / 123456`

---

## 当前验证结果

当前仓库已经验证通过的内容包括：

- 后端 `mvn test`
- 前端 `npm run build`
- `/api/health` 可访问
- 注册 -> 登录 -> 获取当前用户
- 单词卡 CRUD 与 CSV 导入
- 学习训练 -> 错题本 -> 打卡 -> 排行榜
- 学习统计、学习计划、个人中心
- 公共词库浏览、导入、管理员 CSV 导入

---

## 开发注意事项

- Redis 当前承担排行榜、签到状态、随机题池、防重复提交等职责
- 如果是先启动后端、后初始化数据库，排行榜缓存不会自动写入 Redis，初始化完成后需要重启后端
- 管理员公共词库导入接口要求 `ADMIN` 角色
- 前后端当前上传大小限制统一为 `20MB`

---

## 主要文档

- `AGENTS.md`：仓库内智能体工作约束
- `HANDOFF.md`：当前项目交接与本地环境说明
- `TECH_SPEC.md`：项目边界、技术规范与维护建议
- `FRONTEND_PAGE_DESIGN.md`：当前前端页面与交互设计说明
- `DATABASE_DESIGN.md`：表结构、索引和 Redis Key 设计
- `API_LIST.md`：接口清单与请求响应示例
- `ITERATION_TODO.md`：当前状态与仍可继续推进的事项
