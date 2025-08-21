package hr.java.web.webshop.controller;

import hr.java.web.webshop.dto.CategoryDto;
import hr.java.web.webshop.model.Category;
import hr.java.web.webshop.service.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String getCategories(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Category> categories;

        if (search != null && !search.trim().isEmpty()) {
            categories = categoryService.searchByName(search);
            model.addAttribute("searchKeyword", search);
        } else {
            categories = categoryService.findAll();
        }

        model.addAttribute("categories", categories);
        return "categories/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("categoryDto", new CategoryDto());
        return "categories/form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveCategory(@ModelAttribute("categoryDto") Category category,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("category", new Category());
            return "categories/form";
        }
        categoryService.save(category);
        redirectAttributes.addFlashAttribute("message", "Category has been saved successfully");
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.findById(id);
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(category.getName());
        categoryDto.setDescription(category.getDescription());
        categoryDto.setImagePath(category.getImagePath());

        model.addAttribute("categoryDto", categoryDto);
        model.addAttribute("category", category);
        return "categories/form";
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateCategory(@PathVariable Long id, @ModelAttribute("categoryDto") CategoryDto categoryDto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if(result.hasErrors()) {
            model.addAttribute("category", categoryService.findById(id));
            return "categories/form";
        }
        categoryService.update(id, categoryDto);
        redirectAttributes.addFlashAttribute("message", "Category has been updated successfully");
        return "redirect:/categories";
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Category has been deleted successfully");
        return "redirect:/categories";
    }
}
