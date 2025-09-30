package com.learnd.learnd_main.Learnd.model;

import java.util.ArrayList;
import java.util.List;

public class CategoryDTO {
    private int id;
    private String name;
    private List<CategoryDTO> children;

    public CategoryDTO(int id, String name) {
        this.id = id;
        this.name = name;
        this.children = new ArrayList<>();
    }

    public CategoryDTO (Category category) {
        this.id = category.getId();
        this.name = category.getName();
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public List<CategoryDTO> getChildren() {
        return children;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChildren(List<CategoryDTO> children) {
        this.children = children;
    }
    public void addChild(CategoryDTO category) {
        this.children.add(category);
    }
 }
