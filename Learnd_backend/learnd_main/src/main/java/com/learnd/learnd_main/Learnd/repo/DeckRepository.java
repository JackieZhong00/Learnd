package com.learnd.learnd_main.Learnd.repo;

import com.learnd.learnd_main.Learnd.model.Deck;
import com.learnd.learnd_main.Learnd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeckRepository extends JpaRepository<Deck,Integer> {


    void deleteByName(String name);

    //optional is used bc there might be cases where no user is found
    Optional<Deck> findByName(String name);

    Deck findDeckByName(String name);

    List<Deck> findByUser_IdAndNameStartingWith(int id, String name);

    List<Deck> findAllByUser(User user);


    List<Deck> findByUser_IdAndCategory_Id(int userId, int categoryId);

    Optional<Integer> findIdByNameAndUser(String name, User user);

    void deleteById(int id);
}
