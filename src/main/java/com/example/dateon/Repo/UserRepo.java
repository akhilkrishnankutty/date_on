package com.example.dateon.Repo;

import com.example.dateon.Models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  UserRepo extends JpaRepository<Users,Integer> {

}
