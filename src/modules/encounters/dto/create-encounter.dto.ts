import { ApiProperty } from '@nestjs/swagger';
import { IsIn, IsNumber, IsObject, IsString } from 'class-validator';

export class CreateEncounterDto {
  @ApiProperty() @IsString() patientId!: string;

  @ApiProperty({ example: { lat: 36.6, lng: 127.4 } })
  @IsObject() location!: { lat: number; lng: number };

  @ApiProperty({ enum: ['cardiac', 'trauma', 'respiratory', 'stroke', 'other'] })
  @IsString()
  @IsIn(['cardiac', 'trauma', 'respiratory', 'stroke', 'other'])
  condition!: 'cardiac' | 'trauma' | 'respiratory' | 'stroke' | 'other';
}
