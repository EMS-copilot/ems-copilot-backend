import { Body, Controller, Post } from '@nestjs/common';
import { RecommendDto } from './dto/recommend.dto';
import { RecommendService } from './recommend.service';
import { AuditService } from '../audit/audit.service';

@Controller('/api/recommend')
export class RecommendController {
  constructor(
    private readonly svc: RecommendService,
    private readonly audit: AuditService
  ) {}

  @Post()
  async post(@Body() body: RecommendDto) {
    const now = new Date().toISOString();
    const { policy, recommendations } = this.svc.recommend(body);

    await this.audit.log({
      userId: 'system',
      action: 'READ',
      resource: 'recommendation',
      details: { encounterId: body.encounterId, urgency: body.urgency, recommendationCount: recommendations.length }
    });

    return {
      encounterId: body.encounterId,
      timestamp: now,
      policy,
      recommendations
    };
  }
}
