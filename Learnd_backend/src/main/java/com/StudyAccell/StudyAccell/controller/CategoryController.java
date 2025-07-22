package com.StudyAccell.StudyAccell.controller;

import com.StudyAccell.StudyAccell.model.Category;
import com.StudyAccell.StudyAccell.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/category")
public class CategoryController {

    public final CategoryService categoryService;

    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    @PostMapping("/createCategory")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(categoryService.createCategory(category));
    }

    @PatchMapping("/update_category_name") //not sure if param should be String or Category type
    public Optional<Category> updateCategory(@RequestBody String name) {
        return categoryService.updateCategory(name);
    }

    @PatchMapping("/update_category_parent")
    public Optional<Category> updateCategoryParent(@RequestBody Category category) {
        return categoryService.updateCategoryParent(category);
    }

    @GetMapping("/getCategoriesByPrefix/{prefix}")
    public List<Category> getAllCategoryNamesWithPrefix(@PathVariable String prefix) {
        return categoryService.getCategoriesByPrefix(prefix);
    }


    @GetMapping("/getCategoryByName/{name}")
    public ResponseEntity<Category> getCategoryByName(@PathVariable String name, Authentication auth) {
        Optional<Category> category = categoryService.getCategoryByName(name, auth.getName());
        return category.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
