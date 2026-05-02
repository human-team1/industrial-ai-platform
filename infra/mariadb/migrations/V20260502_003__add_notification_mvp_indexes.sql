-- Notification MVP indexes (목록 필터/Unread 카운트/dedup 중복 방지)
CREATE INDEX IF NOT EXISTS idx_notification_user_read_created
  ON notification(user_id, is_read, created_at);

CREATE INDEX IF NOT EXISTS idx_notification_user_type_created
  ON notification(user_id, notification_type, created_at);

CREATE INDEX IF NOT EXISTS idx_notification_user_dedup
  ON notification(user_id, dedup_key);
