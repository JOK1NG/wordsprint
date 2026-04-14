<!-- markdownlint-disable MD024 -->
# WordSprint 前端页面设计方案

本文档用于描述当前前端页面结构、交互重点和页面与接口的对应关系，只保留仍然有效的设计信息。

---

## 1. 设计目标

- 优先保证学习主流程完整
- 页面信息清晰，减少认知负担
- 前端页面能直接反推接口字段和状态需求
- 视觉和交互保持轻量，不做与 MVP 无关的复杂设计

---

## 2. 设计原则

### 2.1 产品原则

- 首页先回答“今天该学什么”
- 学习页先回答“当前这题怎么答”
- 单词卡页先回答“怎么快速整理和筛选”
- 统计页先回答“最近学得怎么样”

### 2.2 交互原则

- 每个页面只保留一个主操作
- 列表筛选集中在顶部
- 所有异步流程都需要加载、空状态和错误提示
- 高频操作放在用户主视线区域

### 2.3 UI 原则

- 延续当前浅色内容区 + 左侧导航布局
- 以 `Element Plus` 现成组件为主
- 强调数据卡片、训练反馈和状态可读性
- 移动端优先纵向堆叠

---

## 3. 当前页面地图

```text
/login                 登录页
/register              注册页
/                      重定向到 /dashboard
/dashboard             学习仪表盘
/word-cards            单词卡列表
/word-cards/create     新增单词卡
/word-cards/edit/:id   编辑单词卡
/study                 学习训练
/wrong-words           错题本
/rank                  排行榜
/statistics            学习统计
/profile               个人中心
/public-library        公共词库
```

说明：

- 学习计划当前整合在 `/profile` 页面里，没有单独拆成 `/study-plan`
- 根路径 `/` 当前只作为登录后的入口跳转

---

## 4. 全局布局设计

### 4.1 桌面端布局

```text
┌──────────────┬──────────────────────────────┐
│ 侧边栏       │ 顶部信息区 / 页面主内容      │
│ Logo         │                              │
│ 导航菜单      │                              │
│ 用户信息入口   │                              │
└──────────────┴──────────────────────────────┘
```

### 4.2 侧边栏导航

- 学习仪表盘
- 单词卡
- 学习训练
- 错题本
- 排行榜
- 学习统计
- 公共词库
- 个人中心

### 4.3 移动端布局

- 侧边栏收起成抽屉菜单
- 卡片和表单纵向排列
- 高频按钮优先保持可点击面积

---

## 5. 页面详细设计

## 5.1 登录页 `/login`

### 页面目标

让用户快速完成登录并进入仪表盘。

### 页面模块

- Logo / 项目名
- 登录表单
- 登录按钮
- 去注册入口

### 需要接口

- `POST /api/auth/login`
- `GET /api/user/me`

---

## 5.2 注册页 `/register`

### 页面目标

完成最小用户创建流程。

### 页面模块

- Logo / 项目名
- 注册表单
- 注册按钮
- 去登录入口

### 需要接口

- `POST /api/auth/register`

---

## 5.3 学习仪表盘 `/dashboard`

### 页面目标

把“今天该做什么”和“当前状态如何”放到一个页面中。

### 页面模块

- 顶部欢迎区
- 今日学习摘要卡片
- 学习 / 复习目标进度
- 连续学习天数、积分、排名
- 快速开始学习入口
- 最近趋势简图

### 需要接口

- `GET /api/study/today-summary`
- `GET /api/study/statistics?rangeType=WEEK`
- `GET /api/study-plan`

---

## 5.4 单词卡列表页 `/word-cards`

### 页面目标

高效管理自己的单词卡。

### 页面模块

- 页面标题 + 新增按钮
- 搜索框
- 筛选区
- 单词卡表格
- 分页器

### 需要接口

- `GET /api/word-cards`
- `GET /api/word-cards/{id}`
- `DELETE /api/word-cards/{id}`

---

## 5.5 新增 / 编辑单词卡页

### 页面目标

低阻力录入和修改单词卡。

### 页面模块

- 返回按钮
- 表单区
- 保存按钮

### 需要接口

- `POST /api/word-cards`
- `PUT /api/word-cards/{id}`
- `GET /api/word-cards/{id}`

---

## 5.6 学习训练页 `/study`

### 页面目标

让用户尽快进入连续答题状态。

### 页面模块

- 学习模式切换
- 当前进度
- 题目卡片
- 提交区 / 选项区
- 判题反馈区
- 下一题按钮
- 本轮总结

### 需要接口

