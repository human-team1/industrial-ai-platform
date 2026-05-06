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
        <h1 className="mt-2 text-2xl font-semibold text-slate-900">이미지 업로드</h1>
        <p className="mt-3 text-sm font-medium text-slate-700">{inspection.statusMessage}</p>
        {settingMessage ? (
          <p className="mt-2 rounded-md border border-blue-200 bg-blue-50 px-3 py-2 text-sm text-blue-700">
            {settingMessage}
          </p>
        ) : null}
      </div>

      <div className="grid grid-cols-1 gap-5 xl:grid-cols-2">
        <div className="flex flex-col gap-5">
          <FileUploadCard
            selectedFile={inspection.selectedFile}
            uploading={inspection.uploading}
            errorMessage={inspection.errorMessage}
            noticeMessage={inspection.noticeMessage}
            onFileSelect={inspection.setSelectedFile}
          />
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
            onSettingClick={() =>
              setSettingMessage(
                '설정 기능은 준비 중입니다. 현재는 기본 ROI와 품질 게이트 설정으로 검사합니다.',
              )
            }
          />
        </div>
        <PreviewCard selectedFile={inspection.selectedFile} uploadResult={inspection.uploadResult} />
      </div>

      <div className="grid grid-cols-1 gap-5 xl:grid-cols-3">
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
