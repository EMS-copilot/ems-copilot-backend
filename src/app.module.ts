import { Module } from '@nestjs/common';
import { HealthController } from './modules/health/health.controller';
import { AuditService } from './modules/audit/audit.service';
import { EncountersController } from './modules/encounters/encounters.controller';
import { EncountersService } from './modules/encounters/encounters.service';
import { RecommendController } from './modules/recommend/recommend.controller';
import { RecommendService } from './modules/recommend/recommend.service';

@Module({
  imports: [],
  controllers: [HealthController, EncountersController, RecommendController],
  providers: [AuditService, EncountersService, RecommendService],
})
export class AppModule {}
