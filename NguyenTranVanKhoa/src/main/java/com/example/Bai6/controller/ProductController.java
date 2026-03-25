package com.example.Bai6.controller;

import com.example.Bai6.DTO.CartItem;
import com.example.Bai6.model.Category;
import com.example.Bai6.model.Product;
import com.example.Bai6.service.CartService;
import com.example.Bai6.service.CategoryService;
import com.example.Bai6.service.ProductService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;
	private final CategoryService categoryService;
	private final CartService cartService;

	public ProductController(ProductService productService,
			CategoryService categoryService,
			CartService cartService) {
		this.productService = productService;
		this.categoryService = categoryService;
		this.cartService = cartService;
	}

	// ═══════════════════════════════════════════════════════════════════════
	// Câu 1 + 2 + 3 + 4 – Danh sách sản phẩm (search / page / sort / filter)
	// ═══════════════════════════════════════════════════════════════════════
	@GetMapping
	public String list(
			@RequestParam(defaultValue = "") String keyword,
			@RequestParam(defaultValue = "0") int categoryId,
			@RequestParam(defaultValue = "name") String sortField,
			@RequestParam(defaultValue = "asc") String sortDir,
			@RequestParam(defaultValue = "0") int page,
			HttpSession session,
			Model model) {

		// Lấy trang sản phẩm
		Page<Product> productPage = productService
				.getProducts(keyword, categoryId, sortField, sortDir, page);

		List<Category> categories = categoryService.getAll();

		// Phân trang
		model.addAttribute("products", productPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", productPage.getTotalPages());
		model.addAttribute("totalItems", productPage.getTotalElements());

		// Tìm kiếm & lọc
		model.addAttribute("keyword", keyword);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("categories", categories);

		// Sắp xếp
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

		// Số sản phẩm trong giỏ (hiển thị badge)
		model.addAttribute("cartCount", cartService.getTotalItems(session));

		return "product/list";
	}

	// ═══════════════════════════════════════════════════════════════════════
	// ADD
	// ═══════════════════════════════════════════════════════════════════════
	@GetMapping("/add")
	public String addForm(Model model) {
		model.addAttribute("product", new Product());
		model.addAttribute("categories", categoryService.getAll());
		return "product/add";
	}

	@PostMapping
	public String create(@Valid @ModelAttribute Product product,
			BindingResult bindingResult,
			@RequestParam(value = "categoryId", required = false) Integer categoryId,
			@RequestParam(value = "imageFile", required = false) MultipartFile file,
			RedirectAttributes redirectAttributes,
			Model model) throws Exception {

		if (bindingResult.hasErrors()) {
			model.addAttribute("categories", categoryService.getAll());
			return "product/add";
		}
		if (categoryId == null) {
			bindingResult.rejectValue("category", "error.category.required", "Vui lòng chọn danh mục");
			model.addAttribute("categories", categoryService.getAll());
			return "product/add";
		}
		Category category = categoryService.getById(categoryId);
		if (category == null) {
			bindingResult.rejectValue("category", "error.category.invalid", "Danh mục không tồn tại");
			model.addAttribute("categories", categoryService.getAll());
			return "product/add";
		}
		product.setCategory(category);
		if (file != null && !file.isEmpty()) {
			product.setImage(productService.uploadImage(file));
		}
		productService.save(product);
		redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công!");
		return "redirect:/products";
	}

	// ═══════════════════════════════════════════════════════════════════════
	// EDIT
	// ═══════════════════════════════════════════════════════════════════════
	@GetMapping("/edit/{id}")
	public String editForm(@PathVariable Long id, Model model) {
		Product product = productService.getById(id);
		if (product == null)
			return "redirect:/products";
		model.addAttribute("product", product);
		model.addAttribute("categories", categoryService.getAll());
		return "product/edit";
	}

	@PostMapping("/update/{id}")
	public String update(@PathVariable Long id,
			@ModelAttribute Product product,
			@RequestParam("categoryId") Integer categoryId,
			@RequestParam(value = "imageFile", required = false) MultipartFile file)
			throws Exception {

		Product existing = productService.getById(id);
		if (existing == null)
			return "redirect:/products";

		existing.setName(product.getName());
		existing.setPrice(product.getPrice());
		existing.setCategory(categoryService.getById(categoryId));
		if (file != null && !file.isEmpty()) {
			existing.setImage(productService.uploadImage(file));
		}
		productService.save(existing);
		return "redirect:/products";
	}

	// ═══════════════════════════════════════════════════════════════════════
	// DELETE
	// ═══════════════════════════════════════════════════════════════════════
	@DeleteMapping("/{id}")
	public String delete(@PathVariable Long id) {
		productService.delete(id);
		return "redirect:/products";
	}

	// ═══════════════════════════════════════════════════════════════════════
	// Câu 5 – Thêm vào giỏ hàng
	// ═══════════════════════════════════════════════════════════════════════
	@PostMapping("/cart/add")
	public String addToCart(@RequestParam Long productId,
			@RequestParam(defaultValue = "1") int quantity,
			HttpSession session) {

		Product product = productService.getById(productId);
		if (product == null)
			return "redirect:/products";

		CartItem item = new CartItem(
				product.getId(),
				product.getName(),
				product.getPrice(),
				quantity,
				product.getImage());
		cartService.addToCart(session, item);
		return "redirect:/cart";
	}
}