-- WordSprint Seed Data
-- 在 init.sql 之后运行此文件，为 demo 用户和公共词库填充完整的示例数据
-- 请在全新数据库上运行，不要在已有业务数据上重复执行
--
-- 用法：
--   mysql -h 127.0.0.1 -u root -p123456 wordsprint < sql/seed_data.sql
--
-- 账号：
--   demo / 123456 （普通用户，有学习数据）
--   admin / 123456 （管理员，可导入公共词库）

USE wordsprint;

-- ========================================
-- 1. 更新 demo 用户已有的 3 张词卡，设置不同学习状态
-- ========================================

UPDATE word_card SET
    familiarity_level = 3,
    memory_status = 'LEARNING',
    correct_count = 8,
    wrong_count = 2,
    last_studied_at = NOW()
WHERE user_id = 1 AND word = 'abandon';

UPDATE word_card SET
    familiarity_level = 5,
    memory_status = 'MASTERED',
    correct_count = 12,
    wrong_count = 1,
    last_studied_at = NOW()
WHERE user_id = 1 AND word = 'benefit';

UPDATE word_card SET
    familiarity_level = 1,
    memory_status = 'LEARNING',
    correct_count = 3,
    wrong_count = 4,
    last_studied_at = NOW()
WHERE user_id = 1 AND word = 'capture';

-- ========================================
-- 2. 为 demo 用户添加更多词卡（共 25 张）
-- ========================================

