export type DashboardDecision = 'NORMAL' | 'DEFECT' | 'RECHECK' | string

export type DashboardOverview = {
  period: {
    startDate: string
    endDate: string
  }
  kpis: {
    totalInspectionCount: number
    totalInspectionChangeRate: number
    anomalyCount: number
    anomalyChangeRate: number
    normalCount: number
    normalChangeRate: number
    anomalyRate: number
    anomalyRateChangePoint: number
  }
  trend: DashboardTrendPoint[]
  topEquipmentAnomalyRates: TopEquipmentAnomalyRate[]
  recentResults: RecentDashboardResult[]
  recentNotifications: RecentDashboardNotification[]
  summary: {
    registeredTargetCount: number
    activeModelCount: number
    totalDataSizeBytes: number
    latestTrainingDate?: string | null
  }
  systemStatus: {
    overallStatus: string
    modelServerStatus: string
    streamServerStatus: string
    storageStatus: string
    lastUpdatedAt: string
  }
}

export type DashboardTrendPoint = {
  date: string
  normalCount: number
  anomalyCount: number
  recheckCount: number
  anomalyRate: number
}

export type TopEquipmentAnomalyRate = {
  targetId?: number | null
  equipmentName: string
  inspectionCount: number
  anomalyCount: number
  anomalyRate: number
}

export type RecentDashboardResult = {
  resultId: number
  inspectionId: number
  inspectedAt: string
  equipmentName: string
  inspectionType: string
  decision: DashboardDecision
  decisionLabel?: string | null
  anomalyScore?: number | null
  locationName?: string | null
}

export type RecentDashboardNotification = {
  notificationId: number
  severity: string
  title: string
  createdAt: string
  targetUrl?: string | null
}
