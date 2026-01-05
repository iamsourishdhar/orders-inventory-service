
package com.example.shop.service;

import com.example.shop.domain.Inventory;
import com.example.shop.repository.InventoryRepository;
import com.example.shop.web.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
  private final InventoryRepository inventoryRepo;

  public InventoryService(InventoryRepository inventoryRepo) {
    this.inventoryRepo = inventoryRepo;
  }

  @Transactional(readOnly = true)
  public Inventory getInventoryByProductId(String productId) {
    return inventoryRepo.findByProductId(productId)
        .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
  }

  @Transactional
  public Inventory upsertInventory(String productId, int totalStock) {
    Inventory inv = inventoryRepo.findByProductId(productId).orElseGet(Inventory::new);
    inv.setProductId(productId);
    inv.setTotalStock(totalStock);
    if (inv.getReservedStock() < 0) inv.setReservedStock(0);
    return inventoryRepo.save(inv);
  }
}
