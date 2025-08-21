package hr.java.web.webshop.controller;

import hr.java.web.webshop.model.Category;
import hr.java.web.webshop.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories/list";  // ← ISPRAVKA: categories umesto category
    }

    @GetMapping("/new")
    public String newCategoryForm(Model model) {
        System.out.println("=== DEBUG: newCategoryForm called ===");
        model.addAttribute("category", new Category());
        System.out.println("=== DEBUG: Returning admin/categories/form ===");
        return "admin/categories/form";  // ← ISPRAVKA: categories umesto category
    }

    @PostMapping("/save")
    public String saveCategory(@Valid @ModelAttribute("category") Category category,
                               BindingResult result,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if (result.hasErrors()) {
            return "admin/categories/form";
        }

        try {
            if (category.getId() == null) {
                if (imageFile != null && !imageFile.isEmpty()) {
                    categoryService.saveCategoryWithImage(category, imageFile);
                } else {
                    categoryService.save(category);
                }
                redirectAttributes.addFlashAttribute("message", "Nova kategorija je uspješno dodana");
            } else {
                if (imageFile != null && !imageFile.isEmpty()) {
                    categoryService.saveCategoryWithImage(category, imageFile);
                } else {
                    categoryService.save(category);
                }
                redirectAttributes.addFlashAttribute("message", "Kategorija je uspješno ažurirana");
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Greška prilikom upload-a slike: " + e.getMessage());
            model.addAttribute("category", category);
            return "admin/categories/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Greška prilikom spremanja kategorije: " + e.getMessage());
            model.addAttribute("category", category);
            return "admin/categories/form";
        }

        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        try {
            Category category = categoryService.findById(id);
            model.addAttribute("category", category);
            return "admin/categories/form";
        } catch (Exception e) {
            model.addAttribute("error", "Kategorija nije pronađena");
            return "redirect:/admin/categories";
        }
    }

    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute("category") Category category,
                                 BindingResult result,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        if (result.hasErrors()) {
            return "admin/categories/form";
        }

        try {
            category.setId(id);

            if (imageFile != null && !imageFile.isEmpty()) {
                categoryService.saveCategoryWithImage(category, imageFile);
            } else {
                categoryService.save(category);
            }

            redirectAttributes.addFlashAttribute("message", "Kategorija je uspješno ažurirana");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Greška prilikom upload-a slike: " + e.getMessage());
            model.addAttribute("category", category);
            return "admin/categories/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Greška prilikom ažuriranja kategorije: " + e.getMessage());
            model.addAttribute("category", category);
            return "admin/categories/form";
        }

        return "redirect:/admin/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Kategorija je uspješno obrisana");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Greška prilikom brisanja kategorije ");
        }
        return "redirect:/admin/categories";
    }
}