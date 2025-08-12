package com.learnd.learnd_main.Learnd.model;

public class DeckRenameRequest {
    String name;
    public DeckRenameRequest () {
        this.name = null;
    }
    public DeckRenameRequest(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setName(String newName) {
        this.name = newName;
    }
}
