package com.example.Bai6.repository;

import com.example.Bai6.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

	Page<Product> findByCategoryId(int categoryId, Pageable pageable);

	Page<Product> findByNameContainingIgnoreCaseAndCategoryId(String keyword, int categoryId, Pageable pageable);
}