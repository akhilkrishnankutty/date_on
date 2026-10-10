package com.example.dateon.Repo;

import com.example.dateon.Models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<Users, Integer> {

        @Query("SELECT u FROM Users u " +
                        "WHERE u.gender <> :gender AND u.lock = false AND u.isPaused = false " +
                        "AND u.id NOT IN :excludedIds " +
                        "ORDER BY ABS(u.compatibilityScore - :targetScore)")
        List<Users> findNearestCompatibleUsers(@Param("gender") String gender,
                        @Param("targetScore") double targetScore,
                        @Param("excludedIds") List<Integer> excludedIds);

        @Query("SELECT u FROM Users u " +
                        "WHERE u.gender <> :gender " +
                        "AND u.lock = false " +
                        "AND u.isPaused = false " +
                        "AND u.status IN ('WAITING_FOR_MATCH', 'REGISTERED', 'MATCH_FINDING') " +
                        "AND u.id NOT IN :excludedIds " +
                        "ORDER BY " +
                        "  CASE WHEN u.location IS NOT NULL AND :location IS NOT NULL AND LOWER(TRIM(u.location)) = LOWER(TRIM(:location)) THEN 0 ELSE 1 END ASC, " +
                        "  ABS(u.age - :age) ASC")
        List<Users> findOptimalCandidates(
                        @Param("gender") String gender,
                        @Param("location") String location,
                        @Param("age") int age,
                        @Param("excludedIds") List<Integer> excludedIds,
                        org.springframework.data.domain.Pageable pageable);

        @Query("SELECT u FROM Users u " +
                        "WHERE u.gender <> :gender " +
                        "AND u.lock = false " +
                        "AND u.isPaused = false " +
                        "AND u.status IN ('WAITING_FOR_MATCH', 'REGISTERED', 'MATCH_FINDING') " +
                        "AND u.id NOT IN :excludedIds " +
                        "ORDER BY ABS(u.age - :age) ASC")
        List<Users> findBroadCandidates(
                        @Param("gender") String gender,
                        @Param("age") int age,
                        @Param("excludedIds") List<Integer> excludedIds,
                        org.springframework.data.domain.Pageable pageable);

        Users findByMail(String mail);

        Users findByNumber(double number);

        List<Users> findByStatusAndIsPausedFalse(String status);
}