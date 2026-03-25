package com.example.Bai6.service;

import com.example.Bai6.DTO.CartItem;
import com.example.Bai6.model.Order;
import com.example.Bai6.model.OrderDetail;
import com.example.Bai6.model.Product;
import com.example.Bai6.repository.OrderRepository;
import com.example.Bai6.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Câu 7: Xử lý đặt hàng – tạo Order, lưu OrderDetail, tính tổng tiền.
 */
@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;

	public OrderService(OrderRepository orderRepository,
			ProductRepository productRepository) {
		this.orderRepository = orderRepository;
		this.productRepository = productRepository;
	}

	/**
	 * Tạo đơn hàng từ danh sách CartItem trong session.
	 * 1. Tạo Order (ngày, tên khách, tổng tiền)
	 * 2. Tạo OrderDetail cho từng sản phẩm (lưu giá tại thời điểm mua)
	 * 3. Lưu tất cả vào DB nhờ CascadeType.ALL
	 */
	@Transactional
	public Order checkout(List<CartItem> cartItems, String customerName) {

		// ── Tính tổng tiền ──────────────────────────────────────────────────
		long total = cartItems.stream().mapToLong(CartItem::getSubtotal).sum();

		// ── Tạo Order ───────────────────────────────────────────────────────
		Order order = new Order();
		order.setCustomerName(customerName);
		order.setOrderDate(LocalDateTime.now());
		order.setTotalAmount(total);
		order.setOrderDetails(new ArrayList<>());

		// ── Tạo từng OrderDetail ────────────────────────────────────────────
		for (CartItem item : cartItems) {
			Product product = productRepository.findById(item.getProductId())
					.orElseThrow(() -> new RuntimeException(
							"Không tìm thấy sản phẩm id=" + item.getProductId()));

			OrderDetail detail = new OrderDetail();
			detail.setOrder(order);
			detail.setProduct(product);
			detail.setQuantity(item.getQuantity());
			detail.setPrice(item.getPrice()); // lưu giá tại thời điểm mua

			order.getOrderDetails().add(detail);
		}

		// ── Lưu Order + cascade lưu OrderDetail ─────────────────────────────
		return orderRepository.save(order);
	}
}