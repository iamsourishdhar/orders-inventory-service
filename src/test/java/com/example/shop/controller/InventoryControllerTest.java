
package com.example.shop.controller;

import com.example.shop.domain.Inventory;
import com.example.shop.service.InventoryService;
import com.example.shop.web.error.GlobalExceptionHandler;
import com.example.shop.web.error.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InventoryController.class)
@Import(GlobalExceptionHandler.class)
class InventoryControllerTest {

  @Autowired private MockMvc mvc;
  @MockBean private InventoryService inventoryService;

  @Test
  void get_inventory_returns_200() throws Exception {
    Inventory inv = new Inventory(); inv.setProductId("SKU-BOOK-123"); inv.setTotalStock(10); inv.setReservedStock(4);
    Mockito.when(inventoryService.getInventoryByProductId("SKU-BOOK-123")).thenReturn(inv);
    mvc.perform(get("/inventory/SKU-BOOK-123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productId", is("SKU-BOOK-123")))
        .andExpect(jsonPath("$.totalStock", is(10)))
        .andExpect(jsonPath("$.reservedStock", is(4)))
        .andExpect(jsonPath("$.availableStock", is(6)));
  }

  @Test
  void get_inventory_not_found_returns_404() throws Exception {
    Mockito.when(inventoryService.getInventoryByProductId("SKU-X")).thenThrow(new NotFoundException("Product not found"));
    mvc.perform(get("/inventory/SKU-X"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error", is("Not Found")))
        .andExpect(jsonPath("$.message", containsString("Product not found")));
  }

  @Test
  void upsert_inventory_returns_200() throws Exception {
    Inventory inv = new Inventory(); inv.setProductId("SKU-NEW"); inv.setTotalStock(20); inv.setReservedStock(0);
    Mockito.when(inventoryService.upsertInventory("SKU-NEW", 20)).thenReturn(inv);
    mvc.perform(post("/inventory/SKU-NEW").param("totalStock", "20").contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productId", is("SKU-NEW")))
        .andExpect(jsonPath("$.totalStock", is(20)))
        .andExpect(jsonPath("$.reservedStock", is(0)))
        .andExpect(jsonPath("$.availableStock", is(20)));
  }
}
