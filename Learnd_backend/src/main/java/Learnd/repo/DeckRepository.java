package Learnd.repo;

import Learnd.model.Deck;
import Learnd.model.User;
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

    List<Deck> findByNameStartingWith(String name);

    List<Deck> findAllByUser(User user);


    Optional<Integer> findIdByNameAndUser(String name, User user);

    void deleteById(int id);
}
