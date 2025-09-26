import { Injectable } from '@nestjs/common';

export type AuditAction = 'READ' | 'WRITE' | 'DELETE' | 'ADMIN';

export interface AuditLog {
  userId: string;
  action: AuditAction;
  resource: string;
  timestamp: string;
  details?: any;
}

@Injectable()
export class AuditService {
  private logs: AuditLog[] = [];

  async log(event: Omit<AuditLog, 'timestamp'>) {
    const log: AuditLog = { ...event, timestamp: new Date().toISOString() };
    this.logs.push(log);
    // 실제 운영: DB/WORM/ES로 전송
    // console.log('[AUDIT]', JSON.stringify(log));
  }

  list() {
    return [...this.logs].sort((a, b) => b.timestamp.localeCompare(a.timestamp));
  }
}
