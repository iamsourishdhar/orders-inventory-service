
package com.example.shop.domain;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

@Embeddable
public class OrderItem {
  @NotBlank
  private String productId;

  @Min(1)
  private int quantity;

  public OrderItem() {}
  public OrderItem(String productId, int quantity) {
    this.productId = productId; this.quantity = quantity;
  }

  public String getProductId() { return productId; }
  public void setProductId(String productId) { this.productId = productId; }
  public int getQuantity() { return quantity; }
  public void setQuantity(int quantity) { this.quantity = quantity; }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OrderItem)) return false;
    OrderItem that = (OrderItem) o;
    return quantity == that.quantity && Objects.equals(productId, that.productId);
  }
  @Override public int hashCode() { return Objects.hash(productId, quantity); }
}
