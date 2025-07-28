package Learnd.repo;

import Learnd.model.MultipleChoiceCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MultipleChoiceCardRepository extends JpaRepository<MultipleChoiceCard, Integer> {

    MultipleChoiceCard findById(int id);
}
