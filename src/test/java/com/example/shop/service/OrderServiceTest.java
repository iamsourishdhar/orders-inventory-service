
package com.example.shop.service;

import com.example.shop.domain.*;
import com.example.shop.repository.InventoryRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.UserRepository;
import com.example.shop.web.error.BadRequestException;
import com.example.shop.web.error.ConflictException;
import com.example.shop.web.error.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock private OrderRepository orderRepo;
  @Mock private UserRepository userRepo;
  @Mock private InventoryRepository inventoryRepo;

  @InjectMocks private OrderService orderService;

  private User user;

  @BeforeEach
  void setup() {
    user = new User();
    try { var idField = User.class.getDeclaredField("id"); idField.setAccessible(true); idField.set(user, 1L);} catch (Exception ignored) {}
    user.setEmail("demo@example.com");
    user.setDisplayName("Demo User");
    when(userRepo.findById(1L)).thenReturn(Optional.of(user));
  }

  @Test
  void createOrder_success_reserves_inventory_atomically() {
    var items = List.of(new OrderItem("SKU-BOOK-123", 2), new OrderItem("SKU-MUG-456", 1));
    Inventory invBook = inv("SKU-BOOK-123", 10, 3);
    Inventory invMug  = inv("SKU-MUG-456", 5, 1);
    when(inventoryRepo.lockByProductId("SKU-BOOK-123")).thenReturn(Optional.of(invBook));
    when(inventoryRepo.lockByProductId("SKU-MUG-456")).thenReturn(Optional.of(invMug));
    when(inventoryRepo.save(any())).thenAnswer(a -> a.getArgument(0));
    when(orderRepo.save(any())).thenAnswer(a -> a.getArgument(0));
    Order created = orderService.createOrder(1L, items);
    assertNotNull(created);
    assertEquals(OrderStatus.PENDING, created.getStatus());
    assertEquals(5, invBook.getReservedStock());
    assertEquals(2, invMug.getReservedStock());
    verify(orderRepo).save(any(Order.class));
  }

  @Test
  void createOrder_fails_cleanly_on_insufficient_stock() {
    var items = List.of(new OrderItem("SKU-BOOK-123", 2), new OrderItem("SKU-MUG-456", 10));
    Inventory invBook = inv("SKU-BOOK-123", 10, 3);
    Inventory invMug  = inv("SKU-MUG-456", 5, 4);
    when(inventoryRepo.lockByProductId("SKU-BOOK-123")).thenReturn(Optional.of(invBook));
    when(inventoryRepo.lockByProductId("SKU-MUG-456")).thenReturn(Optional.of(invMug));
    when(inventoryRepo.save(any())).thenAnswer(a -> a.getArgument(0));
    ConflictException ex = assertThrows(ConflictException.class, () -> orderService.createOrder(1L, items));
    assertTrue(ex.getMessage().contains("Insufficient stock"));
    verify(orderRepo, never()).save(any(Order.class));
  }

  @Test
  void createOrder_bad_request_on_empty_items() {
    assertThrows(BadRequestException.class, () -> orderService.createOrder(1L, Collections.emptyList()));
  }

  @Test
  void confirmOrder_changes_status_if_pending() {
    Order o = new Order(); o.setUser(user); o.setItems(List.of(new OrderItem("SKU-1", 1))); o.setStatus(OrderStatus.PENDING);
    when(orderRepo.findById(42L)).thenReturn(Optional.of(o));
    when(orderRepo.save(any())).thenAnswer(a -> a.getArgument(0));
    Order res = orderService.confirmOrder(42L);
    assertEquals(OrderStatus.CONFIRMED, res.getStatus());
  }

  @Test
  void confirmOrder_conflict_if_not_pending() {
    Order o = new Order(); o.setUser(user); o.setStatus(OrderStatus.CANCELLED);
    when(orderRepo.findById(99L)).thenReturn(Optional.of(o));
    assertThrows(ConflictException.class, () -> orderService.confirmOrder(99L));
  }

  @Test
  void cancelOrder_releases_reservations_and_sets_cancelled() {
    Order o = new Order(); o.setUser(user);
    o.setItems(List.of(new OrderItem("SKU-BOOK-123", 2), new OrderItem("SKU-MUG-456", 1)));
    o.setStatus(OrderStatus.PENDING);
    Inventory invBook = inv("SKU-BOOK-123", 10, 5);
    Inventory invMug  = inv("SKU-MUG-456", 5, 2);
    when(orderRepo.findById(7L)).thenReturn(Optional.of(o));
    when(inventoryRepo.lockByProductId("SKU-BOOK-123")).thenReturn(Optional.of(invBook));
    when(inventoryRepo.lockByProductId("SKU-MUG-456")).thenReturn(Optional.of(invMug));
    when(orderRepo.save(any())).thenAnswer(a -> a.getArgument(0));
    Order res = orderService.cancelOrder(7L);
    assertEquals(OrderStatus.CANCELLED, res.getStatus());
    assertEquals(3, invBook.getReservedStock());
    assertEquals(1, invMug.getReservedStock());
  }

  @Test
  void getOrderById_not_found() {
    when(orderRepo.findById(55L)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> orderService.getOrderById(55L));
  }

  private Inventory inv(String productId, int total, int reserved) {
    Inventory i = new Inventory(); i.setProductId(productId); i.setTotalStock(total); i.setReservedStock(reserved); return i;
  }
}
