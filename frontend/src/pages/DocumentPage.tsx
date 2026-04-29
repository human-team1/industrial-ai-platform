import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { DocumentUploadForm } from '../features/document/ui'

type PageMode = 'list' | 'upload'

export function DocumentPage() {
  const navigate = useNavigate()
  const [mode, setMode] = useState<PageMode>('list')

  const handleSuccess = () => {
    setMode('list')
  }

  return (
    <section className="container py-4">

      {/* 페이지 헤더 */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h1 className="h4 fw-bold mb-1">문서 관리</h1>
          <p className="text-muted small mb-0">
            등록된 문서를 조회하고 관리할 수 있습니다.
          </p>
        </div>
        <div className="d-flex gap-2">
          <button
            className="btn btn-outline-secondary btn-sm"
            onClick={() => navigate(0)} // 새로고침
          >
            ↻ 동기화
          </button>
          <button
            className="btn btn-primary btn-sm"
            onClick={() => setMode('upload')}
          >
            + 문서 등록
          </button>
        </div>
      </div>

      {mode === 'list' ? (
        <div>
          {/* 필터 */}
          <div className="card border-0 shadow-sm mb-4">
            <div className="card-body">
              <div className="row g-2 align-items-center">
                <div className="col-md-3">
                  <select className="form-select form-select-sm">
                    <option value="">문서 유형 전체</option>
                    <option value="MANUAL">설비 매뉴얼</option>
                    <option value="CHECKLIST">점검 체크리스트</option>
                    <option value="TROUBLESHOOTING">장애 대응</option>
                    <option value="STANDARD">점검 기준서</option>
                    <option value="REPORT">보고서</option>
                    <option value="ETC">기타</option>
                  </select>
                </div>
                <div className="col-md-3">
                  <select className="form-select form-select-sm">
                    <option value="">인덱싱 상태 전체</option>
                    <option value="PENDING">대기</option>
                    <option value="PROCESSING">처리중</option>
                    <option value="COMPLETED">완료</option>
                    <option value="FAILED">실패</option>
                  </select>
                </div>
                <div className="col-md-4">
                  <input
                    type="text"
                    className="form-control form-control-sm"
                    placeholder="문서명, 키워드, 작성자로 검색하세요."
                  />
                </div>
                <div className="col-md-2">
                  <button className="btn btn-outline-secondary btn-sm w-100">
                    ↺ 필터 초기화
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* 문서 통계 */}
          <div className="row g-3 mb-4">
            <div className="col-md-3">
              <div className="card border-0 shadow-sm text-center p-3">
                <div className="h4 fw-bold mb-0">0</div>
                <div className="small text-muted">전체 문서</div>
              </div>
            </div>
            <div className="col-md-3">
              <div className="card border-0 shadow-sm text-center p-3">
                <div className="h4 fw-bold mb-0 text-success">0</div>
                <div className="small text-muted">인덱싱 완료</div>
              </div>
            </div>
            <div className="col-md-3">
              <div className="card border-0 shadow-sm text-center p-3">
                <div className="h4 fw-bold mb-0 text-warning">0</div>
                <div className="small text-muted">처리 중</div>
              </div>
            </div>
            <div className="col-md-3">
              <div className="card border-0 shadow-sm text-center p-3">
                <div className="h4 fw-bold mb-0 text-danger">0</div>
                <div className="small text-muted">실패</div>
              </div>
            </div>
          </div>

          {/* 문서 목록 테이블 */}
          <div className="card border-0 shadow-sm">
            <div className="card-body p-0">
              <table className="table table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    <th className="px-3 py-2">문서명</th>
                    <th className="px-3 py-2">유형</th>
                    <th className="px-3 py-2">인덱싱 상태</th>
                    <th className="px-3 py-2">작성일</th>
                    <th className="px-3 py-2">작성자</th>
                    <th className="px-3 py-2">상세/수정</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td colSpan={6} className="text-center py-5 text-muted">
                      등록된 문서가 없습니다.
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            {/* 페이지네이션 */}
            <div className="card-footer bg-white d-flex justify-content-between align-items-center">
              <div className="small text-muted">총 0건</div>
              <nav>
                <ul className="pagination pagination-sm mb-0">
                  <li className="page-item disabled">
                    <span className="page-link">«</span>
                  </li>
                  <li className="page-item active">
                    <span className="page-link">1</span>
                  </li>
                  <li className="page-item disabled">
                    <span className="page-link">»</span>
                  </li>
                </ul>
              </nav>
              <select className="form-select form-select-sm" style={{ width: 'auto' }}>
                <option value="10">10개 보기</option>
                <option value="20">20개 보기</option>
                <option value="50">50개 보기</option>
              </select>
            </div>
          </div>
        </div>
      ) : (
        /* 문서 등록 화면 */
        <div className="row justify-content-center">
          <div className="col-md-8 col-lg-7">
            <div className="card border-0 shadow-sm">
              <div className="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                <div>
                  <h5 className="mb-0 fw-bold">문서 등록 / 수정</h5>
                  <p className="text-muted small mb-0 mt-1">
                    문서를 업로드하고 메타데이터를 설정하여 인덱싱 상태를 관리합니다.
                  </p>
                </div>
                <button
                  className="btn btn-outline-secondary btn-sm"
                  onClick={() => setMode('list')}
                >
                  ← 목록으로
                </button>
              </div>
              <div className="card-body">
                <DocumentUploadForm
                  organizationId={1} // TODO: 실제 인증 구현 후 교체
                  onSuccess={handleSuccess}
                />
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}