package hr.java.web.webshop.service;

import hr.java.web.webshop.dto.CategoryDto;
import hr.java.web.webshop.model.Category;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CategoryService {
    void saveCategoryWithImage(Category category, MultipartFile file) throws IOException;
    List<Category> findAll();
    Category findById(Long id);
    Category save(Category category);
    Category update(Long id, CategoryDto categoryDto);
    void deleteById(Long id);
    List<Category> searchByName(String name);
}
