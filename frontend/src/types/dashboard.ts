/*
 * GET /api/dashboard의 recentTimeline은 백엔드 TimelineItem DTO를 그대로 내려준다.
 * Day 5에서 Timeline 화면 전용 타입(TS Discriminated Union)을 만들 때
 * 이 타입을 그대로 가져가거나 확장해서 쓰면 된다.
 */
export interface DashboardTimelineItem {
  type: 'GOAL' | 'RECORD'
  itemNum: number
  title: string
  content: string
  createdAt: string
  detailUrl: string
}

export interface DashboardSummary {
  activeGoalCount: number
  recordsThisMonth: number
  streakDays: number
  recentTimeline: DashboardTimelineItem[]
}
