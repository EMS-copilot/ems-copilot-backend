import { Injectable } from '@nestjs/common';

@Injectable()
export class EncountersService {
  create() {
    const encounterId =
      typeof crypto !== 'undefined' && 'randomUUID' in crypto
        ? crypto.randomUUID()
        : `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
    return {
      encounterId,
      timestamp: new Date().toISOString(),
      message: '환자 접촉 기록이 성공적으로 생성되었습니다'
    };
  }
}
