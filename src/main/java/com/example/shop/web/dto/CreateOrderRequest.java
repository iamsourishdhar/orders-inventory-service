
package com.example.shop.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CreateOrderRequest {
  @NotNull
  private Long userId;

  @Size(min = 1)
  private List<Item> items;

  public static class Item {
    @NotNull private String productId;
    @NotNull private Integer quantity;
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
  }

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public List<Item> getItems() { return items; }
  public void setItems(List<Item> items) { this.items = items; }
}
