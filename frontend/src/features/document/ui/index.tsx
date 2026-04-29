import { useDocumentUpload } from '../model'
import type { DocumentFormData, DocumentFormErrors, UploadStep } from '../types'

interface DocumentUploadFormProps {
  organizationId: number
  onSuccess?: () => void
}

// 인덱싱 단계 표시 컴포넌트
const IndexingSteps = ({ step, progress }: { step: UploadStep; progress: number }) => {
  const steps = [
    { label: '업로드 완료', step: 'UPLOADING' },
    { label: '텍스트 추출', step: 'INDEXING' },
    { label: '청크 생성', step: 'INDEXING' },
    { label: '임베딩 처리', step: 'INDEXING' },
    { label: '인덱싱 완료', step: 'COMPLETED' }
  ]

  const getStepStatus = (index: number) => {
    if (step === 'COMPLETED') return 'completed'
    if (step === 'UPLOADING' && index === 0) return 'active'
    if (step === 'INDEXING') {
      const activeIndex = Math.floor((progress / 100) * steps.length)
      if (index < activeIndex) return 'completed'
      if (index === activeIndex) return 'active'
    }
    return 'pending'
  }

  return (
    <div className="mb-4">
      <div className="d-flex justify-content-between align-items-center mb-2">
        {steps.map((s, index) => {
          const status = getStepStatus(index)
          return (
            <div key={index} className="d-flex flex-column align-items-center" style={{ flex: 1 }}>
              <div
                className="rounded-circle d-flex align-items-center justify-content-center mb-1"
                style={{
                  width: '32px',
                  height: '32px',
                  backgroundColor:
                    status === 'completed' ? '#198754' :
                    status === 'active' ? '#0d6efd' : '#dee2e6',
                  color: status !== 'pending' ? '#fff' : '#6c757d',
                  fontSize: '12px',
                  fontWeight: 'bold'
                }}
              >
                {status === 'completed' ? '✓' : index + 1}
              </div>
              <small
                style={{
                  fontSize: '10px',
                  color:
                    status === 'completed' ? '#198754' :
                    status === 'active' ? '#0d6efd' : '#6c757d',
                  textAlign: 'center',
                  whiteSpace: 'nowrap'
                }}
              >
                {s.label}
              </small>
            </div>
          )
        })}
      </div>

      {/* 진행률 바 */}
      <div className="progress mt-2" style={{ height: '8px' }}>
        <div
          className="progress-bar progress-bar-striped progress-bar-animated"
          style={{ width: `${progress}%` }}
        />
      </div>
      <div className="text-end small text-muted mt-1">{progress}%</div>
    </div>
  )
}

// 입력 필드 컴포넌트
const InputField = ({
  label,
  name,
  type = 'text',
  value,
  error,
  onChange,
  placeholder,
  required = false
}: {
  label: string
  name: keyof DocumentFormData | keyof DocumentFormErrors
  type?: string
  value: string
  error?: string
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void
  placeholder?: string
  required?: boolean
}) => (
  <div className="mb-3">
    <label htmlFor={name} className="form-label fw-medium">
      {label} {required && <span className="text-danger">*</span>}
    </label>
    <input
      type={type}
      className={`form-control ${error ? 'is-invalid' : ''}`}
      id={name}
      name={name}
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      required={required}
    />
    {error && <div className="invalid-feedback">{error}</div>}
  </div>
)

