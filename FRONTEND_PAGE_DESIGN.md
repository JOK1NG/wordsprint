# WordSprint 前端页面设计方案

本文档用于约束 WordSprint 的前端页面结构、交互流程和数据需求。目标不是先做高保真视觉稿，而是先把页面信息架构、主要模块和接口需求定清楚，减少后续返工。

---

## 1. 设计目标

- 优先保证学习主流程闭环
- 页面结构简单、清晰、可快速实现
- 让前端页面直接反推后端接口字段
- 保持轻量，不做复杂动画和重社交化设计

---

## 2. 设计原则

### 2.1 产品原则

- 首页先告诉用户今天该学什么
- 学习页先让用户尽快进入答题状态
- 单词卡页强调录入、筛选、整理
- 错题本页强调复习和清空反馈
- 统计页强调成长感，不堆砌无用图表

### 2.2 交互原则

- 每个页面只保留一个主操作
- 列表筛选集中在顶部
- 高频按钮始终放在用户视线主区域
- 所有异步状态必须有加载、空状态、错误提示

### 2.3 UI 原则

- 延续现在已有的深色侧边栏 + 浅色内容区结构
- 使用 `Element Plus` 现成组件为主
- 视觉重点放在数据卡片、训练卡片、进度反馈
- 移动端优先纵向堆叠，不保留复杂双栏布局

---

## 3. 页面地图

## 3.1 MVP 页面

```text
/login              登录页
/register           注册页
/                   学习仪表盘
/word-cards         单词卡列表页
/word-cards/create  新增单词卡页
/word-cards/edit/:id 编辑单词卡页
/study              学习训练页
/wrong-words        错题本页
/rank               排行榜页
/statistics         学习统计页
/profile            个人中心页
```

## 3.2 可选增强页面

```text
/public-library     公共词库页
/study-plan         学习计划页
```

---

## 4. 全局布局设计

## 4.1 桌面端布局

```text
┌──────────────┬──────────────────────────────┐
│ 侧边栏       │ 顶部信息区 / 页面主内容      │
│ Logo         │                              │
│ 导航菜单      │                              │
│ 我的进度入口   │                              │
└──────────────┴──────────────────────────────┘
```

### 侧边栏导航建议

- 学习仪表盘
- 单词卡
- 学习训练
- 错题本
- 排行榜
- 学习统计
- 个人中心

### 顶部区域建议

- 页面标题
- 简要说明
- 当前用户昵称
- 快捷操作按钮，如“开始学习”

## 4.2 移动端布局

- 侧边栏收起成抽屉菜单
- 所有卡片纵向排列
- 列表筛选收进折叠面板
- 学习页按钮区固定底部，便于连续答题

---

## 5. 页面详细设计

## 5.1 登录页 `/login`

### 页面目标

让用户快速进入系统。

### 页面模块

- Logo / 项目名
- 登录表单
- 登录按钮
- 去注册入口

### 表单字段

- 用户名
- 密码

### 核心交互

1. 用户输入用户名和密码
2. 点击登录
3. 调用 `/api/auth/login`
4. 成功后保存 token
5. 拉取 `/api/user/me`
6. 跳转到 `/`

### 状态设计

- 提交中按钮 loading
- 登录失败显示后端错误消息
- 已登录用户访问时直接跳转仪表盘

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

### 表单字段

- 用户名
- 昵称
- 密码

### 核心交互

1. 用户填写注册信息
2. 点击注册
3. 调用 `/api/auth/register`
4. 注册成功后提示用户去登录，或者直接跳转登录页

### 状态设计

- 字段前端校验
- 用户名重复时展示业务错误

### 需要接口

- `POST /api/auth/register`

---

## 5.3 学习仪表盘 `/`

### 页面目标

把“今天该做什么”和“现在状态如何”放在一个页面里。

### 页面模块

- 顶部欢迎区
- 今日学习摘要卡片
- 今日目标进度卡片
- 当前连续学习天数卡片
- 当前积分 / 排名卡片
- 快速开始学习入口
- 待复习数量提示
- 最近学习趋势简图

### 建议布局

```text
欢迎区 + 开始学习按钮
四个摘要卡片
今日进度 + 待复习
最近趋势图
```

### 核心交互

- 点击“开始学习”进入 `/study`
- 点击“错题复习”进入 `/wrong-words`
- 点击“查看统计”进入 `/statistics`

### 需要数据

