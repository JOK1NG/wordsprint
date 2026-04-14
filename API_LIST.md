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

说明：

- `nickname` 必填，去除首尾空白后长度需在 `2-20` 个字符之间
- `avatar` 可为空；若传入非空字符串，长度不能超过 `255`
- 仅允许更新当前登录用户自己的资料

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "demo_user",
    "nickname": "Demo New",
    "avatar": "https://example.com/avatar.png",
    "role": "USER"
  }
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

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dailyTargetCount": 20,
    "reviewTargetCount": 10,
    "reminderEnabled": false,
    "reminderTime": null
  }
}
```

说明：

- 若当前用户还没有学习计划记录，返回默认值：`dailyTargetCount=20`、`reviewTargetCount=10`、`reminderEnabled=false`、`reminderTime=null`

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

说明：

- `dailyTargetCount`、`reviewTargetCount` 为正整数，当前实现限制为 `1-500`
- `reminderEnabled=false` 时，`reminderTime` 可为空，服务端会清空已保存的提醒时间
- `reminderEnabled=true` 时，`reminderTime` 必填，格式为 `HH:mm:ss`
- 若当前用户还没有学习计划记录，更新时会自动创建

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dailyTargetCount": 20,
    "reviewTargetCount": 10,
    "reminderEnabled": false,
    "reminderTime": null
  }
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

## 4.6 CSV 导入单词卡

- 方法：`POST`
- 路径：`/api/word-cards/import/csv`
- 鉴权：是
- 请求类型：`multipart/form-data`

请求参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| file | file | 是 | CSV 文件 |

CSV 列顺序（前两列必填）：

`word,meaning,phonetic,exampleSentence,tags,isPublic`

说明：

- 支持首行表头（`word,meaning,...`）
- `tags` 建议用 `|` 分隔，如 `cet4|verb`
- `isPublic` 支持 `true/false/1/0`
- 文件大小上限 `20MB`
- 当前实现会跳过无效行并继续导入其余行

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalRows": 6,
    "successCount": 4,
    "failedCount": 2,
    "errors": [
      {
        "lineNumber": 4,
        "reason": "word 和 meaning 不能为空"
      }
    ]
  }
}
```

---

## 5. 公共词库模块

## 5.1 获取公共词库列表

- 方法：`GET`
- 路径：`/api/public-words`
- 鉴权：是

查询参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| pageNum | int | 页码，默认 1 |
| pageSize | int | 每页数量，默认 10 |
| keyword | string | 关键字，匹配单词/词义 |
| levelTag | string | 难度标签，如 `CET4` |

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "word": "abandon",
        "phonetic": "/əˈbændən/",
        "meaning": "放弃",
        "exampleSentence": "He had to abandon the plan.",
        "levelTag": "CET4",
        "sourceName": "official"
      }
    ],
    "total": 126,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

## 5.2 加入到我的词库

- 方法：`POST`
- 路径：`/api/public-words/{id}/import`
- 鉴权：是

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "wordCardId": 88,
    "imported": true,
    "word": "abandon"
  }
}
```

说明：

- 从公共词库复制一份到当前用户的 `word_card`
- 若用户已导入同单词同词义，返回 `imported=false`，避免重复导入

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

当前实现通常会联动返回以下反馈字段：

- `pointsEarned`
- `todayStudyCount`
- `todayCorrectCount`
- `currentStreakDays`

当前提交答题后已完成以下联动：

- 判题
- 写入 `study_record`
- 更新 `word_card`
- 更新 `wrong_word`
- 更新 `daily_checkin`
- 更新 `user_points`
- 刷新 Redis 排行榜和签到状态
- Redis 防重复提交保护

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
    "date": "2026-03-29",
    "studyCount": 20,
    "correctCount": 16,
    "accuracyRate": 80.0,
    "durationSeconds": 680,
    "pointsEarned": 16,
    "reviewCount": 9,
    "checkedIn": true,
    "streakDays": 5,
    "totalPoints": 128,
    "pendingReviewCount": 6,
    "totalStudied": 240,
    "totalCorrect": 188
  }
}
```

说明：

- 今日维度数据来自 `daily_checkin`
- `reviewCount` 表示当天 `study_mode=WRONG_REVIEW` 的提交次数，来自 `study_record`
- 累计积分、累计学习数、连续天数来自 `user_points`
- `pendingReviewCount` 表示当前 `ACTIVE` 状态错题数

## 6.4 获取学习统计

