import { useEffect } from 'react'
import { useSignup } from '../model'
import type { SignupFormData, SignupFormErrors } from '../types'

interface SignupFormProps {
  googleEmail?: string
  googleName?: string
  googleSub?: string
  googlePicture?: string
}

// 단계 표시 컴포넌트
const StepIndicator = ({ currentStep }: { currentStep: number }) => {
  const steps = ['계정 확인', '기본 정보 입력', '가입 완료']
  return (
    <div className="d-flex align-items-center justify-content-center mb-4">
      {steps.map((label, index) => {
        const stepNum = index + 1
        const isCompleted = currentStep > stepNum
        const isActive = currentStep === stepNum
        return (
          <div key={stepNum} className="d-flex align-items-center">
            <div className="d-flex flex-column align-items-center">
              <div
                className="rounded-circle d-flex align-items-center justify-content-center fw-bold"
                style={{
                  width: '36px',
                  height: '36px',
                  backgroundColor: isCompleted ? '#198754' : isActive ? '#0d6efd' : '#dee2e6',
                  color: isCompleted || isActive ? '#fff' : '#6c757d',
                  fontSize: '14px'
                }}
              >
                {isCompleted ? '✓' : stepNum}
              </div>
              <small
                className="mt-1"
                style={{
                  color: isActive ? '#0d6efd' : isCompleted ? '#198754' : '#6c757d',
                  fontSize: '11px',
                  whiteSpace: 'nowrap'
                }}
              >
                {label}
              </small>
            </div>
            {index < steps.length - 1 && (
              <div
                style={{
                  width: '60px',
                  height: '2px',
                  backgroundColor: isCompleted ? '#198754' : '#dee2e6',
                  margin: '0 8px',
                  marginBottom: '20px'
                }}
              />
            )}
          </div>
        )
      })}
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
  required = false,
  readOnly = false
}: {
  label: string
  name: keyof SignupFormData | keyof SignupFormErrors
  type?: string
  value: string
  error?: string
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void
  placeholder?: string
  required?: boolean
  readOnly?: boolean
}) => (
  <div className="mb-3">
    <label htmlFor={name} className="form-label fw-medium">
      {label} {required && <span className="text-danger">*</span>}
    </label>
    <input
      type={type}
      className={`form-control ${error ? 'is-invalid' : ''} ${readOnly ? 'bg-light' : ''}`}
      id={name}
      name={name}
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      required={required}
      readOnly={readOnly}
    />
    {error && <div className="invalid-feedback">{error}</div>}
  </div>
)

export const SignupForm = ({ googleEmail, googleName, googleSub, googlePicture }: SignupFormProps) => {
  const {
    step,
    formData,
    errors,
    isLoading,
    errorMessage,
    handleChange,
    handleNextStep,
    handlePrevStep,
    handleSubmit,
    handleGoToMain,
    setFormData
  } = useSignup(googleSub, googlePicture)

  // Google 정보 자동 세팅
  useEffect(() => {
    if (googleEmail) {
      setFormData(prev => ({
        ...prev,
        email: googleEmail,
        name: googleName || prev.name
      }))
    }
  }, [googleEmail, googleName, setFormData])

  return (
    <div>
      {/* 단계 표시 */}
      <StepIndicator currentStep={step} />

      {/* 1단계: 계정 확인 */}
      {step === 1 && (
        <div>
          <h5 className="fw-bold mb-1">계정 확인</h5>
          <p className="text-muted small mb-4">Google 계정 정보를 확인해주세요</p>

          <div className="alert alert-success d-flex align-items-center mb-4">
            <span className="me-2">✓</span>
            <span>Google 계정 연동 완료</span>
          </div>

          <div className="card bg-light border-0 p-3 mb-4">
            <div className="d-flex align-items-center">
              {googlePicture ? (
                <img
                  src={googlePicture}
                  alt="프로필"
                  className="rounded-circle me-3"
                  style={{ width: '48px', height: '48px' }}
                />
              ) : (
                <div
                  className="rounded-circle bg-secondary d-flex align-items-center justify-content-center me-3"
                  style={{ width: '48px', height: '48px' }}
                >
                  <span className="text-white fw-bold">
                    {googleName?.charAt(0) || 'U'}
                  </span>
                </div>
              )}
              <div>
                <div className="fw-medium">{googleName || '이름 없음'}</div>
                <div className="text-muted small">{googleEmail || '이메일 없음'}</div>
              </div>
            </div>
          </div>

          <div className="alert alert-info small mb-4">
            <span className="me-1">ℹ</span>
            입력한 정보는 권한 및 알림 수신에 활용됩니다.
          </div>

          <div className="d-grid">
            <button
              className="btn btn-primary btn-lg"
              onClick={handleNextStep}
            >
              다음 →
            </button>
          </div>
        </div>
      )}

      {/* 2단계: 기본 정보 입력 */}
      {step === 2 && (
        <div>
          <h5 className="fw-bold mb-1">기본 정보 입력</h5>
          <p className="text-muted small mb-4">서비스 이용을 위한 추가 정보를 입력해주세요</p>

          <form onSubmit={handleSubmit}>
            <InputField
              label="이름"
              name="name"
              value={formData.name}
              error={errors.name}
              onChange={handleChange}
              placeholder="이름을 입력하세요"
              required
              readOnly={!!googleName}
            />

            <InputField
              label="휴대전화"
              name="phone"
              type="tel"
              value={formData.phone}
              error={errors.phone}
              onChange={handleChange}
              placeholder="010-0000-0000"
              required
            />

            <InputField
              label="소속 조직"
              name="company"
              value={formData.company}
              error={errors.company}
              onChange={handleChange}
              placeholder="회사/조직명을 입력하세요"
              required
            />

            <InputField
              label="부서"
              name="department"
              value={formData.department}
              error={errors.department}
              onChange={handleChange}
              placeholder="부서명을 입력하세요"
            />

            <InputField
              label="직책"
              name="position"
              value={formData.position}
              error={errors.position}
              onChange={handleChange}
              placeholder="직책을 입력하세요"
            />

            {errorMessage && (
              <div className="alert alert-danger mb-3">
                {errorMessage}
              </div>
            )}

            <div className="d-flex gap-2 mt-4">
              <button
                type="button"
                className="btn btn-outline-secondary flex-fill"
                onClick={handlePrevStep}
              >
                ← 이전
              </button>
              <button
                type="submit"
                className="btn btn-primary flex-fill"
                disabled={isLoading}
              >
                {isLoading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    처리 중...
                  </>
                ) : (
                  '가입 완료 →'
                )}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* 3단계: 가입 완료 */}
      {step === 3 && (
        <div className="text-center py-4">
          <div
            className="rounded-circle bg-success d-flex align-items-center justify-content-center mx-auto mb-4"
            style={{ width: '80px', height: '80px' }}
          >
            <span className="text-white" style={{ fontSize: '2rem' }}>✓</span>
          </div>
          <h4 className="fw-bold mb-3">회원가입 신청 완료!</h4>
          <p className="text-muted mb-2">
            관리자 승인 후 로그인이 가능합니다.
          </p>
          <p className="text-muted mb-4">
            승인까지 최대 1~2일 소요됩니다.
          </p>
          <div className="card bg-light border-0 p-3 mb-4 text-start">
            <div className="small text-muted mb-1">가입 이메일</div>
            <div className="fw-medium">{formData.email}</div>
          </div>
          <button
            className="btn btn-primary btn-lg w-100"
            onClick={handleGoToMain}
          >
            메인으로 이동
          </button>
        </div>
      )}
    </div>
  )
}

export default SignupForm