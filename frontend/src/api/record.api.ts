import api from './axios'
import type { RecordDetail } from '../types/record'

export async function fetchRecordDetail(recordNum: number | string): Promise<RecordDetail> {
  const response = await api.get<RecordDetail>(`/api/records/${recordNum}`)
  return response.data
}
