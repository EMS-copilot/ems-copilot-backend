import { ApiProperty } from '@nestjs/swagger';
import { IsIn, IsObject, IsString } from 'class-validator';

export class RecommendDto {
  @ApiProperty() @IsString() encounterId!: string;

  @ApiProperty({ enum: ['critical', 'urgent', 'normal'] })
  @IsString()
  @IsIn(['critical', 'urgent', 'normal'])
  urgency!: 'critical' | 'urgent' | 'normal';

  @ApiProperty({ example: { lat: 36.6, lng: 127.4 } })
  @IsObject() location!: { lat: number; lng: number };
}
