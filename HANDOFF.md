# HANDOFF.md

本文档用于对话迁移、模型切换或多人接力开发时的快速交接。

---

## 1. 当前项目状态

项目名：`WordSprint`

项目定位：

- 轻量单词卡 / 记忆训练平台
- 技术栈：`Vue 3 + Spring Boot + Redis + MySQL`
- 当前阶段：已完成基础骨架、鉴权、单词卡 CRUD、学习训练主流程与关键增强（错题抽题/选择题/防重复提交）

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
- `ITERATION_TODO.md`
- `HANDOFF.md`
- `STUDY_REVIEW.md`

### 工程骨架

- `backend/` Spring Boot 项目已初始化
- `frontend/` Vue 3 + Vite 项目已初始化
- `sql/init.sql` 已生成并验证可执行

### 后端已完成

- 健康检查接口：`GET /api/health`
- 注册接口：`POST /api/auth/register`
- 登录接口：`POST /api/auth/login`
- 退出接口：`POST /api/auth/logout`
- 当前用户接口：`GET /api/user/me`
- 单词卡 CRUD：
  - `POST /api/word-cards`
  - `PUT /api/word-cards/{id}`
  - `DELETE /api/word-cards/{id}`
  - `GET /api/word-cards/{id}`
  - `GET /api/word-cards`
- 学习训练 MVP：
  - `GET /api/study/random`
  - `POST /api/study/submit`
- 学习训练联动增强：
  - 错题本自动维护
  - 每日打卡写入
  - 用户积分汇总更新
- 学习训练质量增强：
  - `CHOICE` 模式返回选项并可前端直接渲染
  - `WRONG_REVIEW` 按 `wrong_word.status=ACTIVE` 抽题
  - `submit` 增加 Redis 防重复提交（短 TTL）
  - 随机抽题改为“有限题池 + 内存随机”（替代 `ORDER BY RAND()`）

### 前端已完成

- 登录页已接入接口
- 注册页已接入接口
- token 存储已接好
- 路由守卫已接好
- 当前用户拉取逻辑已接好
- 退出登录入口已接好
- 单词卡列表/新增/编辑页面已联调
- 学习训练页面骨架已联调（模式切换、答题提交、结果反馈、下一题）

---

## 3. 已验证通过

### 后端

- `cd backend && mvn test` 通过
- 后端可启动并访问 `/api/health`
- 注册 -> 登录 -> 获取当前用户流程已真实验证通过
- 单词卡新增、详情、编辑、分页查询、删除已真实验证通过
- 学习训练随机出题与答题提交已真实验证通过
- 错题本 / 打卡 / 积分汇总联动已真实验证通过
- `CHOICE` 模式选项返回已真实验证通过
- `WRONG_REVIEW` 在错题 `RESOLVED` 后不再返回该题已真实验证通过
- 重复提交返回 `409`、不同答案快速提交可通过已真实验证通过

### 前端

- `cd frontend && npm install` 通过
- `cd frontend && npm run build` 通过
- 前端开发服务器可正常启动

---

## 4. 本地环境状态

当前本机开发环境已配置完成：

- `mysql` 已通过 Homebrew 安装并启动
- `redis` 已通过 Homebrew 安装并启动
- `wordsprint` 数据库已初始化
- `sql/init.sql` 已执行过

当前后端配置默认使用：

- MySQL：`127.0.0.1:3306`
- 用户名：`root`
- 密码：`123456`
- Redis：`127.0.0.1:6379`

注意：

- 本机 `8080` 可能被其他程序占用
- 如后端端口冲突，优先使用 `18080`

---

## 5. 当前未完成内容

### 高优先级

- 错题本模块
  - 错题列表接口
  - 手动移出错题接口
  - 错题专项训练接口
  - 前端错题本页面联调

- 学习训练后续增强
  - `GET /api/study/today-summary`
  - `GET /api/study/statistics`
  - Redis 随机题池会话化（当前为有限题池随机）

### 中优先级

- 用户资料更新接口
- 错题本列表 / 专项训练页面
- 排行榜模块
- 学习统计模块

---

## 6. 下一步推荐顺序

推荐下一位智能体按这个顺序继续：

1. 推进错题本模块（接口 + 前端）
2. 推进学习摘要与统计接口
3. 推进排行榜模块
4. 视情况再补 Redis 题池会话化

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
- 当前优先级仍然是：`MVP 闭环`，不是功能扩张
- 如果修改接口、数据库、文档约定，必须同步更新相应文档

---

## 9. 关键结论

当前项目已经不是纯规划状态，而是：

- 环境可用
- 骨架可跑
- 鉴权可用
- 单词卡前后端闭环可用
- 学习训练主流程可用（含错题/打卡/积分联动）

最值得继续投入的地方，是把 `错题本`、`学习统计`、`排行榜` 三个模块补齐，形成完整 MVP 闭环。
