# WordSprint 开发计划与开发规范

> 项目定位：一个轻量、有趣、适合 Java 学习的单词卡 / 记忆训练平台，采用 `Vue 3 + Spring Boot + Redis + MySQL` 的前后端分离架构。

---

## 1. 项目目标

### 1.1 项目名称

- 中文名：单词卡记忆训练平台
- 英文名建议：`WordSprint` / `MemoCard` / `LexiLoop`

### 1.2 核心目标

本项目不是做成一个庞大的教育平台，而是围绕“背单词”这一个小而完整的场景，练习以下能力：

1. Vue 前后端分离项目的整体开发流程
2. Spring Boot 后端接口、业务分层、鉴权设计
3. Redis 在缓存、排行榜、随机抽题、签到统计中的实际使用
4. MySQL 数据建模、索引设计、分页查询、状态流转
5. 从 0 到 1 做出一个真正可运行、可扩展、可维护的小项目

### 1.3 产品核心玩法

用户可以创建或导入单词卡，进行每日学习、随机抽题、拼写测试、错题回顾、记忆状态管理，并通过连续学习天数和积分排行榜获得正反馈。

---

## 2. 项目范围

### 2.1 本期必须实现

- 用户注册、登录、JWT 鉴权
- 单词卡管理：新增、编辑、删除、查看、标签分类
- 学习计划：每日学习数量设置
- 训练模式：随机抽题、看词忆义、看义拼词
- 错题本：自动记录错误题目并支持重练
- 学习记录：学习次数、正确率、最近学习时间
- 连续打卡：按天记录学习行为
- 排行榜：按积分或连续学习天数排行
- Redis：缓存热门数据、排行榜、签到状态、随机抽题池
- 基础后台接口文档与统一返回格式

### 2.2 第二阶段可选功能

- 单词集导入导出（CSV / Excel）
- 艾宾浩斯复习提醒
- 收藏单词、重点标记
- 公开词库 / 私有词库
- 每日挑战任务
- 邮箱验证码注册
- 管理端词库审核

### 2.3 明确不做

- 实时聊天
- 音视频课堂
- 复杂推荐算法
- 分布式微服务拆分
- 高并发秒杀类复杂场景

目标是先把单体项目做扎实，再考虑扩展。

---

## 3. 技术架构

## 3.1 总体架构

```text
Vue 3 + Vite + Pinia + Axios
            |
          HTTP
            |
Spring Boot -> Service -> Mapper -> MySQL
            |
          Redis
```

### 3.2 推荐技术栈

#### 前端

- `Vue 3`
- `Vite`
- `Vue Router`
- `Pinia`
- `Axios`
- `Element Plus` 或 `Naive UI`
- `ECharts`（用于学习统计图表，可选）

#### 后端

- `Java 17`
- `Spring Boot 3.x`
- `Spring Web`
- `Spring Validation`
- `Spring Security + JWT` 或 `Sa-Token`
- `MyBatis-Plus`
- `Lombok`
- `MySQL 8`
- `Redis`
- `Knife4j / SpringDoc OpenAPI`

#### 开发环境

- `IDEA`
- `Node.js 20+`
- `Maven 3.9+`
- `Docker`（可选，用于本地启动 MySQL / Redis）

### 3.3 架构原则

- 先单体、后扩展
- 先可用、后优化
- 优先清晰分层，不追求炫技
- Redis 只做“加速”和“状态型辅助”，不替代核心持久化数据

---

## 4. 用户角色与权限

### 4.1 用户角色

- `USER`：普通用户，学习、管理自己的单词卡、查看排行榜
- `ADMIN`：管理员，管理公共词库、查看全局统计、处理异常数据

### 4.2 权限边界

- 用户只能操作自己的私有词卡、学习记录、错题记录
- 公共词库默认只读，管理员可维护
- 排行榜与公开统计可对登录用户开放

---

## 5. 功能模块拆解

## 5.1 用户模块

### 功能点

- 注册
- 登录
- 获取当前用户信息
- 修改昵称、头像、学习目标
- 退出登录

### 学习价值

- 表单校验
- 密码加密
- JWT 鉴权
- 接口权限控制

## 5.2 单词卡模块

### 功能点

