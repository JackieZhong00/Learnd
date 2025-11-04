package com.learnd.learnd_main.Learnd.service;

import com.learnd.learnd_main.Learnd.model.Category;
import com.learnd.learnd_main.Learnd.model.CategoryDTO;
import com.learnd.learnd_main.Learnd.model.User;
import com.learnd.learnd_main.Learnd.model.UserPrincipal;
import com.learnd.learnd_main.Learnd.repo.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.learnd.learnd_main.Learnd.repo.UserRepository;

import java.util.*;

@Service
public class CategoryService {

    @PersistenceContext
    private EntityManager entityManager;

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {

        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    private int getUserIdFromPrincipal() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userPrincipal.getUserId();
    }

    public Category createCategory(Category category) {
        int userId = getUserIdFromPrincipal();
        User userRef = entityManager.getReference(User.class, userId);
        category.setUser(userRef);
        return categoryRepository.save(category);
    }

    public Category updateName(String name, int categoryId) {
        int userId = getUserIdFromPrincipal();
        boolean categoryOk = categoryRepository.existsByIdAndUserId(categoryId, userId);
        if (!categoryOk) {
            throw new IllegalArgumentException("category not found for user");
        }
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
        int userId = getUserIdFromPrincipal();
        List<Category> result =  categoryRepository.findByUser_IdAndNameStartingWith(userId,prefix);
        List<CategoryDTO> categoryDTOList = new ArrayList<>();
        for (Category category : result) {
            categoryDTOList.add(new CategoryDTO(category));
        }
        return categoryDTOList;
    }

    public Optional<Category> getCategoryByName(String categoryName) {
        int userId = getUserIdFromPrincipal();
        return categoryRepository.findByNameAndUser_Id(categoryName, userId);
    }

    @Transactional
    public ResponseEntity<Void> updateCategoryParent(int categoryId, int parentId) {
        System.out.println("entered service method for updating category's parent \n");
        if (categoryId == parentId) {
            throw new IllegalArgumentException("a category's parent can not be itself");
        }
        int userId = getUserIdFromPrincipal();
        boolean childOk = categoryRepository.existsByIdAndUserId(categoryId, userId);
        boolean parentOk = categoryRepository.existsByIdAndUserId(parentId, userId);

        if (!parentOk || !childOk) {
            throw new IllegalArgumentException("categories do not belong to user");
        }

        Category parentRef = entityManager.getReference(Category.class, parentId);
        Category categoryRef = entityManager.getReference(Category.class, categoryId);
        if(isDescendant(categoryRef, parentRef)) {
            throw new IllegalArgumentException("Could not update category parent " +
                    "because cycle detected or categories are equal");
        }
        categoryRef.setParent(parentRef);
        categoryRepository.save(categoryRef);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<Void> deleteParentFromCategory(int categoryId) {
        int userId = getUserIdFromPrincipal();
        boolean childOk = categoryRepository.existsByIdAndUserId(categoryId, userId);
        if (!childOk) {
            throw new IllegalArgumentException("category does not belong to user");
        }
        Category categoryRef = entityManager.getReference(Category.class, categoryId);
        categoryRef.setParent(null);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<Void> deleteCategory(int categoryId) {
        int userId = getUserIdFromPrincipal();
        boolean categoryOk = categoryRepository.existsByIdAndUserId(categoryId, userId);
        if (!categoryOk) {
            throw new IllegalArgumentException("category does not belong to user");
        }
        Category categoryRef = entityManager.getReference(Category.class, categoryId);
        categoryRepository.delete(categoryRef);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public List<CategoryDTO> getTree() {
        //get user id to fetch for all categories associated with user
        int id = getUserIdFromPrincipal();
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
