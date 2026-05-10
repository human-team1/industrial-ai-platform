import { useEffect, useState } from 'react'
import type { SystemStatus } from '../../../entities/operation/model/types'
import type { LineChartViewModel, TimeSeriesPoint } from '../../../shared/lib/chartViewModel'
import { compactNumber } from '../../../shared/lib/chartViewModel'

type Options = {
  systemStatus: SystemStatus | null
  maxPoints?: number
}

export type OperationMetricHistory = {
  resourceUsageChart: LineChartViewModel
  responseTimeChart: LineChartViewModel
}

export function useOperationMetricHistory({ systemStatus, maxPoints = 60 }: Options): OperationMetricHistory {
  const [points, setPoints] = useState<TimeSeriesPoint[]>([])

  useEffect(() => {
    if (!systemStatus) return

    const receivedAt = new Date()
    const timestamp = receivedAt.toISOString()
    const sourceCreatedAt = systemStatus.createdAt ?? null

    setPoints((prev) => {
      if (prev.some((point) => point.timestamp === timestamp || (sourceCreatedAt != null && point.sourceCreatedAt === sourceCreatedAt))) {
        return prev
      }

      const next: TimeSeriesPoint = {
        timestamp,
        sourceCreatedAt,
        time: formatTime(receivedAt),
        cpuUsage: compactNumber(systemStatus.cpuUsage),
        memoryUsage: compactNumber(systemStatus.memoryUsage),
        diskUsage: compactNumber(systemStatus.diskUsage),
        responseTimeMs: compactNumber(systemStatus.responseTimeMs),
      }
      return [...prev, next].slice(-maxPoints)
    })
  }, [maxPoints, systemStatus])

  const responseMax = Math.max(...points.map((point) => point.responseTimeMs).filter((value): value is number => typeof value === 'number'), 0)

  return {
    resourceUsageChart: {
      title: '리소스 사용률 추이',
      xKey: 'time',
      caption: `최근 ${maxPoints}회 polling`,
      yMax: 100,
      series: [
        { key: 'cpuUsage', label: 'CPU', unit: '%' },
        { key: 'memoryUsage', label: 'Memory', unit: '%' },
        { key: 'diskUsage', label: 'Disk', unit: '%' },
      ],
      points,
    },
    responseTimeChart: {
      title: 'API 응답시간 추이',
      xKey: 'time',
      caption: `최근 ${maxPoints}회 polling`,
      yMax: Math.max(10, Math.ceil(responseMax * 1.2)),
      series: [
        { key: 'responseTimeMs', label: 'API 응답시간', unit: 'ms' },
      ],
      points,
    },
  }
}

function formatTime(date: Date) {
  return date.toLocaleTimeString('ko-KR', {
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}
