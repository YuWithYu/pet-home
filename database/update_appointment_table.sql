-- 更新appointment表，添加核销码相关字段
ALTER TABLE appointment ADD COLUMN verify_code VARCHAR(64) COMMENT '核销码';
ALTER TABLE appointment ADD COLUMN is_verified TINYINT DEFAULT 0 COMMENT '是否已核销 0-未核销 1-已核销';
ALTER TABLE appointment ADD COLUMN verify_time DATETIME COMMENT '核销时间';

-- 为核销码添加索引，提高查询效率
CREATE INDEX idx_verify_code ON appointment(verify_code);
