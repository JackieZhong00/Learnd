package com.learnd.learnd_main.Learnd.model;

public class RenameRequest {
    String name;
    public RenameRequest () {
        this.name = null;
    }
    public RenameRequest(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setName(String newName) {
        this.name = newName;
    }
}
