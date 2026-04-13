# HANDOFF.md

本文档用于对话迁移、模型切换或多人接力开发时的快速交接。

---

## 1. 当前项目状态

项目名：`WordSprint`

项目定位：

- 轻量单词卡 / 记忆训练平台
- 技术栈：`Vue 3 + Spring Boot + Redis + MySQL`
- 当前阶段：**MVP 已完成 + 增强功能持续推进中**

当前阶段结论：

- MVP 主流程闭环可用（注册登录 -> 词卡 -> 学习训练 -> 错题 -> 打卡积分 -> 排行榜）
- 学习统计与仪表盘已落地（统计页可用）
- 用户资料与学习计划已落地
- CSV 导入（个人词卡 + 管理员公共词库）已落地
- 公共词库浏览 + 一键导入已落地

---

## 2. 已完成内容

### 文档

以下文档已存在并同步更新：

- `AGENTS.md`
- `README.md`
- `WORD_CARD_DEV_PLAN.md`
- `FRONTEND_PAGE_DESIGN.md`
- `DATABASE_DESIGN.md`
- `API_LIST.md`
- `ITERATION_TODO.md`
- `HANDOFF.md`
- `STUDY_REVIEW.md`
- `sql/seed_data.sql`（示例数据）

### 工程骨架

- `backend/` Spring Boot 项目已可运行
- `frontend/` Vue 3 + Vite 项目已可运行
- `sql/init.sql` 可执行
- `sql/seed_data.sql` 可执行（在 init.sql 之后运行）

### 后端已完成

**基础与鉴权：**

