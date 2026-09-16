/*
 * Day 13 — Record Detail(GET /api/records/{id})이 사용하는 응답 타입.
 * 백엔드 GrowthRecordResponse와 1:1로 대응한다. Record Create/Edit은
 * 이번 Day 범위가 아니므로 요청(Request) 타입은 만들지 않는다.
 */
export interface RecordGoalSummary {
  goalNum: number
  goalTitle: string
}

export type MediaType = 'IMAGE' | 'YOUTUBE'

export interface RecordMedia {
  mediaNum: number
  mediaType: MediaType
  mediaUrl: string
  sortOrder: number
}

export interface RecordDetail {
  recordNum: number
  title: string
  content: string
  todayLearning: string | null
  difficulty: string | null
  solution: string | null
  retrospective: string | null
  createdAt: string
  goal: RecordGoalSummary | null
  mediaList: RecordMedia[]
}
