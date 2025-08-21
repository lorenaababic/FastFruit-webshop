package hr.java.web.webshop.controller;

import hr.java.web.webshop.model.Category;
import hr.java.web.webshop.model.Product;
import hr.java.web.webshop.service.CategoryService;
import hr.java.web.webshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("products")
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String getProducts(@RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "category", required = false) Long categoryId,
                              Model model) {
        List<Product> products;

        if (categoryId != null) {
            products = productService.findByCategoryId(categoryId);
        } else if (search != null && !search.trim().isEmpty()) {
            products = productService.searchByName(search);
            model.addAttribute("searchKeyword", search);
        } else {
            products = productService.findAll();
        }

        model.addAttribute("products", products);
        return "products/list";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "products/detail";
    }

    @GetMapping(params = "category")
    public String getProductsByCategory(@RequestParam("category") Long categoryId, Model model) {
        List<Product> products = productService.findByCategoryId(categoryId);
        model.addAttribute("products", products);
        return "products/list";
    }
}