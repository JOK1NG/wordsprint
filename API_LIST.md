# WordSprint 接口设计清单

本文档定义 WordSprint 第一版的核心接口。接口风格以 REST 为主，统一返回结构，统一登录鉴权。

---

## 1. 接口约定

### 1.1 基础前缀

```text
/api
```

### 1.2 统一返回结构

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 1.3 分页返回结构

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

### 1.4 鉴权方式

登录后，在请求头中携带：

```text
Authorization: Bearer {token}
```

### 1.5 常见错误码建议

| code | 含义 |
| --- | --- |
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录或 token 无效 |
| 403 | 无权限 |
| 404 | 数据不存在 |
| 409 | 业务冲突，如重复创建 |
| 500 | 系统异常 |

---

## 2. 用户与鉴权模块

## 2.1 用户注册

- 方法：`POST`
- 路径：`/api/auth/register`
- 鉴权：否

请求体：

```json
{
  "username": "demo_user",
  "password": "123456",
  "nickname": "Demo"
}
```

返回体（MVP 当前实现）：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "demo_user"
  }
}
```

## 2.2 用户登录

- 方法：`POST`
- 路径：`/api/auth/login`
- 鉴权：否

请求体：

```json
{
  "username": "demo_user",
  "password": "123456"
}
```

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "jwt-token",
    "tokenType": "Bearer",
    "expireIn": 86400,
    "userInfo": {
      "id": 1,
      "username": "demo_user",
      "nickname": "Demo",
      "role": "USER"
    }
  }
}
```

## 2.3 获取当前用户信息

- 方法：`GET`
- 路径：`/api/user/me`
- 鉴权：是

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "demo_user",
    "nickname": "Demo",
    "avatar": null,
    "role": "USER"
  }
}
```

## 2.4 更新用户资料

- 方法：`PUT`
- 路径：`/api/user/profile`
- 鉴权：是

请求体：

```json
{
  "nickname": "Demo New",
  "avatar": "https://example.com/avatar.png"
}
```

## 2.5 退出登录

- 方法：`POST`
- 路径：`/api/auth/logout`
- 鉴权：是

说明：

- JWT 无状态方案可只由前端删除 token
- 若实现黑名单机制，可把 token 写入 Redis 黑名单

---

## 3. 学习计划模块

## 3.1 获取学习计划

- 方法：`GET`
- 路径：`/api/study-plan`
- 鉴权：是

## 3.2 更新学习计划

- 方法：`PUT`
- 路径：`/api/study-plan`
- 鉴权：是

请求体：

```json
{
  "dailyTargetCount": 20,
  "reviewTargetCount": 10,
  "reminderEnabled": false,
  "reminderTime": null
}
```

---

## 4. 单词卡模块

## 4.1 新增单词卡

- 方法：`POST`
- 路径：`/api/word-cards`
- 鉴权：是

请求体：

```json
{
  "word": "abandon",
  "phonetic": "/əˈbændən/",
  "meaning": "放弃",
  "exampleSentence": "He had to abandon the plan.",
  "tags": ["cet4", "verb"],
  "isPublic": false
}
```

## 4.2 更新单词卡

- 方法：`PUT`
- 路径：`/api/word-cards/{id}`
- 鉴权：是

## 4.3 删除单词卡

- 方法：`DELETE`
- 路径：`/api/word-cards/{id}`
- 鉴权：是

说明：

- 建议逻辑删除，不做物理删除

## 4.4 获取单词卡详情

- 方法：`GET`
- 路径：`/api/word-cards/{id}`
- 鉴权：是

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "word": "abandon",
    "phonetic": "/əˈbændən/",
    "meaning": "放弃",
    "exampleSentence": "He had to abandon the plan.",
    "tags": ["cet4", "verb"],
    "familiarityLevel": 2,
    "memoryStatus": "LEARNING",
    "wrongCount": 1,
    "correctCount": 3,
    "lastStudiedAt": "2026-03-29 10:30:00"
  }
}
```

## 4.5 分页查询单词卡

- 方法：`GET`
- 路径：`/api/word-cards`
- 鉴权：是

查询参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| pageNum | int | 页码 |
| pageSize | int | 每页数量 |
| keyword | string | 关键字，匹配单词/词义 |
| tag | string | 标签筛选 |
| memoryStatus | string | 记忆状态 |
| minFamiliarity | int | 最小熟练度 |
| maxFamiliarity | int | 最大熟练度 |
| wrongOnly | boolean | 是否只看错题 |

---

## 5. 公共词库模块

## 5.1 获取公共词库列表

- 方法：`GET`
- 路径：`/api/public-words`
- 鉴权：是

## 5.2 加入到我的词库

- 方法：`POST`
- 路径：`/api/public-words/{id}/import`
- 鉴权：是

说明：

- 从公共词库复制一份到当前用户的 `word_card`

---

## 6. 学习训练模块

## 6.1 获取训练题目