export const DocumentUploadForm = ({
  organizationId,
  onSuccess
}: DocumentUploadFormProps) => {
  const {
    formData,
    errors,
    uploadStep,
    progress,
    response,
    errorMessage,
    handleChange,
    handleFileChange,
    handleSubmit,
    resetForm
  } = useDocumentUpload(organizationId)

  // 업로드/인덱싱 중 화면
  if (uploadStep === 'UPLOADING' || uploadStep === 'INDEXING') {
    return (
      <div>
        <h5 className="fw-bold mb-1">문서 처리 중</h5>
        <p className="text-muted small mb-4">
          문서를 업로드하고 검색 가능한 형태로 변환하고 있습니다.
        </p>
        <IndexingSteps step={uploadStep} progress={progress} />
        <div className="alert alert-info small">
          <span className="me-1">ℹ</span>
          처리 중에는 페이지를 벗어나지 마세요.
        </div>
      </div>
    )
  }

  // 완료 화면
  if (uploadStep === 'COMPLETED') {
    return (
      <div className="text-center py-4">
        <div
          className="rounded-circle bg-success d-flex align-items-center justify-content-center mx-auto mb-4"
          style={{ width: '80px', height: '80px' }}
        >
          <span className="text-white" style={{ fontSize: '2rem' }}>✓</span>
        </div>
        <h5 className="fw-bold mb-2">문서 업로드 완료!</h5>
        <p className="text-muted mb-4">
          문서가 성공적으로 업로드되었습니다.<br />
          검색에 바로 반영됩니다.
        </p>

        {/* 업로드된 문서 정보 */}
        {response && (
          <div className="card bg-light border-0 p-3 mb-4 text-start">
            <div className="small text-muted mb-1">문서 제목</div>
            <div className="fw-medium mb-2">{response.title}</div>
            <div className="small text-muted mb-1">문서 유형</div>
            <div className="fw-medium">{response.documentType}</div>
          </div>
        )}

        <div className="d-flex gap-2 justify-content-center">
          <button
            className="btn btn-outline-secondary"
            onClick={resetForm}
          >
            다른 문서 업로드
          </button>
          {onSuccess && (
            <button
              className="btn btn-primary"
              onClick={onSuccess}
            >
              목록으로 이동
            </button>
          )}
        </div>
      </div>
    )
  }

  // 실패 화면
  if (uploadStep === 'FAILED') {
    return (
      <div className="text-center py-4">
        <div
          className="rounded-circle bg-danger d-flex align-items-center justify-content-center mx-auto mb-4"
          style={{ width: '80px', height: '80px' }}
        >
          <span className="text-white" style={{ fontSize: '2rem' }}>✗</span>
        </div>
        <h5 className="fw-bold mb-2">업로드 실패</h5>
        <p className="text-muted mb-4">{errorMessage}</p>
        <button className="btn btn-primary" onClick={resetForm}>
          다시 시도
        </button>
      </div>
    )
  }

  // 기본 폼 화면
  return (
    <div>
      <form onSubmit={handleSubmit}>

        {/* 문서 파일 업로드 */}
        <div className="mb-3">
          <label htmlFor="file" className="form-label fw-medium">
            문서 파일 <span className="text-danger">*</span>
          </label>
          <div
            className={`border rounded p-4 text-center ${errors.file ? 'border-danger' : 'border-dashed'}`}
            style={{ borderStyle: 'dashed', cursor: 'pointer' }}
            onClick={() => document.getElementById('file')?.click()}
          >
            {formData.file ? (
              <div className="d-flex align-items-center justify-content-center gap-2">
                <span>📄</span>
                <div className="text-start">
                  <div className="fw-medium">{formData.file.name}</div>
                  <div className="small text-muted">
                    {(formData.file.size / 1024 / 1024).toFixed(2)} MB
                  </div>
                </div>
              </div>
            ) : (
              <div>
                <div className="mb-2" style={{ fontSize: '2rem' }}>⬆</div>
                <div className="fw-medium">파일을 드래그하거나 클릭하여 업로드하세요.</div>
                <div className="small text-muted mt-1">
                  PDF, DOCX, XLSX, PPTX, TXT 파일 (최대 100MB)
                </div>
              </div>
            )}
          </div>
          <input
            type="file"
            id="file"
            name="file"
            className="d-none"
            onChange={handleFileChange}
            accept=".pdf,.docx,.xlsx,.pptx,.txt"
          />
          {errors.file && (
            <div className="text-danger small mt-1">{errors.file}</div>
          )}
        </div>

        {/* 메타데이터 */}
        <InputField
          label="문서 제목"
          name="title"
          value={formData.title}
          error={errors.title}
          onChange={handleChange}
          placeholder="문서 제목을 입력하세요"
          required
        />

        {/* 문서 유형 */}
        <div className="mb-3">
          <label htmlFor="documentType" className="form-label fw-medium">
            문서 유형 <span className="text-danger">*</span>
          </label>
          <select
            className={`form-select ${errors.documentType ? 'is-invalid' : ''}`}
            id="documentType"
            name="documentType"
            value={formData.documentType}
            onChange={handleChange}
            required
          >
            <option value="">문서 유형을 선택하세요</option>
            <option value="MANUAL">설비 매뉴얼</option>
            <option value="CHECKLIST">점검 체크리스트</option>
            <option value="TROUBLESHOOTING">장애 대응</option>
            <option value="STANDARD">점검 기준서</option>
            <option value="REPORT">보고서</option>
            <option value="ETC">기타</option>
          </select>
          {errors.documentType && (
            <div className="invalid-feedback">{errors.documentType}</div>
          )}
        </div>

        {/* 설명 */}
        <div className="mb-3">
          <label htmlFor="description" className="form-label fw-medium">
            설명
          </label>
          <textarea
            className="form-control"
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange as React.ChangeEventHandler<HTMLTextAreaElement>}
            placeholder="문서에 대한 설명을 입력하세요"
            rows={3}
          />
        </div>

        {/* 태그 */}
        <div className="mb-3">
          <label htmlFor="tags" className="form-label fw-medium">
            태그
          </label>
          <input
            type="text"
            className="form-control"
            id="tags"
            name="tags"
            value={formData.tags}
            onChange={handleChange}
            placeholder="태그를 입력하세요 (쉼표로 구분)"
          />
          <div className="form-text text-muted">예: 설비관리, 점검, 안전</div>
        </div>

        {/* 에러 메시지 */}
        {errorMessage && (
          <div className="alert alert-danger mb-3">
            {errorMessage}
          </div>
        )}

        {/* 버튼 */}
        <div className="d-flex gap-2">
          <button
            type="button"
            className="btn btn-outline-secondary flex-fill"
            onClick={resetForm}
          >
            취소
          </button>
          <button
            type="submit"
            className="btn btn-primary flex-fill"
          >
            우선 저장
          </button>
          <button
            type="submit"
            className="btn btn-success flex-fill"
          >
            문서 저장
          </button>
        </div>
      </form>
    </div>
  )
}

export default DocumentUploadForm