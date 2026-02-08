package com.example.dateon.Repositories;

import com.example.dateon.Models.IceBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IceBreakerRepository extends JpaRepository<IceBreaker, Long> {

    @Query(value = "SELECT * FROM icebreakers ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<IceBreaker> findRandomIceBreakers(int limit);
}
