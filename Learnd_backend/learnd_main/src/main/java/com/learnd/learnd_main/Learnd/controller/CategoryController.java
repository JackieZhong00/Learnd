package com.learnd.learnd_main.Learnd.controller;

import com.learnd.learnd_main.Learnd.model.Category;
import com.learnd.learnd_main.Learnd.model.CategoryDTO;
import com.learnd.learnd_main.Learnd.model.RenameRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.learnd.learnd_main.Learnd.service.CategoryService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/category")
public class CategoryController {

    public final CategoryService categoryService;

    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(categoryService.createCategory(category));
    }

    @PatchMapping("/update_name/{categoryId}") //not sure if param should be String or Category type
    public Category updateName(@RequestBody RenameRequest req, @PathVariable int categoryId) {
        return categoryService.updateName(req.getName(), categoryId);
    }



    @PatchMapping("/{categoryId}/{parentId}/update_parent")
    public ResponseEntity<Void> updateCategoryParent(@PathVariable int categoryId, @PathVariable int parentId) {
        return categoryService.updateCategoryParent(categoryId, parentId);
    }

    @GetMapping("/searchPrefix/{prefix}")
    public List<CategoryDTO> getAllCategoryNamesWithPrefix(@PathVariable String prefix) {
        return categoryService.getCategoriesByPrefix(prefix);
    }


    @GetMapping("/getCategoryByName/{name}")
    public ResponseEntity<Category> getCategoryByName(@PathVariable String name) {
        Optional<Category> category = categoryService.getCategoryByName(name);
        return category.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/getTree")
    public List<CategoryDTO> getTree() {
        //service method takes care of building a user specific tree to return
        return categoryService.getTree();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable int id) {
        return categoryService.deleteCategory(id);
    }

}