- 新建单词卡
- 编辑单词卡
- 删除单词卡
- 单词卡列表分页
- 按标签、熟练度、是否错题筛选
- 查看单词详情

### 建议字段

- 单词
- 音标
- 词义
- 例句
- 标签
- 记忆状态
- 熟练度
- 是否公开

## 5.3 词库模块

### 功能点

- 我的词库
- 公共词库
- 从公共词库加入个人学习清单
- 批量导入词卡

### 说明

第一版可将“词库”和“单词卡”简化为一张表加一个分组字段，不必过早复杂化。

## 5.4 学习训练模块

### 训练模式

- 模式 A：看单词回忆中文
- 模式 B：看中文拼写英文
- 模式 C：随机选择题
- 模式 D：错题重练

### 流程建议

1. 用户进入训练页
2. 系统根据当前模式抽取题目
3. 用户提交答案
4. 后端判定正确与否
5. 更新学习记录、熟练度、错题状态、积分
6. 返回本题反馈与下一题

## 5.5 错题本模块

### 功能点

- 自动加入错题本
- 错题列表
- 错题移除 / 重新掌握
- 错题专项训练

### 关键规则

- 同一用户 + 同一单词只保留一条有效错题记录
- 连续答对若干次后可自动移出错题本

## 5.6 学习打卡与积分模块

### 功能点

- 当天完成学习即自动打卡
- 连续学习天数统计
- 每日积分增长
- 历史打卡日历

### 积分建议规则

- 完成每日目标：+10
- 正确答题：+1
- 连续打卡加成：+N
- 错题纠正成功：+2

## 5.7 排行榜模块

### 排行榜类型

- 总积分榜
- 本周学习时长榜
- 连续打卡榜

### 实现建议

- 使用 Redis `ZSet`
- 定时或事件驱动同步 MySQL 汇总数据

## 5.8 数据统计模块

### 统计内容

- 今日学习数量
- 总学习次数
- 正确率
- 本周学习趋势
- 熟练度分布

前端可用折线图、柱状图、日历热力图展示。

---

## 6. 页面规划

### 6.1 前台页面

- 登录页
- 注册页
- 首页 / 仪表盘
- 单词卡列表页
- 新增 / 编辑单词卡页
- 学习训练页
- 错题本页
- 排行榜页
- 个人中心页
- 学习统计页

### 6.2 后台页面（可选）

- 公共词库管理
- 用户管理
- 数据总览

---

## 7. 后端模块规划

建议采用以下包结构：

```text
com.example.wordsprint
├─ common        // 通用返回、异常、工具类
├─ config        // 配置类
├─ controller    // 接口层
├─ dto           // 请求对象
├─ vo            // 响应对象
├─ entity        // 实体类
├─ mapper        // MyBatis-Plus Mapper
├─ service       // 业务接口
├─ service.impl  // 业务实现
├─ security      // 鉴权相关
├─ redis         // Redis key 与缓存逻辑
└─ job           // 定时任务
```

### 分层职责

- `controller`：接收请求、参数校验、调用 service
- `service`：核心业务逻辑，不直接处理 HTTP 细节
- `mapper`：数据库访问
- `dto/vo`：避免直接暴露 entity
- `common`：统一异常码、返回结构、全局处理

---

## 8. 数据库设计

## 8.1 核心表建议

### `user`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| username | varchar(50) | 用户名 |
| password | varchar(100) | 加密密码 |
| nickname | varchar(50) | 昵称 |
| avatar | varchar(255) | 头像 |
| role | varchar(20) | 角色 |
| status | tinyint | 状态 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

### `word_card`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 所属用户 |
| word | varchar(100) | 单词 |
| phonetic | varchar(100) | 音标 |
| meaning | varchar(255) | 词义 |
| example_sentence | varchar(500) | 例句 |
| tags | varchar(255) | 标签，逗号分隔或单独拆表 |
| source_type | varchar(20) | 来源：private/public/import |
| familiarity_level | int | 熟练度 0-5 |
| memory_status | varchar(20) | new/learning/mastered |
| is_deleted | tinyint | 逻辑删除 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

### `study_record`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| word_card_id | bigint | 单词卡 ID |
| study_mode | varchar(20) | 学习模式 |
| answer_content | varchar(255) | 用户答案 |
| is_correct | tinyint | 是否正确 |
| studied_at | datetime | 学习时间 |