- 今日学习数
- 今日正确数
- 今日正确率
- 今日目标数量
- 当前连续天数
- 当前积分
- 当前排名
- 待复习数量
- 最近 7 天学习趋势

### 需要接口

- `GET /api/study/today-summary`
- `GET /api/study/statistics?rangeType=WEEK`
- `GET /api/study-plan`

---

## 5.4 单词卡列表页 `/word-cards`

### 页面目标

让用户高效管理自己的单词卡。

### 页面模块

- 页面标题 + 新增按钮
- 搜索框
- 筛选区
- 单词卡表格 / 卡片列表
- 分页器

### 筛选项

- 关键字
- 标签
- 记忆状态
- 熟练度范围
- 是否只看错题

### 列表字段

- 单词
- 词义
- 标签
- 记忆状态
- 熟练度
- 错误次数
- 最近学习时间
- 操作列：查看 / 编辑 / 删除

### 核心交互

- 点击“新增单词卡”进入 `/word-cards/create`
- 点击“编辑”进入 `/word-cards/edit/:id`
- 点击“删除”弹确认框
- 点击某一行可查看详情抽屉或详情弹窗

### 需要接口

- `GET /api/word-cards`
- `GET /api/word-cards/{id}`
- `DELETE /api/word-cards/{id}`

---

## 5.5 新增 / 编辑单词卡页

### 页面目标

低阻力录入单词卡。

### 页面模块

- 返回按钮
- 表单区
- 保存按钮

### 表单字段

- 单词
- 音标
- 词义
- 例句
- 标签
- 是否公开

### 核心交互

- 新增页提交时调用 `POST /api/word-cards`
- 编辑页加载详情后调用 `PUT /api/word-cards/{id}`
- 保存成功后回到列表页

### 字段要求

- `word` 必填
- `meaning` 必填
- 其他字段可选

---

## 5.6 学习训练页 `/study`

### 页面目标

这是本项目最核心的页面，要让用户尽快进入连续答题状态。

### 页面模块

- 顶部训练模式切换
- 当前进度条
- 题目卡片
- 答案输入区 / 选项区
- 提交按钮
- 判题反馈区
- 下一题按钮
- 本轮统计摘要

### 训练模式

- 看单词回忆中文
- 看中文拼写英文
- 选择题
- 错题重练

### 题目卡片结构

- 模式标题
- 题目主体
- 辅助信息，如音标或例句
- 当前题号 / 总题数

### 提交流程

1. 页面进入后按模式请求题目
2. 渲染第一题
3. 用户提交答案
4. 调用 `/api/study/submit`
5. 展示正确与否、正确答案、当前积分变化
6. 点击下一题继续

### 页面状态

- 未开始：展示模式选择和开始按钮
- 进行中：展示题目和答题区
- 已提交：展示结果反馈并锁定本题输入
- 已完成：展示本轮总结卡片

### 需要接口

- `GET /api/study/random`
- `POST /api/study/submit`
- `GET /api/study/today-summary`

### 页面实现注意点

- 不要一次塞太多装饰信息
- 提交后反馈要立即可见
- 移动端按钮区保持大尺寸可点击

---

## 5.7 错题本页 `/wrong-words`

### 页面目标

帮助用户集中复习薄弱项。

### 页面模块

- 页面标题
- 状态筛选
- 错题列表
- 批量开始错题训练入口

### 列表字段

- 单词
- 词义
- 错误次数
- 最近答错时间
- 当前状态
- 操作：重练 / 移除

### 核心交互

- 点击“错题训练”进入 `/study?mode=WRONG_REVIEW`
- 点击“移除”调用移出接口
- 可筛选 `ACTIVE` 与 `RESOLVED`

### 需要接口

- `GET /api/wrong-words`
- `POST /api/wrong-words/{wordCardId}/remove`
- `GET /api/wrong-words/practice`

---

## 5.8 排行榜页 `/rank`

### 页面目标

提供成长反馈和一点轻量竞争感。

### 页面模块

- 榜单类型切换
- 我的名次卡片
- 排行榜列表

### 榜单类型

- 积分榜
- 连续打卡榜

### 列表字段

- 排名
- 用户昵称
- 分数 / 天数 / 时长

### 核心交互

- 切换不同榜单类型
- 高亮当前用户

### 需要接口

- `GET /api/rank/points`
- `GET /api/rank/streak`

---

