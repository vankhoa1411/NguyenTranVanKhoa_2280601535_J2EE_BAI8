package com.example.Bai6.controller;

import com.example.Bai6.DTO.CartItem;
import com.example.Bai6.model.Order;
import com.example.Bai6.service.CartService;
import com.example.Bai6.service.OrderService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

	private final CartService cartService;
	private final OrderService orderService;

	public CartController(CartService cartService, OrderService orderService) {
		this.cartService = cartService;
		this.orderService = orderService;
	}

	// ═══════════════════════════════════════════════════════════════════════
	// Câu 6 – Xem giỏ hàng
	// ═══════════════════════════════════════════════════════════════════════
	@GetMapping
	public String viewCart(HttpSession session, Model model) {
		List<CartItem> cart = cartService.getCart(session);
		model.addAttribute("cartItems", cart);
		model.addAttribute("totalAmount", cartService.getTotalAmount(session));
		model.addAttribute("cartCount", cartService.getTotalItems(session));
		return "cart/view";
	}

	/** Cập nhật số lượng sản phẩm trong giỏ */
	@PostMapping("/update")
	public String updateCart(@RequestParam Long productId,
			@RequestParam int quantity,
			HttpSession session) {
		cartService.updateQuantity(session, productId, quantity);
		return "redirect:/cart";
	}

	/** Xóa một sản phẩm khỏi giỏ */
	@PostMapping("/remove")
	public String removeItem(@RequestParam Long productId, HttpSession session) {
		cartService.removeFromCart(session, productId);
		return "redirect:/cart";
	}

	// ═══════════════════════════════════════════════════════════════════════
	// Câu 7 – Trang xác nhận đặt hàng
	// ═══════════════════════════════════════════════════════════════════════
	@GetMapping("/checkout")
	public String checkoutPage(HttpSession session, Model model) {
		List<CartItem> cart = cartService.getCart(session);
		if (cart.isEmpty())
			return "redirect:/cart";

		model.addAttribute("cartItems", cart);
		model.addAttribute("totalAmount", cartService.getTotalAmount(session));
		return "cart/checkout";
	}

	/**
	 * Câu 7 – Xử lý đặt hàng:
	 * 1. Tạo Order
	 * 2. Lưu OrderDetail cho từng sản phẩm
	 * 3. Tính tổng tiền
	 * 4. Xóa giỏ hàng khỏi session
	 */
	@PostMapping("/checkout")
	public String processCheckout(@RequestParam String customerName,
			HttpSession session,
			Model model) {
		List<CartItem> cart = cartService.getCart(session);
		if (cart.isEmpty())
			return "redirect:/cart";

		Order order = orderService.checkout(cart, customerName);
		cartService.clearCart(session);

		model.addAttribute("order", order);
		return "cart/order-success";
	}
}