### `wrong_word`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| word_card_id | bigint | 单词卡 ID |
| wrong_count | int | 错误次数 |
| last_wrong_at | datetime | 最近错误时间 |
| status | varchar(20) | active/resolved |

### `daily_checkin`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| checkin_date | date | 打卡日期 |
| study_count | int | 当日学习题数 |
| points_earned | int | 当日获得积分 |

### `user_points`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| total_points | int | 总积分 |
| streak_days | int | 连续学习天数 |
| total_studied | int | 总学习题数 |
| total_correct | int | 总正确题数 |
| updated_at | datetime | 更新时间 |

## 8.2 索引建议

- `user.username` 唯一索引
- `word_card(user_id, word)` 联合索引
- `study_record(user_id, studied_at)` 索引
- `wrong_word(user_id, status)` 联合索引
- `daily_checkin(user_id, checkin_date)` 唯一索引

## 8.3 数据库规范

- 表名使用小写下划线
- 必备字段：`id`、`created_at`、`updated_at`
- 删除优先逻辑删除
- 状态字段统一使用可枚举值，不使用魔法数字裸写在业务代码中

---

## 9. Redis 设计

## 9.1 使用目标

Redis 必须服务于以下真实场景：

1. 缓存热点数据
2. 提升随机抽题效率
3. 记录签到和连续学习状态
4. 实现排行榜
5. 限制重复提交或短时间刷接口

## 9.2 Key 设计规范

统一格式：

```text
项目名:模块:业务:标识
```

示例：

```text
wordsprint:user:checkin:1001:20260329
wordsprint:rank:points
wordsprint:study:random_pool:1001
wordsprint:word:hot
```

## 9.3 推荐数据结构

### 1）排行榜

- Key：`wordsprint:rank:points`
- 类型：`ZSet`
- value：`userId`
- score：积分

### 2）连续打卡状态

- Key：`wordsprint:user:checkin:{userId}:{yyyyMMdd}`
- 类型：`String`
- 值：`1`
- TTL：2 天

### 3）随机抽题池

- Key：`wordsprint:study:random_pool:{userId}`
- 类型：`List` 或 `Set`
- 值：单词卡 ID
- TTL：30 分钟

### 4）热门词卡缓存

- Key：`wordsprint:word:hot`
- 类型：`String` / JSON
- TTL：10 分钟

### 5）接口幂等 / 防重复提交

- Key：`wordsprint:submit:lock:{userId}:{bizId}`
- 类型：`String`
- TTL：3-5 秒

## 9.4 Redis 使用规范

- Key 必须有统一前缀
- 非永久数据必须设置 TTL
- 不把核心业务唯一数据只放 Redis
- 排行榜可 Redis 为主、MySQL 为兜底
- 避免缓存穿透：不存在的数据可缓存空值短 TTL
- 避免缓存雪崩：TTL 增加随机偏移

---

## 10. API 设计规划

## 10.1 统一接口规范

统一返回结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

分页返回建议：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 0,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

## 10.2 核心接口清单

