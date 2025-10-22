package com.ems.copilot.emscopilot.repository;

import com.ems.copilot.emscopilot.domain.PatientVitalData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Redis 기반 환자 바이탈 정보 저장소
 *
 * CrudRepository를 상속받아 기본 CRUD 기능 제공:
 * - save(entity): 저장/업데이트
 * - findById(id): 조회
 * - existsById(id): 존재 여부 확인
 * - deleteById(id): 삭제
 * - count(): 전체 개수
 *
 * TTL(Time To Live) 30분 후 자동 삭제됨 (PatientVitalData의 @RedisHash 설정)
 */
@Repository
public interface PatientVitalRepository extends CrudRepository<PatientVitalData, String> {

    // 기본 메서드만으로 충분
    // sessionId가 @Id이므로 findById(sessionId)로 조회 가능

}
