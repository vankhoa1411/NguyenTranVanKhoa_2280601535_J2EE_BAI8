package com.example.Bai6.service;

import com.example.Bai6.DTO.CartItem;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Câu 5 + 6: Quản lý giỏ hàng lưu trong HttpSession.
 * Key session: "cart" → List<CartItem>
 */
@Service
public class CartService {

	private static final String CART_KEY = "cart";

	/** Lấy giỏ hàng từ session (tạo mới nếu chưa có) */
	@SuppressWarnings("unchecked")
	public List<CartItem> getCart(HttpSession session) {
		List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_KEY);
		if (cart == null) {
			cart = new ArrayList<>();
			session.setAttribute(CART_KEY, cart);
		}
		return cart;
	}

	/**
	 * Câu 5: Thêm sản phẩm vào giỏ.
	 * Nếu đã tồn tại → cộng dồn quantity.
	 */
	public void addToCart(HttpSession session, CartItem newItem) {
		List<CartItem> cart = getCart(session);
		for (CartItem item : cart) {
			if (item.getProductId().equals(newItem.getProductId())) {
				item.setQuantity(item.getQuantity() + newItem.getQuantity());
				session.setAttribute(CART_KEY, cart);
				return;
			}
		}
		cart.add(newItem);
		session.setAttribute(CART_KEY, cart);
	}

	/** Xóa một sản phẩm khỏi giỏ */
	public void removeFromCart(HttpSession session, Long productId) {
		List<CartItem> cart = getCart(session);
		cart.removeIf(item -> item.getProductId().equals(productId));
		session.setAttribute(CART_KEY, cart);
	}

	/** Cập nhật số lượng (quantity ≤ 0 → xóa luôn) */
	public void updateQuantity(HttpSession session, Long productId, int quantity) {
		List<CartItem> cart = getCart(session);
		for (CartItem item : cart) {
			if (item.getProductId().equals(productId)) {
				if (quantity <= 0)
					cart.remove(item);
				else
					item.setQuantity(quantity);
				break;
			}
		}
		session.setAttribute(CART_KEY, cart);
	}

	/** Tổng tiền toàn giỏ */
	public long getTotalAmount(HttpSession session) {
		return getCart(session).stream().mapToLong(CartItem::getSubtotal).sum();
	}

	/** Tổng số lượng sản phẩm (hiển thị badge navbar) */
	public int getTotalItems(HttpSession session) {
		return getCart(session).stream().mapToInt(CartItem::getQuantity).sum();
	}

	/** Câu 7: Xóa giỏ hàng sau khi đặt hàng thành công */
	public void clearCart(HttpSession session) {
		session.removeAttribute(CART_KEY);
	}
}