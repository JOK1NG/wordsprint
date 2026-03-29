# WordSprint

一个轻量、有趣、适合 Java 学习的单词卡 / 记忆训练平台，采用 `Vue 3 + Spring Boot + Redis + MySQL` 的前后端分离架构。

---

## 项目简介

WordSprint 围绕“背单词”这个单一场景，提供单词卡管理、随机训练、错题回顾、学习打卡、积分排行和学习统计等能力。

这个项目的目标不是做成完整教育平台，而是做成一个技术闭环完整、能真正跑通、同时适合练手的前后端分离项目。

---

## 核心功能

- 用户注册、登录、JWT 鉴权
- 用户资料更新、学习计划设置（每日学习/复习目标、提醒开关）
- 单词卡新增、编辑、删除、分页查询
- 单词卡 CSV 导入（模板下载、导入结果反馈）
- 学习训练：看词忆义、看义拼词、选择题、错题重练
- 错题本自动收集与恢复掌握
- 每日学习打卡与连续学习天数统计
- 积分系统与排行榜
- 学习统计页：趋势、正确率、熟练度分布、打卡日历
- 公共词库浏览与一键导入
- 管理员公共词库 CSV 导入
- Redis 实际承担：排行榜、签到状态、防重复提交等职责

---

## 技术栈

### 前端

- `Vue 3`
- `Vite`
- `Vue Router`
- `Pinia`
- `Axios`
- `Element Plus`
- `ECharts`（可选）

### 后端

- `Java 17`
- `Spring Boot 3.x`
- `Spring Security + JWT`
- `MyBatis-Plus`
- `MySQL 8`
- `Redis 7`
- `Spring Validation`
- `SpringDoc OpenAPI / Knife4j`

### 开发环境

- `Node.js 20+`
- `Maven 3.9+`
- `IDEA`
- `Docker`（可选）

---

## 项目结构建议

```text
WordSprint/
├─ frontend/                  # Vue 项目
│  ├─ src/
│  │  ├─ api/
│  │  ├─ assets/
│  │  ├─ components/
│  │  ├─ composables/
│  │  ├─ layout/
│  │  ├─ router/
│  │  ├─ stores/
│  │  ├─ styles/
│  │  ├─ utils/
│  │  └─ views/
│  └─ package.json
├─ backend/                   # Spring Boot 项目
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
├─ sql/                       # 数据库初始化脚本
│  └─ init.sql
├─ README.md
├─ FRONTEND_PAGE_DESIGN.md
├─ WORD_CARD_DEV_PLAN.md
├─ DATABASE_DESIGN.md
├─ API_LIST.md
└─ ITERATION_TODO.md
```

---

## MVP 范围

第一版建议先完成下面 6 个闭环：

1. 用户注册登录
2. 单词卡 CRUD
3. 随机抽题训练
4. 答题提交与结果判定
5. 错题本
6. 打卡与积分榜

只要这 6 个点跑通，这个项目就已经具备完整的学习价值。

当前状态：以上 MVP 已完成，并已进入增强与收尾阶段。

---

## Redis 使用说明

本项目中的 Redis 不只是“接进来”，而是承担真实业务职责：

- `排行榜`：使用 `ZSet` 保存积分榜和连续打卡榜
- `签到状态`：记录用户当天是否已打卡
- `随机抽题池`：缓存用户当前训练题池，减少重复 SQL 抽取
- `热门统计缓存`：缓存首页热门词卡、公开词库、统计摘要
- `防重复提交`：保护答题提交接口，避免短时间重复提交

---

## 本地环境启动

当前仓库已经完成前后端基础骨架，下面是一份按本机开发环境整理过的启动说明。

### 1. 准备基础工具

推荐环境：

- `Java 17`
- `Maven 3.9+`
- `Node.js 20+`
- `MySQL 8+`
- `Redis 7+`
- `Homebrew`

如果你还没装这些工具，推荐直接用 Homebrew：

```bash
brew install openjdk@17 maven mysql redis
```

如果本机默认 Java 不是 17，先切到 Java 17：

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
```

验证环境：

```bash
java -version
mvn -version
node -v
npm -v
```

### 2. 启动 MySQL 和 Redis

使用 Homebrew 服务启动：

```bash
brew services start mysql
brew services start redis
```

确认服务已起来：

```bash
lsof -nP -iTCP:3306 -sTCP:LISTEN
lsof -nP -iTCP:6379 -sTCP:LISTEN
redis-cli ping
```

如果是第一次安装 MySQL，建议把 `root` 密码设成项目默认值 `123456`，这样和当前后端配置一致：

```bash
mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '123456';"
mysql -u root -e "CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED BY '123456';"
mysql -u root -e "ALTER USER 'root'@'127.0.0.1' IDENTIFIED BY '123456';"
mysql -u root -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION; FLUSH PRIVILEGES;"
```

### 3. 初始化数据库

执行初始化脚本：

```bash
mysql -h 127.0.0.1 -u root -p123456 < sql/init.sql
```

如需查看表结构和设计说明，可参考 `DATABASE_DESIGN.md`。

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认端口：`8080`

如果 `8080` 被占用，可以临时换端口：

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=18080"
```

