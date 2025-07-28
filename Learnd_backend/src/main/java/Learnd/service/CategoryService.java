package Learnd.service;

import Learnd.model.Category;
import Learnd.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import Learnd.repo.CategoryRepository;
import Learnd.repo.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {

        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public Category createCategory(Category category) {
        //check to see if category already exists for user in fb, if it does:
        // don't do anything and just return empty category
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByEmail(email);
        User userObject = user.orElseThrow(() -> new UsernameNotFoundException("User in SecContext no longer in db"));

        //check to see if category exists
        Optional<Category> fetchedCategory = categoryRepository.findByNameAndUser(category.getName(), userObject);

        //if category is present, just return
        if (fetchedCategory.isPresent()) {
            return new Category();
        }

        category.setUser(userObject);
        categoryRepository.save(category);
        return category;
    }

    public Optional<Category> updateCategory(String name) {
        Optional<Category> fetchedCategory = categoryRepository.findByName(name);
        if (fetchedCategory.isEmpty()) {
            return Optional.empty();
        }
        Category categoryObject = fetchedCategory.get();
        categoryObject.setName(name);
        categoryRepository.save(categoryObject);
        return Optional.of(categoryObject);
    }

    public List<Category> getCategoriesByPrefix(String prefix) {
        return categoryRepository.findByNameStartingWith(prefix);
    }

    public Optional<Category> getCategoryByName(String categoryName, String email) {
        return categoryRepository.findByNameAndUserEmail(categoryName, email);
    }

    public Optional<Category> updateCategoryParent(Category category) {
        Optional<Category> fetchedCategory = categoryRepository.findByName(category.getName());
        if (fetchedCategory.isEmpty()) {
            return Optional.empty();
        }
        Category categoryObject = fetchedCategory.get();
        categoryObject.setParent(category);
        categoryRepository.save(categoryObject);
        return Optional.of(categoryObject);
    }
}
