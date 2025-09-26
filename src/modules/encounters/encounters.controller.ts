import { Body, Controller, Post } from '@nestjs/common';
import { CreateEncounterDto } from './dto/create-encounter.dto';
import { EncountersService } from './encounters.service';
import { AuditService } from '../audit/audit.service';

@Controller('/api/encounters')
export class EncountersController {
  constructor(
    private readonly svc: EncountersService,
    private readonly audit: AuditService
  ) {}

  @Post()
  async create(@Body() body: CreateEncounterDto) {
    const res = this.svc.create();
    await this.audit.log({
      userId: 'system',
      action: 'WRITE',
      resource: 'encounter',
      details: { ...body, encounterId: res.encounterId }
    });
    return res;
  }
}
