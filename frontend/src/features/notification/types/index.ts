export type NotificationType =
  | 'DEFECT_DETECTED'
  | 'REINSPECTION_REQUIRED'
  | 'REVIEW_COMPLETED'
  | 'SYSTEM_ERROR'
  | 'DOCUMENT_INDEXING_FAILED'
  | 'SIGNUP_REQUESTED'
  | 'REPORT_GENERATED'

export type NotificationSeverity = 'INFO' | 'WARNING' | 'CRITICAL'

export type NotificationFilter = 'ALL' | 'UNREAD' | 'ANOMALY' | 'SYSTEM' | 'REPORT'

export type NotificationServerFilterType = 'SYSTEM_ERROR' | 'REPORT_GENERATED'

export type NotificationVisualGroup = 'anomaly' | 'review' | 'system' | 'user' | 'report'

export interface NotificationSummaryItem {
  notificationId: number
  notificationType: NotificationType
  severity: NotificationSeverity
  title: string
  isRead: boolean
  createdAt: string
}

export interface NotificationDetail {
  notificationId: number
  notificationType: NotificationType
  severity: NotificationSeverity
  title: string
  message: string
  targetUrl: string | null
  isRead: boolean
}

export interface NotificationDetailViewModel extends NotificationDetail {
  createdAt?: string
}

export interface NotificationListData {
  content: NotificationSummaryItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface NotificationListQuery {
  type?: NotificationServerFilterType
  isRead?: boolean
  page?: number
  size?: number
  sort?: string
}

export const notificationTypeLabelMap: Record<NotificationType, string> = {
  DEFECT_DETECTED: '불량 감지',
  REINSPECTION_REQUIRED: '재검사 필요',
  REVIEW_COMPLETED: '재검토 완료',
  SYSTEM_ERROR: '시스템 오류',
  DOCUMENT_INDEXING_FAILED: '문서 인덱싱 실패',
  SIGNUP_REQUESTED: '가입 신청',
  REPORT_GENERATED: '보고서 생성',
}

export const severityLabelMap: Record<NotificationSeverity, string> = {
  INFO: '정보',
  WARNING: '주의',
  CRITICAL: '긴급',
}

export const notificationVisualGroupMap: Record<NotificationType, NotificationVisualGroup> = {
  DEFECT_DETECTED: 'anomaly',
  REINSPECTION_REQUIRED: 'anomaly',
  REVIEW_COMPLETED: 'review',
  SYSTEM_ERROR: 'system',
  DOCUMENT_INDEXING_FAILED: 'system',
  SIGNUP_REQUESTED: 'user',
  REPORT_GENERATED: 'report',
}