### 用户模块

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/user/me`
- `PUT /api/user/profile`

### 单词卡模块

- `POST /api/word-cards`
- `PUT /api/word-cards/{id}`
- `DELETE /api/word-cards/{id}`
- `GET /api/word-cards/{id}`
- `GET /api/word-cards`

### 学习模块

- `GET /api/study/random`
- `POST /api/study/submit`
- `GET /api/study/today-summary`
- `GET /api/study/statistics`

### 错题模块

- `GET /api/wrong-words`
- `POST /api/wrong-words/{wordId}/remove`
- `GET /api/wrong-words/practice`

### 打卡与排行榜

- `GET /api/checkin/calendar`
- `GET /api/rank/points`
- `GET /api/rank/streak`

## 10.3 API 规范要求

- GET 查数据，POST 新增，PUT 更新，DELETE 删除
- URL 使用复数资源名
- 参数校验放在 DTO 层
- 业务异常统一抛出，不在 controller 中手写大量 if-else
- 文档由 OpenAPI 自动生成，不维护手写接口文档作为主文档

---

## 11. 前端开发计划

## 11.1 推荐目录结构

```text
src
├─ api
├─ assets
├─ components
├─ composables
├─ layout
├─ router
├─ stores
├─ styles
├─ utils
└─ views
```

## 11.2 前端模块拆分

- `api`：接口请求封装
- `stores`：用户信息、学习状态、主题状态
- `views`：页面级组件
- `components`：卡片、统计面板、题目组件、打卡日历
- `utils`：token、时间处理、缓存封装

## 11.3 前端开发顺序

### 第一阶段

- 搭建基础项目
- 配置路由、状态管理、请求拦截器
- 实现登录注册页

### 第二阶段

- 实现单词卡 CRUD 页面
- 实现列表筛选与分页

### 第三阶段

- 实现学习训练页
- 实现提交答案与结果反馈

### 第四阶段

- 实现错题本、统计页、排行榜页
- 做页面对接测试和状态修正

## 11.4 前端规范

- 组件名使用 `PascalCase`
- 页面文件放 `views`
- 通用组件优先抽离，避免复制粘贴
- 请求统一由 `api` 层发起，不在页面里手写裸 Axios
- 不在组件里写死接口地址
- 表单必须有前端校验，但最终以后端校验为准
- 样式优先局部化，公共变量放在统一样式文件

---

## 12. 后端开发计划

## 12.1 开发顺序

### 第一阶段：基础设施

- 初始化 Spring Boot 项目
- 配置 MySQL、Redis、日志、统一返回结构
- 完成异常处理、参数校验、跨域处理

### 第二阶段：鉴权与用户

- 注册登录
- JWT 生成和校验
- 获取用户信息

### 第三阶段：单词卡核心 CRUD

- 新增、编辑、删除、查询
- 分页与筛选
- 数据权限控制

### 第四阶段：学习训练流程

- 随机抽题
- 提交答案
- 更新学习记录
- 更新熟练度、积分、错题本

### 第五阶段：打卡、排行榜、统计

- 打卡逻辑
- Redis 排行榜
- 数据统计接口

### 第六阶段：优化与补充

- 热点缓存
- 防重复提交
- 导入导出
- OpenAPI 文档整理

## 12.2 后端规范

- Controller 只做接口协调，不写复杂业务
- Service 方法名使用动词开头，如 `createWordCard`、`submitAnswer`
- Mapper 只做数据访问，不放业务判断
- DTO 负责接收入参，VO 负责返回视图数据
- Entity 不直接返回给前端
- 所有时间字段统一使用同一种时区策略

---

## 13. 业务规则建议

### 13.1 记忆状态建议

- `NEW`：未学过
- `LEARNING`：学习中
- `REVIEWING`：需要复习
- `MASTERED`：已掌握

### 13.2 熟练度规则建议

- 初始值为 `0`
- 每次答对 `+1`
- 每次答错 `-1`，最低不低于 `0`
- 达到阈值后进入 `MASTERED`

### 13.3 错题规则建议

- 首次答错加入错题本
- 连续答对 3 次可标记为已解决
- 已解决错题保留历史，但默认不展示在活跃错题列表

### 13.4 打卡规则建议

- 当天学习题数 >= 1 即记为打卡
- 若中断一天，则连续天数重新计算
- 连续天数每天首次学习时刷新

---

## 14. 开发里程碑

## 14.1 推荐 4 周版本计划

### 第 1 周：项目骨架

- 初始化前后端项目
- 建表
- 完成登录注册
- 完成基础页面框架

### 第 2 周：核心 CRUD

- 完成单词卡管理
- 完成词卡分页、筛选、详情
- 打通前后端对接

### 第 3 周：学习完整流程

- 完成随机抽题
- 完成答题提交
- 完成学习记录与错题本
- 完成打卡逻辑

### 第 4 周：优化与展示

- 完成排行榜
- 完成统计页面
- 加入 Redis 缓存优化
- 修复细节问题并整理文档

## 14.2 最小可运行版本（MVP）

如果希望尽快出成果，先完成以下最小可运行流程：

1. 注册登录
2. 单词卡 CRUD
3. 随机抽题
4. 答题判定
5. 错题本
6. 打卡与积分榜

---

## 15. 开发规范

## 15.1 命名规范

### Java

- 类名：`PascalCase`
- 方法名：`camelCase`
- 常量：`UPPER_SNAKE_CASE`
- 包名：全小写

### 数据库

- 表名、字段名：`snake_case`
- 主键统一 `id`
- 时间字段统一 `created_at`、`updated_at`

### 前端

- 组件名：`PascalCase`
- 变量、函数：`camelCase`
- 文件夹名：推荐小写短横线或按现有风格统一

## 15.2 代码风格规范

- 单个方法尽量只做一件事
- 避免超长方法，建议控制在 50 行左右，超过时拆分私有方法
- 避免硬编码状态值，使用枚举或常量类
- 空值判断统一策略，避免同项目混用多种风格
- 日志记录关键流程，但不打印密码、token、敏感信息

## 15.3 异常处理规范

- 业务错误使用自定义异常
- 参数错误由校验框架统一处理
- 未知错误统一兜底为系统异常
- 对前端返回统一错误码和消息

## 15.4 安全规范

- 密码必须加密存储
- JWT 需要过期时间
- 重要接口必须鉴权
- 用户数据查询必须校验归属权
- 防止越权修改他人数据
- 上传功能如后续接入，必须限制文件类型和大小

## 15.5 Redis 使用规范

- 明确缓存来源与失效策略
- 更新数据库后及时删除或刷新缓存
- 不允许无 TTL 的临时业务 key 长期堆积
- 排行榜与签到等状态 key 建议按模块集中管理

## 15.6 Git 规范

即使是个人项目，也建议遵守：

- `main`：稳定分支
- `dev`：开发分支
- 功能开发使用 `feature/xxx`

提交信息建议：

```text
feat: add study submit api
fix: correct streak day calculation
refactor: simplify word card query logic
docs: add redis design notes
```

## 15.7 文档规范

- README 说明项目介绍、启动方式、技术栈、目录结构
- 接口文档通过 OpenAPI 自动生成
- 核心业务规则写入独立文档，避免只存在于代码注释里

---

## 16. 测试规范

## 16.1 后端测试重点

- 用户注册登录
- JWT 鉴权有效性
- 单词卡 CRUD 权限校验
- 答题提交正确性
- 错题本状态变化
- 连续打卡计算
- Redis 排行榜同步逻辑

## 16.2 前端测试重点

- 登录态跳转
- 页面表单校验
- 学习流程交互正确性
- 错题本和排行榜显示正确
- 异常状态提示友好

## 16.3 手工测试清单

- 新用户能否完整注册登录
- 单词卡能否新增、编辑、删除、查询
- 学习答题后是否写入学习记录
- 答错后是否进入错题本
- 打卡是否只在当天首次学习后建立
- 排行榜是否随积分变化而变化

---

## 17. 性能与优化建议

- 热门统计和排行榜优先走 Redis
- 列表查询必须分页
- 高频查询字段建立索引
- 学习提交接口增加防重复机制
- 大批量导入词卡时使用批处理
- 图表统计接口避免返回过大原始数据集

---

## 18. 上线前检查清单

- 配置文件中无明文敏感信息提交到仓库
- 默认账号和测试数据已处理
- MySQL 和 Redis 连接配置正确
- 跨域配置可用
- 生产环境关闭调试日志
- README 补齐启动说明
- 核心接口已完成自测

---

## 19. 推荐开发顺序总结

最推荐按下面顺序做：

1. 先搭后端基础设施和登录鉴权
2. 再做单词卡 CRUD
3. 再打通学习训练主流程
4. 最后接入错题本、打卡、排行榜、统计
5. 完成后再做导入导出和公共词库等增强功能

这样能保证你很快做出一个可运行的完整流程，同时又能逐步把 Redis、统计、缓存这些点加进去。

---

## 20. 最终完成标准

当项目满足以下条件时，可以认为第一版完成：

- 用户可以正常注册登录
- 用户可以管理自己的单词卡
- 用户可以开始学习并提交答案
- 系统可以记录错题、积分、打卡、统计
- Redis 已实际用于排行榜、缓存、随机抽题池或签到状态
- 前后端可以本地完整跑通
- README 和接口文档可供他人快速启动

---

如果后续继续开发，下一份文档建议直接写：

1. `README.md`：项目启动说明
2. `DATABASE_DESIGN.md`：建表 SQL 与索引说明
3. `API_LIST.md`：接口设计清单
4. `ITERATION_TODO.md`：分阶段开发任务表
