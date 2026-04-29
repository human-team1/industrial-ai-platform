// 문서 유형
export type DocumentType =
  | 'MANUAL'
  | 'CHECKLIST'
  | 'TROUBLESHOOTING'
  | 'STANDARD'
  | 'REPORT'
  | 'ETC'

// 문서 상태
export type DocumentStatus = 'ACTIVE' | 'DELETED'

// 인덱싱 상태
export type IndexingStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'

// 인덱싱 잡 상태
export type DocumentIndexJobStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'

// 문서 생성 요청 타입
export interface CreateDocumentRequest {
  title: string
  documentType: DocumentType
  fileId: number
  organizationId: number
}

// 문서 생성 응답 타입
export interface CreateDocumentResponse {
  documentId: number
  organizationId: number
  ownerUserId: number
  title: string
  documentType: DocumentType
  currentStatus: DocumentStatus
  latestVersionId?: number // ✅ 인덱싱 상태 조회에 필요
}

// 인덱싱 상태 타입
export interface DocumentIndexingStatus {
  documentVersionId: number
  indexingStatus: IndexingStatus
  jobStatus: DocumentIndexJobStatus
  errorMessage?: string
}

// 업로드 단계
export type UploadStep =
  | 'IDLE'          // 초기 상태
  | 'UPLOADING'     // 파일 업로드 중
  | 'INDEXING'      // 인덱싱 중
  | 'COMPLETED'     // 완료
  | 'FAILED'        // 실패

// 폼 데이터 타입
export interface DocumentFormData {
  title: string
  documentType: DocumentType | ''
  description: string
  tags: string
  file: File | null
}

// 폼 에러 타입
export interface DocumentFormErrors {
  title?: string
  documentType?: string
  file?: string
}