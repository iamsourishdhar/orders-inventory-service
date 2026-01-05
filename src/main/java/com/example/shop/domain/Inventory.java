
package com.example.shop.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "inventory", uniqueConstraints = {
  @UniqueConstraint(name = "uk_inventory_product", columnNames = "product_id")
})
public class Inventory {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(name = "product_id", nullable = false, unique = true)
  private String productId;

  @Min(0)
  @Column(name = "total_stock", nullable = false)
  private int totalStock;

  @Min(0)
  @Column(name = "reserved_stock", nullable = false)
  private int reservedStock;

  public Long getId() { return id; }
  public String getProductId() { return productId; }
  public void setProductId(String productId) { this.productId = productId; }
  public int getTotalStock() { return totalStock; }
  public void setTotalStock(int totalStock) { this.totalStock = totalStock; }
  public int getReservedStock() { return reservedStock; }
  public void setReservedStock(int reservedStock) { this.reservedStock = reservedStock; }

  @Transient
  public int getAvailableStock() { return totalStock - reservedStock; }
}
