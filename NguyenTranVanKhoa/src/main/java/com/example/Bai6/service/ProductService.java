package com.example.Bai6.service;

import com.example.Bai6.model.Product;
import com.example.Bai6.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final String UPLOAD_DIR = "uploads/assets/";

	// Số sản phẩm mỗi trang - Câu 2
	private static final int PAGE_SIZE = 5;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	// ── Giữ nguyên các method cũ ────────────────────────────────────────────

	public List<Product> getAll() {
		return productRepository.findAll();
	}

	public Product getById(Long id) {
		return productRepository.findById(id).orElse(null);
	}

	public Product save(Product product) {
		return productRepository.save(product);
	}

	public void delete(Long id) {
		productRepository.deleteById(id);
	}

	public String uploadImage(MultipartFile file) throws IOException {
		if (file == null || file.isEmpty())
			return null;
		Files.createDirectories(Paths.get(UPLOAD_DIR));
		String ext = file.getOriginalFilename()
				.substring(file.getOriginalFilename().lastIndexOf("."));
		String fileName = UUID.randomUUID() + ext;
		Files.copy(file.getInputStream(),
				Paths.get(UPLOAD_DIR + fileName),
				StandardCopyOption.REPLACE_EXISTING);
		return fileName;
	}

	// ── Method MỚI: Câu 1 + 2 + 3 + 4 ──────────────────────────────────────

	/**
	 * Lấy danh sách sản phẩm có hỗ trợ:
	 * Câu 1 – tìm kiếm theo keyword
	 * Câu 2 – phân trang 5 sản phẩm/trang
	 * Câu 3 – sắp xếp theo giá tăng/giảm dần
	 * Câu 4 – lọc theo category
	 */
	public Page<Product> getProducts(String keyword, int categoryId,
			String sortField, String sortDir,
			int pageNo) {

		Sort sort = sortDir.equalsIgnoreCase("desc")
				? Sort.by(sortField).descending()
				: Sort.by(sortField).ascending();

		Pageable pageable = PageRequest.of(pageNo, PAGE_SIZE, sort);

		boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
		boolean hasCat = categoryId > 0;

		if (hasKeyword && hasCat) {
			return productRepository
					.findByNameContainingIgnoreCaseAndCategoryId(keyword, categoryId, pageable);
		} else if (hasKeyword) {
			return productRepository
					.findByNameContainingIgnoreCase(keyword, pageable);
		} else if (hasCat) {
			return productRepository
					.findByCategoryId(categoryId, pageable);
		} else {
			return productRepository.findAll(pageable);
		}
	}
}