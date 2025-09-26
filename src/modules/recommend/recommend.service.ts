import { Injectable } from '@nestjs/common';
import { mockHospitals } from '../hospitals/hospitals.mock';

@Injectable()
export class RecommendService {
  recommend(body: { urgency: 'critical' | 'urgent' | 'normal'; location: { lat: number; lng: number } }) {
    const policy = {
      highRisk: body.urgency === 'critical',
      rejectAllowed: body.urgency !== 'critical'
    };

    const recommendations = mockHospitals
      .map((h, idx) => {
        const distanceFactor =
          Math.abs(body.location.lat - h.location.lat) + Math.abs(body.location.lng - h.location.lng);
        const urgencyMultiplier =
          body.urgency === 'critical' ? 0.8 : body.urgency === 'urgent' ? 1.0 : 1.2;
        const eta = Math.round(h.baseEta * (1 + distanceFactor * 10) * urgencyMultiplier);

        const acceptProbability = Math.round(h.capacity * 100 * (body.urgency === 'critical' ? 1.2 : 1.0));
        const doorToTreatment = Math.round(15 + Math.random() * 30);

        const reasons: string[] = [];
        if (h.capacity > 0.7) reasons.push('높은 가용 병상');
        if (eta < 15) reasons.push('빠른 도착 가능');
        if (acceptProbability > 80) reasons.push('높은 수용 확률');
        if (body.urgency === 'critical' && idx === 0) reasons.push('중증 환자 우선 배치');

        return {
          rank: 0,
          hospitalId: h.hospitalId,
          name: h.name,
          eta,
          acceptProbability: Math.min(acceptProbability, 100),
          doorToTreatment,
          reasons: reasons.length ? reasons : ['표준 추천']
        };
      })
      .sort((a, b) => (body.urgency === 'critical' ? a.eta - b.eta : b.acceptProbability - a.acceptProbability))
      .map((r, i) => ({ ...r, rank: i + 1 }));

    return { policy, recommendations };
  }
}
