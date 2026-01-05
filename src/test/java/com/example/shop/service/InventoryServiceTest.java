
package com.example.shop.service;

import com.example.shop.domain.Inventory;
import com.example.shop.repository.InventoryRepository;
import com.example.shop.web.error.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

  @Mock private InventoryRepository inventoryRepo;
  @InjectMocks private InventoryService inventoryService;

  @Test
  void getInventoryByProductId_returns_inventory() {
    Inventory inv = new Inventory(); inv.setProductId("SKU-BOOK-123"); inv.setTotalStock(10); inv.setReservedStock(4);
    when(inventoryRepo.findByProductId("SKU-BOOK-123")).thenReturn(Optional.of(inv));
    Inventory res = inventoryService.getInventoryByProductId("SKU-BOOK-123");
    assertEquals("SKU-BOOK-123", res.getProductId());
    assertEquals(10, res.getTotalStock());
    assertEquals(4, res.getReservedStock());
    assertEquals(6, res.getAvailableStock());
  }

  @Test
  void getInventoryByProductId_not_found_throws() {
    when(inventoryRepo.findByProductId("SKU-X")).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> inventoryService.getInventoryByProductId("SKU-X"));
  }

  @Test
  void upsertInventory_creates_new_or_updates_existing() {
    when(inventoryRepo.findByProductId("SKU-NEW")).thenReturn(Optional.empty());
    when(inventoryRepo.save(any())).thenAnswer(a -> a.getArgument(0));
    Inventory created = inventoryService.upsertInventory("SKU-NEW", 20);
    assertEquals("SKU-NEW", created.getProductId()); assertEquals(20, created.getTotalStock()); assertEquals(0, created.getReservedStock());

    Inventory existing = new Inventory(); existing.setProductId("SKU-EXIST"); existing.setTotalStock(5); existing.setReservedStock(2);
    when(inventoryRepo.findByProductId("SKU-EXIST")).thenReturn(Optional.of(existing));
    Inventory updated = inventoryService.upsertInventory("SKU-EXIST", 15);
    assertEquals("SKU-EXIST", updated.getProductId()); assertEquals(15, updated.getTotalStock()); assertEquals(2, updated.getReservedStock());
  }
}
