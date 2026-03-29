# HANDOFF.md

本文档用于对话迁移、模型切换或多人接力开发时的快速交接。

---

## 1. 当前项目状态

项目名：`WordSprint`

项目定位：

- 轻量单词卡 / 记忆训练平台
- 技术栈：`Vue 3 + Spring Boot + Redis + MySQL`
- 当前阶段：**MVP 核心功能已完成** ✅
  - 基础骨架、鉴权、单词卡 CRUD ✅
  - 学习训练主流程 ✅
  - 错题本模块（接口 + 前端页面）✅
  - 排行榜模块（Redis ZSet + 前端页面）✅
  - 学习打卡与积分联动 ✅

---

## 2. 已完成内容

### 文档

以下文档已存在并已补齐：

- `AGENTS.md`
- `README.md`
- `WORD_CARD_DEV_PLAN.md`
- `FRONTEND_PAGE_DESIGN.md`
- `DATABASE_DESIGN.md`
- `API_LIST.md`
- `ITERATION_TODO.md`（已更新 MVP 完成状态）
- `HANDOFF.md`
- `STUDY_REVIEW.md`

### 工程骨架

- `backend/` Spring Boot 项目已初始化
- `frontend/` Vue 3 + Vite 项目已初始化
- `sql/init.sql` 已生成并验证可执行

### 后端已完成

**基础接口：**
- 健康检查接口：`GET /api/health`
- 注册接口：`POST /api/auth/register`
- 登录接口：`POST /api/auth/login`
- 退出接口：`POST /api/auth/logout`
- 当前用户接口：`GET /api/user/me`

**单词卡 CRUD：**
- `POST /api/word-cards`
- `PUT /api/word-cards/{id}`
- `DELETE /api/word-cards/{id}`
- `GET /api/word-cards/{id}`
- `GET /api/word-cards`

**学习训练模块：**
- `GET /api/study/random`（支持四种模式：WORD_TO_MEANING/MEANING_TO_WORD/CHOICE/WRONG_REVIEW）
- `POST /api/study/submit`
- 错题本自动维护（答错自动记录，连续答对3次自动标记 RESOLVED）
- 每日打卡自动写入
- 用户积分实时更新
- Redis 防重复提交锁
- 有限题池随机抽题（替代 ORDER BY RAND()）

**错题本模块：**
- `GET /api/wrong-words`（分页 + 状态筛选 + 活跃/累计统计）
- `POST /api/wrong-words/{wordCardId}/remove`（手动标记 RESOLVED）
- `POST /api/wrong-words/{wordCardId}/restore`（撤销移除，标记 ACTIVE）
- `GET /api/wrong-words/practice`（错题专项训练抽题）
- `WrongWordService` / `WrongWordController` / `WrongWordVO` 完整实现

**排行榜模块（Redis）：**
- `GET /api/rank/points`（积分榜，Redis ZSet 实现）
- `GET /api/rank/streak`（连续打卡榜，Redis ZSet 实现）
- `RedisRankService`（封装 ZADD/ZREVRANK/ZREVRANGE 操作）
- 积分/打卡数据实时同步到 Redis
- 自动兜底（Redis 无数据时从 MySQL 加载）

### 前端已完成

**基础页面：**
- 登录页已接入接口
- 注册页已接入接口
- 学习仪表盘（Dashboard）

**单词卡模块：**
- 单词卡列表/新增/编辑页面已联调
- 分页、筛选、搜索功能完整

**学习训练模块：**
- 学习训练页面（`views/study/StudyTrainingView.vue`）
- 四种模式切换（看词忆义/看义拼词/选择题/错题重练）
- 答题提交与结果反馈
- 下一题/本轮总结

**错题本模块：**
- 错题本页面（`views/wrongword/WrongWordListView.vue`）✅
- 错题列表表格（单词/词义/错误次数/时间/状态）
- 状态筛选标签（全部/活跃/已解决）
- 统计卡片（活跃错题数/累计错题数）✅
- 操作按钮（重练/移除/撤销）
- 分页器
- 空状态提示
- 侧边栏导航入口

**排行榜模块：**
- 排行榜页面（`views/rank/RankView.vue`）✅
- 积分榜/连续打卡榜切换
- 我的排名卡片（排名/分数/头像）
- 榜单表格（前三名金/银/铜样式）
- 当前用户行高亮
- 侧边栏导航入口（🏆 排行榜）

**路由配置：**
- `/login` - 登录
- `/register` - 注册
- `/` - 仪表盘
- `/word-cards` - 单词卡列表
- `/word-cards/create` - 新增单词卡
- `/word-cards/edit/:id` - 编辑单词卡
- `/study` - 学习训练
- `/wrong-words` - 错题本 ✅
- `/rank` - 排行榜 ✅

