package com.blog.dao;

import com.blog.model.Category;
import java.util.List;

public interface CategoryDao {
    List<Category> findAll();
    Category findById(int id);
    boolean save(Category category);
    boolean update(Category category);
    boolean delete(int id);
}