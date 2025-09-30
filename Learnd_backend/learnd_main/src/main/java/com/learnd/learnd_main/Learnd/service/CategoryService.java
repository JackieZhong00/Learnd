package com.learnd.learnd_main.Learnd.service;

import com.learnd.learnd_main.Learnd.model.Category;
import com.learnd.learnd_main.Learnd.model.CategoryDTO;
import com.learnd.learnd_main.Learnd.model.User;
import com.learnd.learnd_main.Learnd.repo.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.learnd.learnd_main.Learnd.repo.UserRepository;

import java.util.*;

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
        User user = userRepository.findByEmail(email).orElseThrow();

        //check to see if category exists - you want this optional to be empty
        Optional<Category> fetchedCategory = categoryRepository.findByNameAndUser_Id(category.getName(), user.getId());
        if (fetchedCategory.isEmpty()){
            category.setUser(user);
            categoryRepository.save(category);
            return category;
        }
        throw new IllegalArgumentException("a category with this name already exists");

    }

    public Category updateName(String name, int categoryId) {
        Optional<Category> fetchedCategory = categoryRepository.findById(categoryId);
        if (fetchedCategory.isEmpty()) {
            throw new IllegalArgumentException("a category with this id does not exist");
        }
        Category category = fetchedCategory.get();
        category.setName(name);
        categoryRepository.save(category);
        return category;
    }

    public List<CategoryDTO> getCategoriesByPrefix(String prefix) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        int userId = userRepository.findByEmail(email).orElseThrow().getId();
        List<Category> result =  categoryRepository.findByUser_IdAndNameStartingWith(userId,prefix);
        List<CategoryDTO> categoryDTOList = new ArrayList<>();
        for (Category category : result) {
            categoryDTOList.add(new CategoryDTO(category));
        }
        return categoryDTOList;
    }

    public Optional<Category> getCategoryByName(String categoryName) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        int userId = user.getId();
        return categoryRepository.findByNameAndUser_Id(categoryName, userId);
    }

    @Transactional
    public ResponseEntity<Void> updateCategoryParent(int categoryId, int parentId) {
        System.out.println("entered service method for updating category's parent \n");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("User in SecContext no longer in db"));
        Category fetchedParent = categoryRepository.findByIdAndUser_Id(parentId, user.getId()).orElseThrow(
                () -> new IllegalArgumentException("parent category with this id does not exist")
        );
        Category fetchedCategory = categoryRepository.findByIdAndUser_Id(categoryId, user.getId())
                .orElseThrow( () -> new IllegalArgumentException("could not find current category with id"));
        if(fetchedParent.getId() == fetchedCategory.getId() || isDescendant(fetchedCategory, fetchedParent)) {
            throw new IllegalArgumentException("Could not update category parent " +
                    "because cycle detected or categories are equal");
        }
        fetchedCategory.setParent(fetchedParent);
        categoryRepository.save(fetchedCategory);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<Void> deleteCategory(int categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        categoryRepository.delete(category);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public List<CategoryDTO> getTree() {
        //get user id to fetch for all categories associated with user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        int id = userRepository.findByEmail(email).orElseThrow().getId();
        List<Category> categories = categoryRepository.findByUser_Id(id);

        //create map where key = categoryId, value = categoryDTO representing the category
        Map<Integer, CategoryDTO> categoryMap = new HashMap<>();
        for (Category c : categories) {
            categoryMap.put(c.getId(),new CategoryDTO(c.getId(), c.getName()));
        }

        //find category roots to return
        List<CategoryDTO> roots = new ArrayList<>();
        for (Category c : categories) {
            if (c.getParent() == null) {
                roots.add(categoryMap.get(c.getId()));
                continue;
            }
            //get parentDTO of current category and add current category dto to parentDTOs children list
            CategoryDTO parentDTO = categoryMap.get(c.getParent().getId());
            parentDTO.addChild(categoryMap.get(c.getId()));

        }

        return roots;
    }

    private boolean isDescendant(Category potentialChild, Category potentialParent){

        while (potentialParent != null && potentialParent.getParent() != null) {
            potentialParent = potentialParent.getParent();
            if (potentialParent.getId() == potentialChild.getId()) {
                return true;
            }
        }
        return false;

    }
}
