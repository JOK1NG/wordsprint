# 学习训练改动审阅

审阅范围：`backend/src/main/java/com/example/wordsprint/service/impl/StudyServiceImpl.java`、`frontend/src/views/study/StudyTrainingView.vue`、`API_LIST.md`、`DATABASE_DESIGN.md`。

## 复核结果

### 1) submit 幂等与并发冲突

- 结论：**存在，已修复**。
- 修复内容：
  - 在 `backend/src/main/java/com/example/wordsprint/service/impl/StudyServiceImpl.java:218` 增加 Redis 短 TTL 防重复提交锁。
  - 锁 key 使用 `userId + wordCardId + mode + answerFingerprint`，防止同 payload 双击重复提交，同时允许用户快速提交不同答案。
  - `wrong_word`、`daily_checkin`、`user_points` 的首次创建改为“插入 + 重复键兜底重查”，降低并发首次写入冲突风险。
- 验证：
  - 同题同答案连续提交：第二次返回 `409`。
  - 同题不同答案快速提交：允许通过。

### 2) CHOICE 模式无选项

- 结论：**存在，已修复**。
- 修复内容：
  - 在 `backend/src/main/java/com/example/wordsprint/service/impl/StudyServiceImpl.java:191` 增加 `buildChoiceOptions`。
  - `CHOICE` 模式返回 4 个选项（含正确项 + 干扰项）。
  - 前端 `frontend/src/views/study/StudyTrainingView.vue` 已具备选项渲染逻辑，无需额外改动。
- 验证：
  - `GET /api/study/random?mode=CHOICE` 返回 `options` 数组，数量为 4。

### 3) WRONG_REVIEW 未按 wrong_word 抽题

- 结论：**存在，已修复**。
- 修复内容：
  - 在 `backend/src/main/java/com/example/wordsprint/service/impl/StudyServiceImpl.java:138` 新增 `fetchWrongReviewCards`。
  - `WRONG_REVIEW` 现在按 `wrong_word.status = ACTIVE` 选题，不再依赖 `word_card.wrong_count > 0`。
- 验证：
  - 单词答错后再连续答对 3 次会被标记为 `RESOLVED`，随后 `WRONG_REVIEW` 不再返回该题。

### 4) ORDER BY RAND() 性能风险

- 结论：**存在，已修复为更轻量方案**。
- 修复内容：
  - 普通模式改为先取有限题池（默认 200）再在内存随机抽样，避免数据库 `ORDER BY RAND()`。
  - 位置：`backend/src/main/java/com/example/wordsprint/service/impl/StudyServiceImpl.java:119`。

## 当前状态

- 学习训练主流程可用，且上述审阅问题已全部修复。
- 已通过：
  - `cd backend && mvn test`
  - 实际接口冒烟（注册/登录/建卡/CHOICE/WRONG_REVIEW/submit 幂等）
