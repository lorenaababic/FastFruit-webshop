package hr.java.web.webshop.service;

import hr.java.web.webshop.dto.ProductDto;
import hr.java.web.webshop.model.Product;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    void saveProductWithImage(Product product, MultipartFile image) throws IOException;
    List<Product> findAll();
    List<Product> findByCategoryId(Long categoryId);
    Product findById(Long id);
    Product save(Product product);
    Product update(Long id, ProductDto productDto);
    void deleteById(Long id);
    List<Product> searchByName(String name);
}
