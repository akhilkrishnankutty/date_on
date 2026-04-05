package com.example.dateon.Repo;

import com.example.dateon.Models.UserForgotPass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserForgotPassRepo extends JpaRepository<UserForgotPass, Integer> {
    List<UserForgotPass> findByMail(String mail);
}
