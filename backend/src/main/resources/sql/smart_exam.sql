-- 用户表
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号/学号',
  `role` TINYINT NOT NULL DEFAULT 2 COMMENT '角色：1-管理员，2-教师，3-学生',
  `clazz_id` BIGINT COMMENT '班级ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_clazz` (`clazz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 题目表
CREATE TABLE `question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `content` TEXT NOT NULL COMMENT '题目内容',
  `type` TINYINT NOT NULL COMMENT '题目类型：1-单选题，2-多选题，3-判断题，4-填空题，5-简答题',
  `difficulty` TINYINT NOT NULL DEFAULT 1 COMMENT '难度：1-简单，2-中等，3-困难',
  `subject_id` BIGINT COMMENT '科目ID',
  `chapter_id` BIGINT COMMENT '章节ID',
  `knowledge_point` VARCHAR(255) COMMENT '知识点',
  `options` JSON COMMENT '选项（JSON格式）',
  `answer` TEXT NOT NULL COMMENT '答案',
  `analysis` TEXT COMMENT '解析',
  `score` DECIMAL(5,2) NOT NULL DEFAULT 5.00 COMMENT '分值',
  `creator_id` BIGINT COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_difficulty` (`difficulty`),
  KEY `idx_subject` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- 科目表
CREATE TABLE `subject` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '科目ID',
  `name` VARCHAR(100) NOT NULL COMMENT '科目名称',
  `code` VARCHAR(50) COMMENT '科目代码',
  `description` TEXT COMMENT '科目描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目表';

-- 试卷表
CREATE TABLE `exam_paper` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '试卷ID',
  `title` VARCHAR(200) NOT NULL COMMENT '试卷标题',
  `description` TEXT COMMENT '试卷描述',
  `subject_id` BIGINT COMMENT '科目ID',
  `total_score` DECIMAL(5,2) NOT NULL DEFAULT 100.00 COMMENT '总分',
  `duration` INT NOT NULL COMMENT '考试时长（分钟）',
  `question_count` INT NOT NULL COMMENT '题目数量',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布',
  `creator_id` BIGINT COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_subject` (`subject_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 试卷题目关联表
CREATE TABLE `exam_paper_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `paper_id` BIGINT NOT NULL COMMENT '试卷ID',
  `question_id` BIGINT NOT NULL COMMENT '题目ID',
  `question_order` INT NOT NULL COMMENT '题目顺序',
  `score` DECIMAL(5,2) NOT NULL COMMENT '该题分值',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_paper_question` (`paper_id`, `question_id`),
  KEY `idx_paper` (`paper_id`),
  KEY `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表';

-- 考试表
CREATE TABLE `exam` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '考试ID',
  `title` VARCHAR(200) NOT NULL COMMENT '考试标题',
  `paper_id` BIGINT NOT NULL COMMENT '试卷ID',
  `subject_id` BIGINT COMMENT '科目ID',
  `clazz_ids` VARCHAR(500) COMMENT '发布班级ID列表，逗号分隔',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `duration` INT NOT NULL COMMENT '考试时长（分钟）',
  `description` TEXT COMMENT '考试描述',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未发布，1-进行中，2-已结束',
  `creator_id` BIGINT COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_paper` (`paper_id`),
  KEY `idx_status` (`status`),
  KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试表';