- 方法：`GET`
- 路径：`/api/study/statistics`
- 鉴权：是

查询参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| rangeType | string | 支持 `WEEK` / `MONTH` / `ALL`；分别表示最近 7 天、最近 30 天、全部历史 |

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "rangeType": "MONTH",
    "startDate": "2026-02-28",
    "endDate": "2026-03-29",
    "rangeStudyCount": 126,
    "rangeCorrectCount": 101,
    "rangeAccuracyRate": 80.16,
    "rangeDurationSeconds": 3860,
    "rangePointsEarned": 101,
    "totalStudied": 240,
    "totalCorrect": 188,
    "totalAccuracyRate": 78.33,
    "totalPoints": 128,
    "streakDays": 5,
    "maxStreakDays": 9,
    "pendingReviewCount": 6,
    "totalWrongCount": 11,
    "trend": [
      {
        "date": "2026-02-28",
        "studyCount": 6,
        "correctCount": 5,
        "accuracyRate": 83.33,
        "durationSeconds": 180,
        "pointsEarned": 5
      }
    ]
  }
}
```

说明：

- 区间趋势数据来自 `daily_checkin`
- `WEEK` 返回最近 7 天趋势，`MONTH` 返回最近 30 天趋势
- `ALL` 返回从首次学习日到今天的按日趋势；若没有历史数据，则返回当天一条全 `0` 数据
- `trend` 会补齐区间内缺失日期，未学习日期返回 `0`
- 累计数据来自 `user_points`

## 6.5 获取熟练度分布

- 方法：`GET`
- 路径：`/api/study/familiarity-distribution`
- 鉴权：是

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCards": 24,
    "items": [
      {
        "familiarityLevel": 0,
        "count": 5
      },
      {
        "familiarityLevel": 1,
        "count": 7
      }
    ]
  }
}
```

说明：

- 仅统计当前登录用户自己的单词卡
- 默认返回 `0-5` 全部熟练度档位，没有数据的档位也会返回 `count: 0`

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

## 7.3 恢复错题到活跃状态

- 方法：`POST`
- 路径：`/api/wrong-words/{wordCardId}/restore`
- 鉴权：是

说明：

- 用于把已移除或已解决的错题重新标记为 `ACTIVE`

## 7.4 获取错题专项训练题目

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

返回体（MVP 当前实现，最近 30 天）：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "startDate": "2026-02-28",
    "endDate": "2026-03-29",
    "currentStreakDays": 5,
    "days": [
      {
        "date": "2026-03-29",
        "checkedIn": true,
        "studyCount": 20,
        "correctCount": 16,
        "pointsEarned": 18
      }
    ]
  }
}
```

说明：

- 当前实现固定返回最近 `30` 天数据，用于统计页最小可用日历区
- `days` 会补齐区间内缺失日期，未学习日期返回 `checkedIn: false` 和各统计字段 `0`
- `currentStreakDays` 来自 `user_points`

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
        "avatar": "https://example.com/avatar-alice.png",
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

---

## 10. 管理端接口（可选）

## 10.1 公共词库新增

- 方法：`POST`
- 路径：`/api/admin/public-words`
- 鉴权：管理员

## 10.2 公共词库编辑

- 方法：`PUT`
- 路径：`/api/admin/public-words/{id}`
- 鉴权：管理员

## 10.3 公共词库禁用

- 方法：`POST`
- 路径：`/api/admin/public-words/{id}/disable`
- 鉴权：管理员

## 10.4 公共词库 CSV 批量导入

- 方法：`POST`
- 路径：`/api/admin/public-words/import/csv`
- 鉴权：管理员
- 请求类型：`multipart/form-data`

请求参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| file | file | 是 | CSV 文件 |

CSV 列顺序（前两列必填）：

`word,meaning,phonetic,exampleSentence,levelTag,sourceName,status`

说明：

- 支持首行表头（`word,meaning,...`）
- `status` 支持 `1/0/true/false`，默认 `1`
- 文件大小上限 `20MB`
- 以 `word + levelTag` 作为匹配键：存在则更新，不存在则新增

返回体：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalRows": 6,
    "insertedCount": 4,
    "updatedCount": 1,
    "failedCount": 1,
    "errors": [
      {
        "lineNumber": 5,
        "reason": "word 和 meaning 不能为空"
      }
    ]
  }
}
```

---

## 11. 参数校验建议

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

## 12. 接口开发顺序建议

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