- 方法：`GET`
- 路径：`/api/study/random`
- 鉴权：是

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| mode | string | 是 | `WORD_TO_MEANING` / `MEANING_TO_WORD` / `CHOICE` / `WRONG_REVIEW` |
| size | int | 否 | 一次拉取题数，默认 10 |

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "sessionId": "study-session-001",
    "mode": "WORD_TO_MEANING",
    "questions": [
      {
        "questionId": "q1",
        "wordCardId": 1,
        "word": "abandon",
        "meaning": null,
        "options": []
      }
    ]
  }
}
```

说明：

- 若采用 Redis 随机池，可先缓存本轮训练题 ID 集合
- `MEANING_TO_WORD` 模式下可不返回 `word`

## 6.2 提交答题结果

- 方法：`POST`
- 路径：`/api/study/submit`
- 鉴权：是

请求体：

```json
{
  "wordCardId": 1,
  "studyMode": "MEANING_TO_WORD",
  "answerContent": "abandon",
  "durationSeconds": 8
}
```

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "isCorrect": true,
    "correctAnswer": "abandon",
    "familiarityLevel": 3,
    "memoryStatus": "LEARNING"
  }
}
```

增强版可追加字段（后续迭代）：

- `pointsEarned`
- `todayStudyCount`
- `todayCorrectCount`
- `currentStreakDays`

后端后续需要完成：

- 判题
- 写入 `study_record`
- 更新 `word_card`
- 更新 `wrong_word`
- 更新 `daily_checkin`
- 更新 `user_points`
- 刷新 Redis 排行榜和签到状态

## 6.3 获取今日学习摘要

- 方法：`GET`
- 路径：`/api/study/today-summary`
- 鉴权：是

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "studyCount": 20,
    "correctCount": 16,
    "accuracy": 0.8,
    "durationSeconds": 680,
    "dailyTargetCount": 20,
    "finished": true
  }
}
```

## 6.4 获取学习统计

- 方法：`GET`
- 路径：`/api/study/statistics`
- 鉴权：是

查询参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| rangeType | string | `WEEK` / `MONTH` / `ALL` |

返回体建议包含：

- 总学习数
- 总正确数
- 正确率
- 连续学习天数
- 每日趋势数组
- 熟练度分布数组

---

## 7. 错题本模块

## 7.1 获取错题列表

- 方法：`GET`
- 路径：`/api/wrong-words`
- 鉴权：是

查询参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| pageNum | int | 页码 |
| pageSize | int | 每页数量 |
| status | string | `ACTIVE` / `RESOLVED` |

## 7.2 手动移出错题本

- 方法：`POST`
- 路径：`/api/wrong-words/{wordCardId}/remove`
- 鉴权：是

说明：

- 本质上是把状态改为 `RESOLVED`

## 7.3 获取错题专项训练题目

- 方法：`GET`
- 路径：`/api/wrong-words/practice`
- 鉴权：是

查询参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| size | int | 默认 10 |

---

## 8. 打卡模块

## 8.1 获取打卡日历

- 方法：`GET`
- 路径：`/api/checkin/calendar`
- 鉴权：是

查询参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| year | int | 年 |
| month | int | 月 |

返回体建议：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "year": 2026,
    "month": 3,
    "currentStreakDays": 5,
    "days": [
      {
        "date": "2026-03-29",
        "checked": true,
        "studyCount": 20,
        "pointsEarned": 18
      }
    ]
  }
}
```

## 8.2 获取今日打卡状态

- 方法：`GET`
- 路径：`/api/checkin/today`
- 鉴权：是

---

## 9. 排行榜模块

## 9.1 积分榜

- 方法：`GET`
- 路径：`/api/rank/points`
- 鉴权：是

查询参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| limit | int | 默认 20 |

返回体建议：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "myRank": 3,
    "myScore": 128,
    "list": [
      {
        "rank": 1,
        "userId": 2,
        "nickname": "Alice",
        "score": 150
      }
    ]
  }
}
```

## 9.2 连续打卡榜

- 方法：`GET`
- 路径：`/api/rank/streak`
- 鉴权：是

## 9.3 周学习时长榜

- 方法：`GET`
- 路径：`/api/rank/weekly-duration`
- 鉴权：是

---

## 10. 仪表盘模块

## 10.1 获取首页摘要

- 方法：`GET`
- 路径：`/api/dashboard/summary`
- 鉴权：是

返回体建议包含：

- 今日学习数
- 今日正确率
- 当前连续天数
- 当前积分
- 今日目标完成度
- 待复习数量

## 10.2 获取热门词卡

- 方法：`GET`
- 路径：`/api/dashboard/hot-words`
- 鉴权：是

说明：

- 可从 Redis 缓存读取

---

## 11. 管理端接口（可选）

## 11.1 公共词库新增

- 方法：`POST`
- 路径：`/api/admin/public-words`
- 鉴权：管理员

## 11.2 公共词库编辑

- 方法：`PUT`
- 路径：`/api/admin/public-words/{id}`
- 鉴权：管理员

## 11.3 公共词库禁用

- 方法：`POST`
- 路径：`/api/admin/public-words/{id}/disable`
- 鉴权：管理员

---

## 12. 参数校验建议

### 注册

- `username`：4-20 位，仅字母数字下划线
- `password`：6-20 位
- `nickname`：2-20 位

### 单词卡

- `word`：不能为空，最长 100
- `meaning`：不能为空，最长 255
- `exampleSentence`：最长 500

### 学习提交

- `wordCardId`：必须存在且属于当前用户
- `studyMode`：必须在允许范围内
- `durationSeconds`：不小于 0

---

## 13. 接口开发顺序建议

建议按下面顺序实现：

1. `/api/auth/register`
2. `/api/auth/login`
3. `/api/user/me`
4. `/api/word-cards` 全套 CRUD
5. `/api/study/random`
6. `/api/study/submit`
7. `/api/wrong-words`
8. `/api/checkin/calendar`
9. `/api/rank/points`
10. `/api/study/statistics`