-- 考试记录表
CREATE TABLE `exam_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `exam_id` BIGINT NOT NULL COMMENT '考试ID',
  `paper_id` BIGINT NOT NULL COMMENT '试卷ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `clazz_id` BIGINT COMMENT '班级ID',
  `start_time` DATETIME COMMENT '开始答题时间',
  `submit_time` DATETIME COMMENT '提交时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-答题中，2-已提交，3-已阅卷，4-已超时',
  `score` DECIMAL(5,2) COMMENT '得分',
  `objective_score` DECIMAL(5,2) COMMENT '客观题得分',
  `subjective_score` DECIMAL(5,2) COMMENT '主观题得分',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_user` (`exam_id`, `user_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_clazz` (`clazz_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- 答题记录表
CREATE TABLE `answer_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `exam_record_id` BIGINT NOT NULL COMMENT '考试记录ID',
  `question_id` BIGINT NOT NULL COMMENT '题目ID',
  `user_answer` TEXT COMMENT '用户答案',
  `is_correct` TINYINT COMMENT '是否正确：1-正确，0-错误，2-待阅卷',
  `score` DECIMAL(5,2) COMMENT '得分',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_question` (`exam_record_id`, `question_id`),
  KEY `idx_exam_record` (`exam_record_id`),
  KEY `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题记录表';

-- 成绩统计表
CREATE TABLE `score_statistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `exam_id` BIGINT NOT NULL COMMENT '考试ID',
  `subject_id` BIGINT COMMENT '科目ID',
  `total_count` INT NOT NULL DEFAULT 0 COMMENT '总人数',
  `submit_count` INT NOT NULL DEFAULT 0 COMMENT '提交人数',
  `avg_score` DECIMAL(5,2) COMMENT '平均分',
  `max_score` DECIMAL(5,2) COMMENT '最高分',
  `min_score` DECIMAL(5,2) COMMENT '最低分',
  `pass_rate` DECIMAL(5,2) COMMENT '及格率',
  `excellent_rate` DECIMAL(5,2) COMMENT '优秀率',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam` (`exam_id`),
  KEY `idx_subject` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩统计表';

-- 班级表
CREATE TABLE `clazz` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `class_name` VARCHAR(100) NOT NULL COMMENT '班级名称',
  `grade` VARCHAR(50) NOT NULL COMMENT '年级',
  `major` VARCHAR(200) COMMENT '专业',
  `teacher_id` BIGINT COMMENT '班主任ID',
  `student_count` INT DEFAULT 0 COMMENT '学生人数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 插入默认管理员账号
INSERT INTO `user` (`username`, `password`, `real_name`, `email`, `role`, `status`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@smartexam.com', 1, 1);

-- 插入测试班级数据
INSERT INTO `clazz` (`class_name`, `grade`, `major`, `student_count`) VALUES 
('计算机科学1班', '大一', '计算机科学与技术', 45),
('软件工程2班', '大二', '软件工程', 42),
('大数据3班', '大三', '数据科学与大数据技术', 38),
('人工智能1班', '大一', '人工智能', 40),
('计算机网络班', '大四', '网络工程', 35);

-- 插入测试学生数据
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `role`, `clazz_id`, `status`) VALUES 
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', '2024001', 3, 1, 1),
('student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', '2024002', 3, 1, 1),
('student3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王五', '2024003', 3, 2, 1),
('student4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵六', '2024004', 3, 2, 1),
('student5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '孙七', '2024005', 3, 3, 1);

-- 插入测试科目
INSERT INTO `subject` (`name`, `code`, `description`) VALUES 
('Java程序设计', 'JAVA', 'Java编程语言基础与进阶'),
('数据库原理', 'DB', '数据库系统原理与应用'),
('计算机网络', 'NETWORK', '计算机网络基础理论');

-- 插入测试考试数据
INSERT INTO `exam` (`title`, `paper_id`, `subject_id`, `clazz_ids`, `start_time`, `end_time`, `duration`, `description`, `status`, `creator_id`) VALUES 
('2024年期中数学考试', 1, 1, '1,2', '2026-04-15 09:00:00', '2026-04-15 11:00:00', 120, '第一学期期中考试', 0, 1),
('计算机基础期末考试', 2, 2, '1,3', '2026-04-20 14:00:00', '2026-04-20 16:00:00', 120, '计算机基础期末测试', 1, 1),
('英语四级模拟考试', 3, 3, '2,4', '2026-04-10 08:00:00', '2026-04-10 10:20:00', 140, 'CET4模拟考试', 2, 1);
