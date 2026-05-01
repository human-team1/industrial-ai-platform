import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useUploadInspection } from '../features/inspection/model'
import {
  AnalysisInsightCard,
  FileUploadCard,
  InspectionRunCard,
  PreviewCard,
  ProgressStatusCard,
  ResultSummaryCard,
} from '../features/inspection/ui'

export function UploadInspectionPage() {
  const navigate = useNavigate()
  const [settingMessage, setSettingMessage] = useState<string | null>(null)
  const inspection = useUploadInspection()

  return (
    <section className="space-y-5">
      <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <p className="text-xs font-semibold uppercase tracking-wide text-blue-600">Upload Inspection</p>
        <h1 className="mt-2 text-2xl font-semibold text-slate-900">탐지 업로드</h1>
        <p className="mt-2 text-sm text-slate-600">
          이미지 또는 비디오 파일을 업로드하여 이상 여부를 탐지하고 결과를 확인할 수 있습니다.
        </p>
        <p className="mt-3 text-sm font-medium text-slate-700">{inspection.statusMessage}</p>
        {settingMessage ? (
          <p className="mt-2 rounded-md border border-blue-200 bg-blue-50 px-3 py-2 text-sm text-blue-700">
            {settingMessage}
          </p>
        ) : null}
      </div>

      <div className="grid grid-cols-1 gap-5 xl:grid-cols-12">
        <FileUploadCard
          selectedFile={inspection.selectedFile}
          uploading={inspection.uploading}
          errorMessage={inspection.errorMessage}
          noticeMessage={inspection.noticeMessage}
          onFileSelect={inspection.setSelectedFile}
        />
        <PreviewCard selectedFile={inspection.selectedFile} uploadResult={inspection.uploadResult} />
        <InspectionRunCard
          selectedFile={inspection.selectedFile}
          targetOptions={inspection.targetOptions}
          thresholdOptions={inspection.thresholdOptions}
          selectedTargetId={inspection.selectedTargetId}
          selectedThresholdId={inspection.selectedThresholdId}
          selectedModel={inspection.selectedModel}
          loadingOptions={inspection.loadingOptions}
          uploading={inspection.uploading}
          onTargetChange={inspection.setSelectedTargetId}
          onThresholdChange={inspection.setSelectedThresholdId}
          onModelChange={inspection.setSelectedModel}
          onSubmit={inspection.submit}
          onSettingClick={() => setSettingMessage('설정 기능은 준비 중입니다.')}
        />
        <ProgressStatusCard
          selectedFile={inspection.selectedFile}
          uploading={inspection.uploading}
          uploadResult={inspection.uploadResult}
          requestDurationMs={inspection.requestDurationMs}
        />
        <ResultSummaryCard
          uploadResult={inspection.uploadResult}
          selectedThreshold={inspection.selectedThreshold}
          onGoResults={() => navigate('/results')}
          onReset={inspection.reset}
        />
        <AnalysisInsightCard
          uploadResult={inspection.uploadResult}
          onGoResults={() => navigate('/results')}
        />
      </div>
    </section>
  )
}