- `GET /api/health`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/user/me`
- `PUT /api/user/profile`
- 全局异常状态码修复：业务失败不再错误返回 HTTP 200

**学习计划：**

- `GET /api/study-plan`
- `PUT /api/study-plan`
- 支持默认计划回填（用户无记录时返回默认值）

**单词卡：**

- `POST /api/word-cards`
- `PUT /api/word-cards/{id}`
- `DELETE /api/word-cards/{id}`
- `GET /api/word-cards/{id}`
- `GET /api/word-cards`
- `POST /api/word-cards/import/csv`

**学习训练：**

- `GET /api/study/random`
- `POST /api/study/submit`
- 错题自动维护、打卡自动写入、积分联动

**错题本：**

- `GET /api/wrong-words`
- `POST /api/wrong-words/{wordCardId}/remove`
- `POST /api/wrong-words/{wordCardId}/restore`
- `GET /api/wrong-words/practice`

**排行榜：**

- `GET /api/rank/points`
- `GET /api/rank/streak`
- Redis ZSet 实时更新 + MySQL 兜底

**学习统计：**

- `GET /api/study/today-summary`
- `GET /api/study/statistics`（`WEEK` / `MONTH` / `ALL`）
- `GET /api/study/familiarity-distribution`
- `GET /api/checkin/calendar`

**公共词库：**

- `GET /api/public-words`
- `POST /api/public-words/{id}/import`
- `POST /api/admin/public-words/import/csv`（管理员）

**导入与上传限制：**

- 个人词卡 CSV 导入支持批量写入优化
- 公共词库 CSV 导入支持批量写入优化（新增/更新）
- 前后端统一上传上限：`20MB`

**示例数据（`sql/seed_data.sql`）：**

- 演示账号 `demo / 123456`（普通用户，含 25 张词卡 + 80 条学习记录 + 7 天打卡 + 积分汇总）
- 管理员账号 `admin / 123456`（可使用管理员 CSV 导入功能）
- 公共词库 50 条（CET4=20, CET6=15, IELTS=15）
- demo 用户词卡覆盖 5 种记忆状态 + 6 个熟练度等级
- 错题本 6 条（3 ACTIVE + 3 RESOLVED）

### 前端已完成

**页面与路由：**

- `/login` 登录
- `/register` 注册
- `/` 学习仪表盘
- `/word-cards` 单词卡列表
- `/word-cards/create` 新增单词卡
- `/word-cards/edit/:id` 编辑单词卡
- `/study` 学习训练
- `/wrong-words` 错题本
- `/rank` 排行榜
- `/statistics` 学习统计
- `/profile` 个人中心
- `/public-library` 公共词库

**功能联调：**

- 登录/注册/获取当前用户
- 单词卡 CRUD + CSV 导入（模板下载 + 结果反馈）
- 学习训练四模式 + 答题反馈 + 本轮总结
- 错题本列表/筛选/重练/移除/撤销
- 排行榜（积分榜/连续打卡榜）
- 仪表盘（今日摘要、7 天趋势、学习/复习目标达成）
- 学习统计页（范围切换、趋势、熟练度分布、打卡日历）
- 个人中心（资料更新 + 学习计划设置）
- 公共词库页（分页/筛选/导入 + 管理员 CSV 导入）

**体验优化（阶段 12 部分）：**

- 核心页面补齐加载失败态与重试入口
- 前端错误提示提取逻辑统一（`extractErrorMessage`）
- 顶部标题映射补齐（不再大量回退“学习仪表盘”）

---

## 3. 已验证通过

### 后端

- `cd backend && mvn test` 通过
- 后端可启动并访问 `/api/health`
- 注册 -> 登录 -> 获取当前用户流程通过
- 单词卡 CRUD 通过
- 学习训练 / 错题 / 打卡 / 积分联动通过
- 排行榜 Redis 联动通过
- 学习统计接口（today-summary/statistics/distribution/calendar）通过
- 学习计划接口通过
- 公共词库列表与导入通过
- 管理员 CSV 导入公共词库通过
  - 非管理员：`403`
  - 管理员：`200`
- 上传超限统一返回：`上传文件不能超过 20MB`

### 前端

- `cd frontend && npm run build` 通过
- 主要页面路由可达
- 关键业务链路联调通过：
  - 登录 -> 仪表盘
  - 词卡导入/管理 -> 学习训练 -> 错题本 -> 排行榜
  - 统计页与个人中心数据可读写
  - 公共词库浏览与导入

---

## 4. 本地环境状态

当前本机开发环境：

- MySQL：`127.0.0.1:3306`
- Redis：`127.0.0.1:6379`
- 数据库：`wordsprint`
- SQL 初始化文件：`sql/init.sql`
- SQL 种子数据文件：`sql/seed_data.sql`（公共词库 50 条 + demo 用户 25 张词卡 + 80 条学习记录 + 7 天打卡 + 积分汇总）
- 演示账号：`demo / 123456`（普通用户，有学习数据）、`admin / 123456`（管理员）

后端配置（dev）：

- DB 用户：`root`
- DB 密码：`123456`

端口说明：

- 默认后端：`8080`
- 若冲突可用：`18080`
- **当前最近一次联调在 `8080` 完成**

---

## 5. 当前未完成内容

结合 `ITERATION_TODO.md`，当前主要剩余项：

### 阶段 10 / 11 的可选增强

- 统计页图表组件接入（目前为轻量可视化，不依赖图表库）

### 阶段 12（优化与收尾）

- 补充日志
- 优化 Redis 缓存失效策略
- 修复明显交互问题

---

## 6. 下一步推荐顺序

推荐按以下顺序继续：

1. **Redis 缓存策略收口**（热点缓存与失效策略明确化）
2. **交互细节修复**（空态、重试路径、提示语一致性再打磨）
3. **补充日志**（关键业务流程结构化日志）

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

- 先读：`AGENTS.md`，再读：`HANDOFF.md`
- 遵循 MVP 边界，不扩成大而全平台
- 修改接口/数据库/约定必须同步文档
- 管理员公共词库导入接口需 `ADMIN` 角色
- CSV 相关上传限制统一为 `20MB`

---

## 9. 关键结论

**当前项目状态：MVP 完成，增强功能已进入可用阶段，示例数据已就绪。**

- ✅ 核心学习闭环可用
- ✅ 统计、个人中心、学习计划可用
- ✅ 公共词库 + CSV 导入链路可用
- ✅ 管理员公共词库 CSV 导入可用（含权限与大小限制）
- ✅ 关键构建与测试通过
- ✅ 示例数据已就绪（50 条公共词库 + demo 用户 25 张词卡 + 80 条学习记录 + 7 天打卡 + 积分汇总）

当前适合进入收尾打磨阶段。
