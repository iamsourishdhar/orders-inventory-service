
package com.example.shop.controller;

import com.example.shop.domain.*;
import com.example.shop.service.OrderService;
import com.example.shop.web.dto.CreateOrderRequest;
import com.example.shop.web.error.GlobalExceptionHandler;
import com.example.shop.web.error.NotFoundException;
import com.example.shop.web.error.ConflictException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper mapper;

  @MockBean private OrderService orderService;

  private Order sampleOrder(Long id, OrderStatus status) {
    User u = new User();
    try { var idField = User.class.getDeclaredField("id"); idField.setAccessible(true); idField.set(u, 1L);} catch (Exception ignored) {}
    u.setEmail("demo@example.com"); u.setDisplayName("Demo User");
    Order o = new Order();
    try { var idField = Order.class.getDeclaredField("id"); idField.setAccessible(true); idField.set(o, id);} catch (Exception ignored) {}
    o.setUser(u); o.setStatus(status);
    o.setItems(List.of(new OrderItem("SKU-BOOK-123", 2), new OrderItem("SKU-MUG-456", 1)));
    return o;
  }

  @Test
  void post_orders_creates_and_returns_201() throws Exception {
    CreateOrderRequest req = new CreateOrderRequest();
    req.setUserId(1L);
    CreateOrderRequest.Item i1 = new CreateOrderRequest.Item(); i1.setProductId("SKU-BOOK-123"); i1.setQuantity(2);
    CreateOrderRequest.Item i2 = new CreateOrderRequest.Item(); i2.setProductId("SKU-MUG-456"); i2.setQuantity(1);
    req.setItems(List.of(i1, i2));
    Order created = sampleOrder(101L, OrderStatus.PENDING);
    Mockito.when(orderService.createOrder(Mockito.eq(1L), Mockito.anyList())).thenReturn(created);
    mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(req)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/orders/101"))
        .andExpect(jsonPath("$.id", is(101)))
        .andExpect(jsonPath("$.userId", is(1)))
        .andExpect(jsonPath("$.status", is("PENDING")))
        .andExpect(jsonPath("$.items", hasSize(2)));
  }

  @Test
  void post_orders_conflict_on_insufficient_stock() throws Exception {
    CreateOrderRequest req = new CreateOrderRequest(); req.setUserId(1L);
    CreateOrderRequest.Item i1 = new CreateOrderRequest.Item(); i1.setProductId("SKU-BOOK-123"); i1.setQuantity(100);
    req.setItems(List.of(i1));
    Mockito.when(orderService.createOrder(Mockito.eq(1L), Mockito.anyList())).thenThrow(new ConflictException("Insufficient stock"));
    mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(req)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error", is("Conflict")))
        .andExpect(jsonPath("$.message", containsString("Insufficient stock")));
  }

  @Test
  void get_order_returns_200() throws Exception {
    Order o = sampleOrder(123L, OrderStatus.PENDING);
    Mockito.when(orderService.getOrderById(123L)).thenReturn(o);
    mvc.perform(get("/orders/123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(123)))
        .andExpect(jsonPath("$.items", hasSize(2)));
  }

  @Test
  void get_order_not_found_returns_404() throws Exception {
    Mockito.when(orderService.getOrderById(999L)).thenThrow(new NotFoundException("Order not found"));
    mvc.perform(get("/orders/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error", is("Not Found")))
        .andExpect(jsonPath("$.message", containsString("Order not found")));
  }
}
