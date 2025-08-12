package com.learnd.learnd_main.Learnd.repo;

import com.learnd.learnd_main.Learnd.model.Category;
import com.learnd.learnd_main.Learnd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Integer> {
    public Optional<Category> findByName(String name);
    public Optional<Category> findById(int id);

    public void deleteByName(String name);
    public void deleteById(int id);

    List<Category> findByNameStartingWith(String name);


    Optional<Category> findByNameAndUserEmail(String name, String email);

    Optional<Category> findByNameAndUser(String name, User user);
}
