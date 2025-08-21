package hr.java.web.webshop.service.impl;

import hr.java.web.webshop.dto.ProductDto;
import hr.java.web.webshop.model.Category;
import hr.java.web.webshop.model.Product;
import hr.java.web.webshop.repository.CategoryRepository;
import hr.java.web.webshop.repository.ProductRepository;
import hr.java.web.webshop.service.FileStorageService;
import hr.java.web.webshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Pageable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private final FileStorageService fileStorageService;

    @Override
    public void saveProductWithImage(Product product, MultipartFile image) throws IOException {
        String imagePath = fileStorageService.storeFile(image);
        product.setImagePath(imagePath);
        productRepository.save(product);
    }

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public Product save(Product product) {
        Category category = categoryRepository.findById(product.getCategory().getId()).orElseThrow(() -> new RuntimeException("Category not found"));

        product.setName(product.getName());
        product.setDescription(product.getDescription());
        product.setPrice(product.getPrice());
        product.setImagePath(product.getImagePath());
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    @Override
    public Product update(Long id, ProductDto productDto) {
        Product product = findById(id);
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setImagePath(productDto.getImagePath());
        product.setCategory(category);
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    @Override
    public void deleteById(Long id) {
        if(!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }

    @Override
    public List<Product> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return findAll();
        }
        return productRepository.findByNameContainingIgnoreCase(name.trim());
    }
}