---

## 3. 已验证通过

### 后端

- `cd backend && mvn test` 通过
- 后端可启动并访问 `/api/health`
- 注册 -> 登录 -> 获取当前用户流程已真实验证通过
- 单词卡 CRUD 已真实验证通过
- 学习训练随机出题与答题提交已真实验证通过
- 错题本自动维护（答错记录/连续答对移除）已真实验证通过
- `CHOICE` 模式选项返回已真实验证通过
- `WRONG_REVIEW` 错题重练模式已真实验证通过
- 重复提交返回 `409` 已真实验证通过
- **排行榜 Redis 实时更新已真实验证通过** ✅
- **错题本统计（活跃/累计）已真实验证通过** ✅

### 前端

- `cd frontend && npm install` 通过
- `cd frontend && npm run build` 通过
- 前端开发服务器可正常启动
- **错题本页面联调通过**（列表/筛选/移除/撤销/统计）✅
- **排行榜页面联调通过**（积分榜/连续打卡榜/我的排名）✅

---

## 4. 本地环境状态

当前本机开发环境已配置完成：

- `mysql` 已通过 Homebrew 安装并启动
- `redis` 已通过 Homebrew 安装并启动
- `wordsprint` 数据库已初始化
- `sql/init.sql` 已执行过
- **bun** 已安装（用于 gstack browse 截图测试）

当前后端配置默认使用：

- MySQL：`127.0.0.1:3306`
- 用户名：`root`
- 密码：`123456`
- Redis：`127.0.0.1:6379`

注意：

- 本机 `8080` 可能被其他程序占用
- 如后端端口冲突，优先使用 `18080`
- **当前后端运行在 18080 端口**

---

## 5. 当前未完成内容

### 已标记为完成（MVP 7/7 项）

根据 `ITERATION_TODO.md`，MVP 完成标准已达成：

- [x] 注册登录
- [x] 单词卡 CRUD
- [x] 学习训练
- [x] 错题本（含统计）
- [x] 打卡
- [x] 积分榜
- [x] Redis 已实际承担排行榜或缓存职责

### 可选增强（第二阶段）

以下功能可在 MVP 基础上继续扩展：

- 学习统计模块
  - `GET /api/study/today-summary`
  - `GET /api/study/statistics`
  - 学习趋势图表
  - 正确率统计
  - 打卡日历热力图

- Redis 优化
  - 随机题池会话化（当前为有限题池随机）
  - 热点数据缓存

- 用户功能
  - 用户资料更新接口
  - 头像上传

- 数据导入导出
  - CSV 导入词卡
  - 公共词库

---

## 6. 下一步推荐顺序

**如果继续开发，推荐优先级：**

1. **学习统计模块**（仪表盘数据增强、趋势图表）
2. **Redis 缓存优化**（热点数据缓存、题池会话化）
3. **用户资料功能**（头像上传、资料编辑）
4. **数据导入导出**（CSV 导入、公共词库）

**如果验收 MVP，请验证：**

1. 注册 -> 登录 -> 单词卡 CRUD -> 学习训练 -> 错题本 -> 排行榜
2. 答题后检查：错题本是否记录、积分是否增加、排行榜是否更新
3. Redis 数据检查：`redis-cli ZRANGE wordsprint:rank:points 0 -1 WITHSCORES`

---

## 7. 启动命令

### 后端

```bash
cd backend
mvn spring-boot:run
```

如果 `8080` 被占用：

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=18080"
```

### 前端

```bash
cd frontend
npm run dev
```

如果后端跑在 `18080`：

```bash
cd frontend
VITE_API_BASE_URL=http://127.0.0.1:18080 npm run dev
```

---

## 8. 重要注意事项

- 先读：`AGENTS.md`
- 再读：`HANDOFF.md`
- 不要擅自扩展为大而全平台
- 不要跳过文档直接改代码
- **MVP 已完成**，后续开发请遵循文档优先级
- 如果修改接口、数据库、文档约定，必须同步更新相应文档

---

## 9. 关键结论

**当前项目状态：MVP 核心功能已完成并验证通过**

- ✅ 环境可用（MySQL + Redis + 前后端服务）
- ✅ 鉴权可用（注册/登录/JWT）
- ✅ 单词卡前后端闭环可用
- ✅ 学习训练主流程可用（含错题/打卡/积分联动）
- ✅ **错题本模块完整可用（接口 + 前端页面 + 统计）** ✅
- ✅ **排行榜模块完整可用（Redis ZSet + 前端页面）** ✅
- ✅ Redis 已实际承担排行榜职责

**MVP 第一版已完成，具备完整的学习训练闭环能力。**

如果继续开发，建议聚焦：**学习统计模块**（仪表盘数据增强、趋势图表）。
