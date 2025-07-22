package com.StudyAccell.StudyAccell;

import com.StudyAccell.StudyAccell.model.User;
import com.StudyAccell.StudyAccell.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByEmail() {
        String email = "test@gmail.com"; // Replace with a real email in your database
        Optional<User> user = userRepository.findByEmail(email);
        assert(user.isPresent());
    }
}