启动后可访问健康检查：

```bash
curl http://127.0.0.1:8080/api/health
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认端口：`5173`

如果后端不是跑在 `8080`，例如你用了 `18080`，则前端启动前先覆盖接口地址：

```bash
cd frontend
VITE_API_BASE_URL=http://127.0.0.1:18080 npm run dev
```

### 6. 最小联调顺序

推荐按这个顺序验证：

1. 先启动 MySQL 和 Redis
2. 执行 `sql/init.sql`
3. 启动后端并访问 `/api/health`
4. 启动前端
5. 打开登录页，测试注册 -> 登录 -> 进入仪表盘
6. 再验证单词卡 CRUD

### 7. 常见问题

#### 1）后端启动失败，提示 JWT secret 相关错误

- 确认 `backend/src/main/resources/application-dev.yml` 中 `jwt.secret` 是非空字符串

#### 2）注册 / 登录返回 `500`

- 通常是 MySQL 没启动，或者数据库 `wordsprint` 还没初始化
- 先检查 `3306` 端口和 `sql/init.sql` 是否执行成功

#### 3）后端启动失败，提示端口占用

- 换一个端口启动，例如 `18080`

#### 4）前端页面能打开，但接口请求失败

- 检查 `VITE_API_BASE_URL` 是否和后端实际端口一致

#### 5）Redis 相关功能异常

- 先执行 `redis-cli ping`
- 返回 `PONG` 才说明 Redis 已正常运行

### 8. 停止本地服务

```bash
brew services stop mysql
brew services stop redis
```

---

## 环境变量建议

### 后端配置建议

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/wordsprint?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=123456

spring.data.redis.host=127.0.0.1
spring.data.redis.port=6379

jwt.secret=replace-with-your-secret
jwt.expire-hours=24
```

### 前端配置建议

```env
VITE_API_BASE_URL=http://127.0.0.1:8080
```

---

## 主要页面

- 登录页
- 注册页
- 首页 / 学习仪表盘
- 单词卡列表页
- 新增 / 编辑单词卡页
- 学习训练页
- 错题本页
- 排行榜页
- 学习统计页
- 个人中心页
- 公共词库页

---

## 主要文档

- `WORD_CARD_DEV_PLAN.md`：完整开发计划与开发规范
- `FRONTEND_PAGE_DESIGN.md`：前端页面结构、交互流程、数据需求设计
- `DATABASE_DESIGN.md`：数据库设计、建表 SQL、索引与 Redis Key 规划
- `API_LIST.md`：接口设计清单和请求响应样例
- `ITERATION_TODO.md`：分阶段开发任务拆解

---

## 推荐开发顺序

当前建议：

1. 先补示例数据（公共词库演示数据、学习统计演示数据）
2. 再完善 README 与 API 文档（保证可交接、可展示）
3. 再做 Redis 失效策略和交互细节收尾

---

## Git 提交规范

推荐使用下面的提交前缀：

```text
feat: add study submit api
fix: correct streak calculation
refactor: simplify word query service
docs: add database design
style: adjust dashboard layout
```

---

## 当前状态

当前仓库已完成 MVP 并落地多项增强能力，整体可用性与演示完整度较高。

### 已完成

- 用户与鉴权：注册、登录、当前用户、资料更新
- 学习计划：读取/更新每日学习与复习目标
- 单词卡：CRUD + CSV 导入 + 批量写入优化
- 学习训练：四种模式、答题提交、判题反馈、学习记录联动
- 错题本：列表、状态切换、移除/撤销、专项重练
- 打卡积分与排行榜：自动打卡、积分累计、积分榜/连续打卡榜（Redis ZSet）
- 学习统计：今日摘要、`WEEK/MONTH/ALL` 趋势、熟练度分布、打卡日历
- 公共词库：分页浏览、一键导入个人词库、管理员 CSV 批量导入
- 上传限制：前后端统一 `20MB`
- 阶段 12 部分优化：异常提示统一、核心页面空态/加载态/错误态补齐

### 当前已验证通过的能力

- 后端 `mvn test` 通过
- 后端可启动并访问 `/api/health`
- 注册 -> 登录 -> 获取当前用户流程可用
- 单词卡 CRUD 与 CSV 导入可用
- 学习训练 / 错题本 / 排行榜 / 学习统计链路可用
- 学习计划与个人中心联调可用
- 公共词库与管理员 CSV 导入可用
- 前端 `npm run build` 通过

### 下一步优先事项

- 准备示例数据（提升“开箱即用”体验）
- 完善 README 和 API 文档口径
- 优化 Redis 缓存失效策略与收尾交互

### 迁移对话时建议优先告知下一位智能体

- 本地环境已配置完成：`mysql`、`redis`、`wordsprint` 数据库、`sql/init.sql` 已跑过
- 当前后端默认端口为 `8080`（冲突时可切 `18080`）
- 若要测试管理员公共词库导入，请确保账号角色为 `ADMIN`
- 当前主要收尾点在：示例数据、文档完善、缓存策略优化