- `GET /api/study/random`
- `POST /api/study/submit`
- `GET /api/study/today-summary`

---

## 5.7 错题本页 `/wrong-words`

### 页面目标

帮助用户集中复习薄弱项。

### 页面模块

- 页面标题
- 状态筛选
- 错题列表
- 错题专项训练入口

### 需要接口

- `GET /api/wrong-words`
- `POST /api/wrong-words/{wordCardId}/remove`
- `POST /api/wrong-words/{wordCardId}/restore`
- `GET /api/wrong-words/practice`

---

## 5.8 排行榜页 `/rank`

### 页面目标

展示成长反馈和轻量竞争感。

### 页面模块

- 榜单类型切换
- 我的排名卡片
- 排行榜列表

### 列表重点字段

- 排名
- 头像
- 用户昵称
- 分数 / 连续天数

### 需要接口

- `GET /api/rank/points`
- `GET /api/rank/streak`

---

## 5.9 学习统计页 `/statistics`

### 页面目标

展示学习结果和阶段趋势。

### 页面模块

- 时间范围切换
- 统计摘要卡片
- 趋势图
- 熟练度分布图
- 打卡日历

### 需要接口

- `GET /api/study/statistics`
- `GET /api/study/familiarity-distribution`
- `GET /api/checkin/calendar`

---

## 5.10 个人中心页 `/profile`

### 页面目标

管理用户资料和学习计划。

### 页面模块

- 头像与昵称信息
- 用户名展示
- 学习计划设置
- 退出登录按钮

### 需要接口

- `GET /api/user/me`
- `PUT /api/user/profile`
- `GET /api/study-plan`
- `PUT /api/study-plan`

---

## 5.11 公共词库页 `/public-library`

### 页面目标

降低首次使用门槛，让用户可以直接导入现成词库。

### 页面模块

- 词库筛选
- 列表分页
- 一键导入按钮
- 管理员 CSV 导入入口

### 需要接口

- `GET /api/public-words`
- `POST /api/public-words/{id}/import`
- `POST /api/admin/public-words/import/csv`

---

## 6. 当前组件结构

## 6.1 页面级组件

- `views/LoginView.vue`
- `views/RegisterView.vue`
- `views/DashboardView.vue`
- `views/wordcard/WordCardListView.vue`
- `views/wordcard/WordCardFormView.vue`
- `views/study/StudyTrainingView.vue`
- `views/wrongword/WrongWordListView.vue`
- `views/rank/RankView.vue`
- `views/statistics/StatisticsView.vue`
- `views/profile/ProfileView.vue`
- `views/publicword/PublicWordLibraryView.vue`

## 6.2 业务组件

- `components/common/PageHeader.vue`
- `components/common/SummaryCard.vue`
- `components/dashboard/WelcomeSection.vue`
- `components/dashboard/GoalProgressCard.vue`
- `components/dashboard/TrendList.vue`
- `components/study/ModeSelector.vue`
- `components/study/QuestionCard.vue`
- `components/study/SessionSummary.vue`
- `components/rank/MyRankCard.vue`
- `components/rank/RankTable.vue`
- `components/statistics/TrendChart.vue`
- `components/statistics/FamiliarityChart.vue`
- `components/statistics/CheckinCalendar.vue`
- `components/statistics/ExtraInfoCard.vue`

---

## 7. 路由守卫与登录态

### 7.1 公开页面

- `/login`
- `/register`

### 7.2 受保护页面

- 其他所有业务页

### 7.3 守卫规则

- 无 token 访问业务页时跳转 `/login`
- 已登录用户访问 `/login` 或 `/register` 时跳转 `/dashboard`
- 401 时清空 token 并返回登录页

---

## 8. 状态设计

### 8.1 空状态

- 单词卡为空：提示先添加单词卡
- 错题本为空：提示当前没有活跃错题
- 排行榜为空：展示默认占位

### 8.2 错误状态

- 网络错误：统一消息提示
- 列表加载失败：提供重试入口
- 学习题获取失败：允许重新开始本轮训练

---

## 9. 当前落地说明

- 根路径会进入布局后重定向到 `/dashboard`
- 公共词库已经是当前正式页面，不再属于“后续可选页面”
- 学习计划能力已经并入个人中心，不单独拆页
- 排行榜当前已经完成头像展示链路，接口返回 `avatar`，前端表格和“我的排名”卡片都能消费该字段

---

## 10. 一句话结论

WordSprint 的前端仍然围绕三件事组织：

- 今天学什么
- 当前这题怎么答
- 最近学得怎么样

只要页面继续围绕这三件事展开，功能扩展就不会跑偏。
