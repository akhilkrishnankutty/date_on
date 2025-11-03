package com.example.dateon.Repo;

import com.example.dateon.Models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface  UserRepo extends JpaRepository<Users,Integer> {

    @Query("SELECT u FROM Users u WHERE u.gender = :gender AND u.lock = false ORDER BY ABS(u.compatibilityScore - :targetScore)")
    Users findNearestCompatibleUser(@Param("gender") String gender, @Param("targetScore") double targetScore);

}