INSERT INTO word_card (user_id, word, phonetic, meaning, example_sentence, tags, source_type, familiarity_level, memory_status, correct_count, wrong_count, last_studied_at) VALUES
-- LEARNING 级别
(1, 'diverse', '/daɪˈvɜːrs/', '多样的; 不同的', 'The city has a diverse population.', 'cet4,adj', 'PRIVATE', 2, 'LEARNING', 4, 2, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 'establish', '/ɪˈstæblɪʃ/', '建立; 确立', 'They established a new company last year.', 'cet4,verb', 'PRIVATE', 2, 'LEARNING', 5, 3, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 'fundamental', '/ˌfʌndəˈmentl/', '基本的; 根本的', 'Education is a fundamental right.', 'cet4,adj', 'IMPORT', 1, 'LEARNING', 3, 3, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 'implement', '/ˈɪmplɪment/', '实施; 执行', 'We need to implement the new policy.', 'cet4,verb', 'IMPORT', 2, 'LEARNING', 5, 2, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 'obvious', '/ˈɒbviəs/', '明显的', 'The answer is obvious to everyone.', 'cet4,adj', 'PRIVATE', 2, 'LEARNING', 6, 3, DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- REVIEWING 级别
(1, 'perceive', '/pərˈsiːv/', '感知; 察觉', 'She perceived a change in his attitude.', 'cet6,verb', 'IMPORT', 3, 'REVIEWING', 7, 4, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 'reluctant', '/rɪˈlʌktənt/', '不情愿的', 'He was reluctant to admit his mistake.', 'cet4,adj', 'PRIVATE', 3, 'REVIEWING', 8, 5, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 'substantial', '/səbˈstænʃl/', '大量的; 重大的', 'They made a substantial investment.', 'cet6,adj', 'IMPORT', 3, 'REVIEWING', 9, 4, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 'deteriorate', '/dɪˈtɪriəreɪt/', '恶化; 退化', 'His health began to deteriorate rapidly.', 'cet6,verb', 'IMPORT', 3, 'REVIEWING', 7, 5, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 'elaborate', '/ɪˈlæbərət/', '精心制作的; 详尽的', 'She made elaborate preparations for the party.', 'cet6,adj', 'PRIVATE', 3, 'REVIEWING', 6, 4, DATE_SUB(NOW(), INTERVAL 4 DAY)),

-- MASTERED 级别
(1, 'advantage', '/ədˈvɑːntɪdʒ/', '优势; 有利条件', 'Learning English is a great advantage.', 'cet4,noun', 'PRIVATE', 5, 'MASTERED', 15, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 'environment', '/ɪnˈvaɪrənmənt/', '环境', 'We should protect the environment.', 'cet4,noun', 'IMPORT', 4, 'MASTERED', 12, 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 'opportunity', '/ˌɒpərˈtjuːnəti/', '机会', 'This is a great opportunity for growth.', 'cet4,noun', 'PRIVATE', 4, 'MASTERED', 11, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 'significant', '/sɪɡˈnɪfɪkənt/', '重要的; 显著的', 'There was a significant improvement.', 'cet4,adj', 'IMPORT', 5, 'MASTERED', 14, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 'consequence', '/ˈkɒnsɪkwəns/', '结果; 后果', 'Every action has consequences.', 'cet6,noun', 'IMPORT', 4, 'MASTERED', 10, 2, DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- NEW 级别（还没开始学）
(1, 'ambiguous', '/æmˈbɪɡjuəs/', '模棱两可的', 'His answer was deliberately ambiguous.', 'cet6,adj', 'IMPORT', 0, 'NEW', 0, 0, NULL),
(1, 'comprehensive', '/ˌkɒmprɪˈhensɪv/', '综合的; 全面的', 'She gave a comprehensive review.', 'cet4,adj', 'IMPORT', 0, 'NEW', 0, 0, NULL),
(1, 'phenomenon', '/fɪˈnɒmɪnən/', '现象', 'This is a natural phenomenon.', 'cet4,noun', 'PRIVATE', 0, 'NEW', 0, 0, NULL),
(1, 'strategy', '/ˈstrætədʒi/', '策略; 战略', 'We need a new marketing strategy.', 'cet4,noun', 'IMPORT', 0, 'NEW', 0, 0, NULL),
(1, 'vulnerable', '/ˈvʌlnərəbl/', '脆弱的; 易受伤的', 'Children are more vulnerable to illness.', 'ielts,adj', 'IMPORT', 0, 'NEW', 0, 0, NULL),

-- IELTS 级别
(1, 'fluctuate', '/ˈflʌktʃueɪt/', '波动; 起伏', 'Prices fluctuate according to demand.', 'ielts,verb', 'IMPORT', 1, 'LEARNING', 3, 4, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 'assess', '/əˈses/', '评估; 评定', 'We need to assess the impact of the policy.', 'ielts,verb', 'IMPORT', 2, 'LEARNING', 4, 3, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ========================================
-- 3. 公共词库（50 条：CET4 20条 + CET6 15条 + IELTS 15条）
-- ========================================

INSERT INTO public_word_library (word, phonetic, meaning, example_sentence, level_tag, source_name, status) VALUES
-- CET4
('achieve', '/əˈtʃiːv/', '达到; 取得', 'She achieved her goal through hard work.', 'CET4', 'official', 1),
('analyze', '/ˈænəlaɪz/', '分析', 'We need to analyze the data carefully.', 'CET4', 'official', 1),
('approach', '/əˈprəʊtʃ/', '接近; 方法', 'His approach to the problem was creative.', 'CET4', 'official', 1),
('appropriate', '/əˈprəʊpriət/', '适当的', 'Please wear appropriate clothing.', 'CET4', 'official', 1),
('barrier', '/ˈbæriər/', '障碍; 屏障', 'Language can be a barrier to communication.', 'CET4', 'official', 1),
('concept', '/ˈkɒnsept/', '概念; 观念', 'The concept is difficult to understand.', 'CET4', 'official', 1),
('conscious', '/ˈkɒnʃəs/', '有意识的', 'She was conscious of being watched.', 'CET4', 'official', 1),
('contrast', '/ˈkɒntrɑːst/', '对比; 差异', 'There is a contrast between the two plans.', 'CET4', 'official', 1),
('demonstrate', '/ˈdemənstreɪt/', '证明; 示范', 'He demonstrated how to use the equipment.', 'CET4', 'official', 1),
('emerge', '/ɪˈmɜːdʒ/', '出现; 浮现', 'New details emerged from the investigation.', 'CET4', 'official', 1),
('evolve', '/ɪˈvɒlv/', '进化; 发展', 'Technology continues to evolve rapidly.', 'CET4', 'official', 1),
('generate', '/ˈdʒenəreɪt/', '产生; 发生', 'The project will generate new jobs.', 'CET4', 'official', 1),
('inevitable', '/ɪnˈevɪtəbl/', '不可避免的', 'Change is inevitable in any organization.', 'CET4', 'official', 1),
('maintain', '/meɪnˈteɪn/', '维持; 保持', 'It is important to maintain a healthy diet.', 'CET4', 'official', 1),
('potential', '/pəˈtenʃl/', '潜在的; 潜力', 'Every student has potential for growth.', 'CET4', 'official', 1),
('recognize', '/ˈrekəɡnaɪz/', '认出; 承认', 'I did not recognize her at first.', 'CET4', 'official', 1),
('sufficient', '/səˈfɪʃnt/', '足够的', 'We have sufficient evidence to proceed.', 'CET4', 'official', 1),
('tendency', '/ˈtendənsi/', '趋势; 倾向', 'There is a tendency to spend more online.', 'CET4', 'official', 1),
('transfer', '/trænsˈfɜːr/', '转移; 调动', 'She transferred to a different department.', 'CET4', 'official', 1),
('voluntary', '/ˈvɒləntri/', '自愿的; 志愿的', 'Participation is completely voluntary.', 'CET4', 'official', 1),

-- CET6
('accumulate', '/əˈkjuːmjəleɪt/', '积累; 积聚', 'Wealth does not accumulate overnight.', 'CET6', 'official', 1),
('compensate', '/ˈkɒmpenseɪt/', '补偿; 赔偿', 'The company will compensate for the loss.', 'CET6', 'official', 1),
('contemplate', '/ˈkɒntəmpleɪt/', '沉思; 考虑', 'She contemplated the meaning of the poem.', 'CET6', 'official', 1),
('deficiency', '/dɪˈfɪʃnsi/', '缺乏; 不足', 'Vitamin deficiency can cause health problems.', 'CET6', 'official', 1),
('discriminate', '/dɪˈskrɪmɪneɪt/', '歧视; 区分', 'It is wrong to discriminate based on age.', 'CET6', 'official', 1),
('exaggerate', '/ɪɡˈzædʒəreɪt/', '夸大; 夸张', 'Do not exaggerate the difficulty of the task.', 'CET6', 'official', 1),
('illuminate', '/ɪˈluːmɪneɪt/', '照亮; 说明', 'The research illuminated the issue.', 'CET6', 'official', 1),
('indispensable', '/ˌɪndɪˈspensəbl/', '不可缺少的', 'Water is indispensable to life.', 'CET6', 'official', 1),
('legitimate', '/lɪˈdʒɪtɪmət/', '合法的; 正当的', 'They have a legitimate concern.', 'CET6', 'official', 1),
('prevalent', '/ˈprevələnt/', '流行的; 普遍的', 'This belief is prevalent in the region.', 'CET6', 'official', 1),
('simultaneously', '/ˌsɪmlˈteɪniəsli/', '同时地', 'Both events occurred simultaneously.', 'CET6', 'official', 1),
('speculate', '/ˈspekjuleɪt/', '推测; 投机', 'Analysts speculate about the market trend.', 'CET6', 'official', 1),
('trigger', '/ˈtrɪɡər/', '触发; 引起', 'The incident triggered a health debate.', 'CET6', 'official', 1),
('undermine', '/ˌʌndəˈmaɪn/', '逐渐削弱', 'Rumors can undermine public trust.', 'CET6', 'official', 1),
('validate', '/ˈvælɪdeɪt/', '验证; 确认', 'We need to validate the test results.', 'CET6', 'official', 1),

-- IELTS
('accommodate', '/əˈkɒmədeɪt/', '容纳; 适应', 'The hall can accommodate 500 people.', 'IELTS', 'official', 1),
('bilingual', '/baɪˈlɪŋɡwəl/', '双语的', 'She is bilingual in English and French.', 'IELTS', 'official', 1),
('curriculum', '/kəˈrɪkjələm/', '课程', 'The school updated its curriculum.', 'IELTS', 'official', 1),
('deterioration', '/dɪˌtɪriəˈreɪʃn/', '恶化; 退化', 'The deterioration of air quality is alarming.', 'IELTS', 'official', 1),
('exploit', '/ɪkˈsplɔɪt/', '开发; 利用', 'We should exploit renewable energy sources.', 'IELTS', 'official', 1),
('feasible', '/ˈfiːzəbl/', '可行的', 'Is this plan feasible within the budget?', 'IELTS', 'official', 1),
('hypothesis', '/haɪˈpɒθəsɪs/', '假设; 假说', 'The hypothesis needs further testing.', 'IELTS', 'official', 1),
('infrastructure', '/ˈɪnfrəstrʌktʃər/', '基础设施', 'The government invested in infrastructure.', 'IELTS', 'official', 1),
('mitigate', '/ˈmɪtɪɡeɪt/', '减轻; 缓和', 'Measures were taken to mitigate the damage.', 'IELTS', 'official', 1),
('paradigm', '/ˈpærədaɪm/', '范例; 范式', 'This discovery shifted the scientific paradigm.', 'IELTS', 'official', 1),
('predominantly', '/prɪˈdɒmɪnəntli/', '主要地; 显著地', 'The audience was predominantly young adults.', 'IELTS', 'official', 1),
('resilient', '/rɪˈzɪliənt/', '有弹性的; 能恢复的', 'Children are remarkably resilient.', 'IELTS', 'official', 1),
('sustainable', '/səˈsteɪnəbl/', '可持续的', 'We must find sustainable solutions.', 'IELTS', 'official', 1),
('threshold', '/ˈθreʃhəʊld/', '门槛; 阈值', 'The country is on the threshold of change.', 'IELTS', 'official', 1),
('utilise', '/ˈjuːtɪlaɪz/', '利用', 'We should utilise all available resources.', 'IELTS', 'official', 1);

-- ========================================
-- 4. 学习记录（demo 用户最近 7 天，共 80 条）
-- ========================================
-- 词卡 ID 1=abandon, 2=benefit, 3=capture, 4-9=LEARNING, 10-14=REVIEWING,
-- 15-19=MASTERED, 20-24=NEW, 25-26=IELTS

INSERT INTO study_record (user_id, word_card_id, study_mode, answer_content, correct_answer, is_correct, duration_seconds, studied_at) VALUES
-- 6 天前（10 题，7 对 3 错）
(1, 1, 'WORD_TO_MEANING', '放弃', '放弃', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:10:00')),
(1, 2, 'WORD_TO_MEANING', '好处', '好处; 使受益', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:11:00')),
(1, 3, 'MEANING_TO_WORD', 'capture', '捕获; 记录', 0, 8, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:13:00')),
(1, 15, 'WORD_TO_MEANING', '多样性', '多样的; 不同的', 0, 6, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:15:00')),
(1, 20, 'CHOICE', '多样的; 不同的', '多样的; 不同的', 1, 5, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:17:00')),
(1, 11, 'WORD_TO_MEANING', '优势', '优势; 有利条件', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:19:00')),
(1, 12, 'MEANING_TO_WORD', 'environment', '环境', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:21:00')),
(1, 5, 'WORD_TO_MEANING', '实施', '实施; 执行', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:23:00')),
(1, 16, 'CHOICE', '模棱两可的', '模棱两可的', 1, 5, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:25:00')),
(1, 6, 'WORD_TO_MEANING', '建立', '建立; 确立', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:27:00')),

-- 5 天前（12 题，9 对 3 错）
(1, 1, 'WORD_TO_MEANING', '放弃', '放弃', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:05:00')),
(1, 4, 'MEANING_TO_WORD', 'efficient', '高效的', 1, 6, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:07:00')),
(1, 7, 'WORD_TO_MEANING', '基本的', '基本的; 根本的', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:09:00')),
(1, 10, 'CHOICE', '感知', '感知; 察觉', 1, 5, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:11:00')),
(1, 13, 'WORD_TO_MEANING', '重要的', '重要的; 显著的', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:13:00')),
(1, 14, 'MEANING_TO_WORD', 'consequence', '结果; 后果', 1, 5, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:15:00')),
(1, 8, 'WORD_TO_MEANING', '不情愿的', '不情愿的', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:17:00')),
(1, 9, 'CHOICE', '大量的', '大量的; 重大的', 0, 7, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:19:00')),
(1, 25, 'WORD_TO_MEANING', '波动', '波动; 起伏', 0, 8, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:21:00')),
(1, 2, 'MEANING_TO_WORD', 'benefit', '好处; 使受益', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:23:00')),
(1, 11, 'WORD_TO_MEANING', '优势', '优势; 有利条件', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:25:00')),
(1, 17, 'CHOICE', '综合的; 全面的', '综合的; 全面的', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:27:00')),

-- 4 天前（15 题，11 对 4 错）
(1, 3, 'WORD_TO_MEANING', '捕获', '捕获; 记录', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:00:00')),
(1, 5, 'MEANING_TO_WORD', 'implement', '实施; 执行', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:02:00')),
(1, 6, 'CHOICE', '建立; 确立', '建立; 确立', 1, 5, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:04:00')),
(1, 10, 'WORD_TO_MEANING', '感知', '感知; 察觉', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:06:00')),
(1, 15, 'MEANING_TO_WORD', 'diverse', '多样的; 不同的', 1, 5, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:08:00')),
(1, 9, 'WORD_TO_MEANING', '大量的', '大量的; 重大的', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:10:00')),
(1, 7, 'CHOICE', '基本的; 根本的', '基本的; 根本的', 0, 7, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:12:00')),
(1, 12, 'MEANING_TO_WORD', 'environment', '环境', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:14:00')),
(1, 25, 'WORD_TO_MEANING', '波动', '波动; 起伏', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:16:00')),
(1, 13, 'CHOICE', '重要的; 显著的', '重要的; 显著的', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:18:00')),
(1, 4, 'WORD_TO_MEANING', '高效的', '高效的', 0, 8, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:20:00')),
(1, 8, 'MEANING_TO_WORD', 'reluctant', '不情愿的', 0, 9, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:22:00')),
(1, 1, 'WORD_TO_MEANING', '放弃', '放弃', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:24:00')),
(1, 26, 'MEANING_TO_WORD', 'assess', '评估; 评定', 0, 7, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:26:00')),
(1, 11, 'CHOICE', '优势; 有利条件', '优势; 有利条件', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:28:00')),

-- 3 天前（8 题，6 对 2 错）
(1, 2, 'MEANING_TO_WORD', 'benefit', '好处; 使受益', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '11:00:00')),
(1, 14, 'WORD_TO_MEANING', '结果; 后果', '结果; 后果', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '11:02:00')),
(1, 3, 'CHOICE', '捕获; 记录', '捕获; 记录', 1, 5, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '11:04:00')),
(1, 7, 'WORD_TO_MEANING', '基本的', '基本的; 根本的', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '11:06:00')),
(1, 8, 'MEANING_TO_WORD', 'reluctant', '不情愿的', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '11:08:00')),
(1, 16, 'CHOICE', '模棱两可的', '模棱两可的', 1, 5, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '11:10:00')),
(1, 25, 'WORD_TO_MEANING', '波动', '波动; 起伏', 0, 7, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '11:12:00')),
(1, 5, 'MEANING_TO_WORD', 'implement', '实施; 执行', 0, 8, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '11:14:00')),

-- 2 天前（14 题，11 对 3 错）
(1, 1, 'WORD_TO_MEANING', '放弃', '放弃', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:00:00')),
(1, 4, 'MEANING_TO_WORD', 'efficient', '高效的', 1, 5, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:02:00')),
(1, 9, 'CHOICE', '大量的; 重大的', '大量的; 重大的', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:04:00')),
(1, 10, 'WORD_TO_MEANING', '感知', '感知; 察觉', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:06:00')),
(1, 12, 'MEANING_TO_WORD', 'environment', '环境', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:08:00')),
(1, 13, 'WORD_TO_MEANING', '重要的', '重要的; 显著的', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:10:00')),
(1, 15, 'CHOICE', '多样的; 不同的', '多样的; 不同的', 1, 5, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:12:00')),
(1, 6, 'MEANING_TO_WORD', 'establish', '建立; 确立', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:14:00')),
(1, 26, 'WORD_TO_MEANING', '评估', '评估; 评定', 0, 8, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:16:00')),
(1, 11, 'WORD_TO_MEANING', '优势', '优势; 有利条件', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:18:00')),
(1, 3, 'MEANING_TO_WORD', 'capture', '捕获; 记录', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:20:00')),
(1, 7, 'WORD_TO_MEANING', '基本的', '基本的; 根本的', 0, 7, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:22:00')),
(1, 14, 'CHOICE', '结果; 后果', '结果; 后果', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:24:00')),
(1, 8, 'WORD_TO_MEANING', '不情愿的', '不情愿的', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:26:00')),

-- 昨天（12 题，10 对 2 错）
(1, 2, 'WORD_TO_MEANING', '好处', '好处; 使受益', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:30:00')),
(1, 5, 'CHOICE', '实施; 执行', '实施; 执行', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:32:00')),
(1, 9, 'WORD_TO_MEANING', '大量的', '大量的; 重大的', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:34:00')),
(1, 15, 'MEANING_TO_WORD', 'diverse', '多样的; 不同的', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:36:00')),
(1, 10, 'CHOICE', '感知; 察觉', '感知; 察觉', 1, 5, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:38:00')),
(1, 13, 'MEANING_TO_WORD', 'significant', '重要的; 显著的', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:40:00')),
(1, 16, 'WORD_TO_MEANING', '模棱两可的', '模棱两可的', 0, 9, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:42:00')),
(1, 4, 'MEANING_TO_WORD', 'efficient', '高效的', 1, 5, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:44:00')),
(1, 11, 'WORD_TO_MEANING', '优势', '优势; 有利条件', 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:46:00')),
(1, 6, 'CHOICE', '建立; 确立', '建立; 确立', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:48:00')),
(1, 25, 'WORD_TO_MEANING', '波动', '波动; 起伏', 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:50:00')),
(1, 3, 'MEANING_TO_WORD', 'capture', '捕获', 0, 8, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:52:00')),

-- 今天（9 题，7 对 2 错）
(1, 1, 'WORD_TO_MEANING', '放弃', '放弃', 1, 3, TIMESTAMP(CURDATE(), '09:00:00')),
(1, 12, 'CHOICE', '环境', '环境', 1, 4, TIMESTAMP(CURDATE(), '09:02:00')),
(1, 7, 'MEANING_TO_WORD', 'fundamental', '基本的; 根本的', 1, 5, TIMESTAMP(CURDATE(), '09:04:00')),
(1, 8, 'WORD_TO_MEANING', '不情愿的', '不情愿的', 1, 3, TIMESTAMP(CURDATE(), '09:06:00')),
(1, 14, 'CHOICE', '结果; 后果', '结果; 后果', 1, 4, TIMESTAMP(CURDATE(), '09:08:00')),
(1, 26, 'WORD_TO_MEANING', '评估', '评估; 评定', 0, 8, TIMESTAMP(CURDATE(), '09:10:00')),
(1, 5, 'MEANING_TO_WORD', 'implement', '实施; 执行', 0, 9, TIMESTAMP(CURDATE(), '09:12:00')),
(1, 9, 'WORD_TO_MEANING', '大量的', '大量的; 重大的', 1, 3, TIMESTAMP(CURDATE(), '09:14:00')),
(1, 2, 'CHOICE', '好处; 使受益', '好处; 使受益', 1, 4, TIMESTAMP(CURDATE(), '09:16:00'));

-- ========================================
-- 5. 错题记录（3 ACTIVE + 3 RESOLVED）
-- ========================================

INSERT INTO wrong_word (user_id, word_card_id, wrong_count, resolved_correct_streak, last_wrong_at, status) VALUES
-- ACTIVE 错题
(1, 3, 4, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), 'ACTIVE'),
(1, 26, 3, 0, NOW(), 'ACTIVE'),
(1, 5, 2, 0, NOW(), 'ACTIVE'),
-- RESOLVED 错题
(1, 25, 2, 3, DATE_SUB(NOW(), INTERVAL 2 DAY), 'RESOLVED'),
(1, 16, 1, 3, DATE_SUB(NOW(), INTERVAL 4 DAY), 'RESOLVED'),
(1, 7, 2, 3, DATE_SUB(NOW(), INTERVAL 3 DAY), 'RESOLVED');

-- ========================================
-- 6. 每日打卡（最近 7 天连续打卡）
-- ========================================

INSERT INTO daily_checkin (user_id, checkin_date, study_count, correct_count, total_duration_seconds, points_earned) VALUES
(1, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 10, 7, 320, 7),
(1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 12, 9, 410, 9),
(1, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 15, 11, 520, 11),
(1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 8, 6, 280, 8),
(1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 14, 11, 490, 14),
(1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 12, 10, 400, 12),
(1, CURDATE(), 9, 7, 290, 9);

-- ========================================
-- 7. 更新 demo 用户积分汇总
-- ========================================

UPDATE user_points SET
    total_points = 220,
    streak_days = 7,
    max_streak_days = 7,
    total_studied = 80,
    total_correct = 61,
    total_duration_seconds = 2850,
    last_checkin_date = CURDATE()
WHERE user_id = 1;

-- ========================================
-- 8. 为 admin 用户补基础积分记录
-- ========================================

UPDATE user_points SET
    total_points = 0,
    streak_days = 0,
    max_streak_days = 0,
    total_studied = 0,
    total_correct = 0,
    total_duration_seconds = 0
WHERE user_id = 2;