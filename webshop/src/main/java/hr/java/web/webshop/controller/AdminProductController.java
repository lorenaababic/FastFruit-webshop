package hr.java.web.webshop.controller;

import hr.java.web.webshop.model.Product;
import hr.java.web.webshop.service.CategoryService;
import hr.java.web.webshop.service.ProductService;
import hr.java.web.webshop.service.FileStorageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/products/list";
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        return "admin/products/form";
    }

    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult result,
                              @RequestParam(value = "productImage", required = false) MultipartFile file,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/products/form";
        }

        try {
            if (product.getId() == null) {
                if (file != null && !file.isEmpty()) {
                    productService.saveProductWithImage(product, file);
                } else {
                    productService.save(product);
                }
                redirectAttributes.addFlashAttribute("message",
                        "Novi proizvod '" + product.getName() + "' je uspješno dodan");
            } else {
                Product existingProduct = productService.findById(product.getId());

                product.setCreatedAt(existingProduct.getCreatedAt());
                product.setUpdatedAt(LocalDateTime.now());

                if (file != null && !file.isEmpty()) {
                    productService.saveProductWithImage(product, file);
                } else {
                    if (existingProduct.getImagePath() != null) {
                        product.setImagePath(existingProduct.getImagePath());
                    }
                    productService.save(product);
                }
                redirectAttributes.addFlashAttribute("message",
                        "Proizvod '" + product.getName() + "' je uspješno ažuriran");
            }

        } catch (IOException e) {
            logger.error("Error uploading image for product: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Greška prilikom upload-a slike: " + e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "admin/products/form";
        } catch (Exception e) {
            logger.error("Error saving product: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Greška prilikom spremanja proizvoda: " + e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "admin/products/form";
        }

        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findById(id);
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.findAll());
            return "admin/products/form";
        } catch (Exception e) {
            logger.error("Product not found with ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Proizvod s ID " + id + " nije pronađen");
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("product") Product product,
                                BindingResult result,
                                @RequestParam(value = "productImage", required = false) MultipartFile file,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/products/form";
        }

        try {
            product.setId(id);

            Product existingProduct = productService.findById(id);
            product.setCreatedAt(existingProduct.getCreatedAt());
            product.setUpdatedAt(LocalDateTime.now());

            if (file != null && !file.isEmpty()) {
                productService.saveProductWithImage(product, file);
            } else {
                if (existingProduct.getImagePath() != null) {
                    product.setImagePath(existingProduct.getImagePath());
                }
                productService.save(product);
            }

            redirectAttributes.addFlashAttribute("message",
                    "Proizvod '" + product.getName() + "' je uspješno ažuriran");
        } catch (IOException e) {
            logger.error("Error uploading image for product {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Greška prilikom upload-a slike: " + e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "admin/products/form";
        } catch (Exception e) {
            logger.error("Error updating product {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Greška prilikom ažuriranja proizvoda: " + e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "admin/products/form";
        }

        return "redirect:/admin/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findById(id);
            String productName = product.getName();

            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                try {
                    fileStorageService.deleteFile(product.getImagePath());
                } catch (Exception e) {
                    logger.warn("Error deleting image file {}: {}", product.getImagePath(), e.getMessage());
                }
            }

            productService.deleteById(id);
            redirectAttributes.addFlashAttribute("message",
                    "Proizvod '" + productName + "' je uspješno obrisan");

        } catch (Exception e) {
            String errorMessage;

            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                errorMessage = "Proizvod nije pronađen ili je već obrisan.";
            } else {
                errorMessage = "Greška prilikom brisanja proizvoda. Molimo pokušajte ponovo.";
            }

            redirectAttributes.addFlashAttribute("error", errorMessage);
            logger.error("Error deleting product with ID {}: {}", id, e.getMessage(), e);
        }
        return "redirect:/admin/products";
    }
}