package hr.java.web.webshop.service.impl;

import hr.java.web.webshop.dto.CategoryDto;
import hr.java.web.webshop.model.Category;
import hr.java.web.webshop.repository.CategoryRepository;
import hr.java.web.webshop.service.CategoryService;
import hr.java.web.webshop.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private final FileStorageService fileStorageService;

    @Override
    public void saveCategoryWithImage(Category category, MultipartFile file) throws IOException {
        String imagePath = fileStorageService.storeFile(file);

        category.setImagePath(imagePath);

        categoryRepository.save(category);
    }

    private CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, FileStorageService fileStorageService) {
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
    }


    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public Category save(Category category) {
        category.setName(category.getName());
        category.setDescription(category.getDescription());
        category.setImagePath(category.getImagePath());
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    @Override
    public Category update(Long id, CategoryDto categoryDto) {
        Category category = findById(id);
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        category.setImagePath(categoryDto.getImagePath());
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    @Override
    public void deleteById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public List<Category> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return findAll();
        }
        return categoryRepository.findByNameContainingIgnoreCase(name.trim());
    }
}
