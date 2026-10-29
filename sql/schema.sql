-- 与 im-server 共用 cao_im_db。请先执行 im-server/sql/schema.sql。
USE cao_im_db;

CREATE TABLE IF NOT EXISTS push_device (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'IM 用户 ID',
    platform VARCHAR(16) NOT NULL COMMENT 'android 或 ios',
    provider VARCHAR(32) NOT NULL COMMENT 'umeng 及厂商通道名',
    token VARCHAR(512) NOT NULL COMMENT '友盟 device token 或厂商设备令牌',
    device_id VARCHAR(128) NOT NULL COMMENT '客户端设备标识，同一设备覆盖旧令牌',
    enabled TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_device (user_id, device_id),
    KEY idx_user_enabled (user_id, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='离线推送设备';

CREATE TABLE IF NOT EXISTS push_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    biz_type VARCHAR(32) NOT NULL,
    biz_id VARCHAR(64) DEFAULT '',
    title VARCHAR(64) NOT NULL,
    body VARCHAR(160) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL COMMENT 'logged/sent/skipped/failed',
    detail VARCHAR(255) DEFAULT '',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_biz (user_id, biz_type, biz_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='离线推送记录';
