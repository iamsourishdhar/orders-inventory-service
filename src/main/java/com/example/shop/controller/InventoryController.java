
package com.example.shop.controller;

import com.example.shop.domain.Inventory;
import com.example.shop.service.InventoryService;
import com.example.shop.web.dto.InventoryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
  private final InventoryService inventoryService;
  public InventoryController(InventoryService inventoryService) { this.inventoryService = inventoryService; }

  @GetMapping("/{productId}")
  public ResponseEntity<InventoryResponse> get(@PathVariable String productId) {
    Inventory inv = inventoryService.getInventoryByProductId(productId);
    InventoryResponse res = new InventoryResponse();
    res.productId = inv.getProductId();
    res.totalStock = inv.getTotalStock();
    res.reservedStock = inv.getReservedStock();
    res.availableStock = inv.getAvailableStock();
    return ResponseEntity.ok(res);
  }

  @PostMapping("/{productId}")
  public ResponseEntity<InventoryResponse> upsert(@PathVariable String productId,
                                                @RequestParam int totalStock) {
    Inventory inv = inventoryService.upsertInventory(productId, totalStock);
    InventoryResponse res = new InventoryResponse();
    res.productId = inv.getProductId();
    res.totalStock = inv.getTotalStock();
    res.reservedStock = inv.getReservedStock();
    res.availableStock = inv.getAvailableStock();
    return ResponseEntity.ok(res);
  }
}
