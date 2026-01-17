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
                        "WHERE u.gender <> :gender AND u.lock = false " +
                        "AND u.id NOT IN :excludedIds " +
                        "ORDER BY ABS(u.compatibilityScore - :targetScore)")
        List<Users> findNearestCompatibleUsers(@Param("gender") String gender,
                        @Param("targetScore") double targetScore,
                        @Param("excludedIds") List<Integer> excludedIds);

        Users findByMail(String mail);
}