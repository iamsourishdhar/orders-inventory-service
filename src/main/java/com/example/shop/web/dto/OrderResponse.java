
package com.example.shop.web.dto;

import com.example.shop.domain.OrderStatus;
import java.time.OffsetDateTime;
import java.util.List;

public class OrderResponse {
  public Long id;
  public Long userId;
  public OrderStatus status;
  public List<Item> items;
  public OffsetDateTime createdAt;
  public OffsetDateTime updatedAt;

  public static class Item {
    public String productId;
    public int quantity;
    public Item() {}
    public Item(String p, int q) { productId = p; quantity = q; }
  }
}
