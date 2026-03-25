package com.example.Bai6.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {

	private Long productId;
	private String productName;
	private long price;
	private int quantity;
	private String image;

	public long getSubtotal() {
		return price * quantity;
	}
}