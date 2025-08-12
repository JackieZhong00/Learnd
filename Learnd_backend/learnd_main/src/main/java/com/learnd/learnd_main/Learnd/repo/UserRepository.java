package com.learnd.learnd_main.Learnd.repo;

import com.learnd.learnd_main.Learnd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {


    void deleteByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}