## 5.9 学习统计页 `/statistics`

### 页面目标

展示“我学了多少”和“学得怎么样”。

### 页面模块

- 时间范围切换
- 统计摘要卡片
- 学习趋势折线图
- 正确率卡片
- 熟练度分布图
- 打卡热力区或日历区

### 摘要字段

- 总学习数
- 总正确数
- 正确率
- 总学习时长
- 当前连续天数

### 核心交互

- 切换 `WEEK` / `MONTH` / `ALL`
- 切换后刷新图表数据

### 需要接口

- `GET /api/study/statistics`
- `GET /api/checkin/calendar`

---

## 5.10 个人中心页 `/profile`

### 页面目标

管理用户基础资料和个人学习目标。

### 页面模块

- 头像与昵称信息
- 用户名展示
- 学习计划设置
- 退出登录按钮

### 可编辑字段

- 昵称
- 头像
- 每日学习目标
- 每日复习目标

### 需要接口

- `GET /api/user/me`
- `PUT /api/user/profile`
- `GET /api/study-plan`
- `PUT /api/study-plan`

---

## 6. 组件拆分建议

## 6.1 页面级组件

- `views/LoginView.vue`
- `views/RegisterView.vue`
- `views/DashboardView.vue`
- `views/WordCardListView.vue`
- `views/WordCardFormView.vue`
- `views/StudyView.vue`
- `views/WrongWordsView.vue`
- `views/RankView.vue`
- `views/StatisticsView.vue`
- `views/ProfileView.vue`

## 6.2 业务组件

- `components/dashboard/SummaryCard.vue`
- `components/study/StudyModeTabs.vue`
- `components/study/QuestionCard.vue`
- `components/study/StudyResultPanel.vue`
- `components/word-card/WordCardFilter.vue`
- `components/word-card/WordCardTable.vue`
- `components/rank/RankList.vue`
- `components/statistics/TrendChart.vue`
- `components/statistics/CheckinCalendar.vue`

第一版不用提前把组件拆得太细，优先在页面内先做通，重复明显后再抽离。

---

## 7. 路由守卫与登录态

### 7.1 公开页面

- `/login`
- `/register`

### 7.2 受保护页面

- 其他所有业务页

### 7.3 守卫规则

- 无 token 访问业务页时跳转 `/login`
- 已登录用户访问 `/login` 或 `/register` 时跳转 `/`
- 401 时清空 token 并跳回登录页

---

## 8. 空状态与错误状态

## 8.1 空状态

- 单词卡为空：提示先添加单词卡
- 错题本为空：提示“当前没有活跃错题”
- 排行榜为空：展示默认占位

## 8.2 错误状态

- 网络错误：使用全局消息提示
- 列表加载失败：提供重试按钮
- 学习题获取失败：展示“重新开始本轮训练”按钮

---

## 9. MVP 页面优先级

第一阶段建议按以下顺序开发：

1. 登录页
2. 注册页
3. 学习仪表盘
4. 单词卡列表页
5. 新增 / 编辑单词卡页
6. 学习训练页
7. 错题本页
8. 排行榜页
9. 学习统计页
10. 个人中心页

---

## 10. 页面与接口反推关系

## 10.1 第一批必须先落地的接口

- `/api/auth/register`
- `/api/auth/login`
- `/api/user/me`
- `/api/word-cards`
- `/api/word-cards/{id}`
- `/api/study/random`
- `/api/study/submit`
- `/api/wrong-words`

## 10.2 第二批再补的接口

- `/api/rank/points`
- `/api/rank/streak`
- `/api/study/statistics`
- `/api/checkin/calendar`
- `/api/study-plan`

---

## 11. 当前前端骨架对应关系

目前仓库中已经初始化了：

- `LoginView.vue`
- `RegisterView.vue`
- `DashboardView.vue`
- `AppLayout.vue`
- `router/index.js`
- `api/request.js`
- `api/auth.js`
- `stores/user.js`

下一步应优先补齐：

- `WordCardListView.vue`
- `WordCardFormView.vue`
- `StudyView.vue`
- `WrongWordsView.vue`
- `RankView.vue`
- `StatisticsView.vue`
- `ProfileView.vue`

---

## 12. 一句话结论

WordSprint 的前端应该围绕三件事组织：

- 今天学什么
- 当前这题怎么答
- 我最近学得怎么样

只要页面始终围绕这三件事展开，产品就不会跑偏。
