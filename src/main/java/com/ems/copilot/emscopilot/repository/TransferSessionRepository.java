package com.ems.copilot.emscopilot.repository;

import com.ems.copilot.emscopilot.domain.TransferSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransferSessionRepository extends JpaRepository<TransferSession, String> {

    /**
     * 세션 코드로 조회
     */
    Optional<TransferSession> findBySessionCode(String sessionCode);

    /**
     * 해당 연도의 최대 세션 번호 조회
     * 예: S2025-001 → 1, S2025-002 → 2, S2025-010 → 10
     *
     * @param year "2025" 형식의 연도
     * @return 최대 번호 (없으면 0)
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(s.sessionCode, 7) AS int)), 0) " +
            "FROM TransferSession s " +
            "WHERE s.sessionCode LIKE CONCAT('S', :year, '-%')")
    Integer findMaxSessionNumberByYear(@Param("year") String year);
}
