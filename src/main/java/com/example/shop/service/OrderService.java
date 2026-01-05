
package com.example.shop.service;

import com.example.shop.domain.*;
import com.example.shop.repository.InventoryRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.UserRepository;
import com.example.shop.web.error.BadRequestException;
import com.example.shop.web.error.ConflictException;
import com.example.shop.web.error.NotFoundException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
@EnableAsync
@Service
public class OrderService {
  private final OrderRepository orderRepo;
  private final UserRepository userRepo;
  private final InventoryRepository inventoryRepo;

  public OrderService(OrderRepository orderRepo, UserRepository userRepo, InventoryRepository inventoryRepo) {
    this.orderRepo = orderRepo;
    this.userRepo = userRepo;
    this.inventoryRepo = inventoryRepo;
  }

  /** Create order and atomically reserve inventory (pessimistic locking). */
  @Async
  @Transactional
  public CompletableFuture<Order> createOrder(Long userId, List<OrderItem> items) {
    if (items == null || items.isEmpty()) {
      throw new BadRequestException("Order must contain at least one item");
    }

    User user = userRepo.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found: " + userId));

    // Acquire locks in deterministic order to avoid deadlocks
    List<OrderItem> sorted = items.stream()
        .sorted(Comparator.comparing(OrderItem::getProductId))
        .collect(Collectors.toList());

    Map<String, Inventory> locked = new HashMap<>();

    // Lock & reserve
    for (OrderItem item : sorted) {
      Inventory inv = inventoryRepo.lockByProductId(item.getProductId())
          .orElseThrow(() -> new NotFoundException("Inventory not found for product: " + item.getProductId()));

      int available = inv.getTotalStock() - inv.getReservedStock();
      if (item.getQuantity() <= 0) {
        throw new BadRequestException("Quantity must be positive for product: " + item.getProductId());
      }
      if (available < item.getQuantity()) {
        throw new ConflictException("Insufficient stock for product " + item.getProductId()
            + " (available=" + available + ", requested=" + item.getQuantity() + ")");
      }

      inv.setReservedStock(inv.getReservedStock() + item.getQuantity());
      inventoryRepo.save(inv);
      locked.put(item.getProductId(), inv);
    }

    // Persist order as PENDING
    Order order = new Order();
    order.setUser(user);
    order.setItems(new ArrayList<>(items));
    order.setStatus(OrderStatus.PENDING);
    order.touch();

    return CompletableFuture.supplyAsync(() -> orderRepo.save(order));
  }

  /** Confirm a PENDING order. */
  @Transactional
  public Order confirmOrder(Long orderId) {
    Order order = orderRepo.findById(orderId)
        .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

    if (order.getStatus() != OrderStatus.PENDING) {
      throw new ConflictException("Order not in PENDING state; current: " + order.getStatus());
    }

    order.setStatus(OrderStatus.CONFIRMED);
    order.touch();
    return orderRepo.save(order);
  }

  /** Cancel a PENDING order and release inventory reservations. */
  @Transactional
  public Order cancelOrder(Long orderId) {
    Order order = orderRepo.findById(orderId)
        .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

    if (order.getStatus() != OrderStatus.PENDING) {
        throw new ConflictException("Only PENDING orders can be cancelled; current: " + order.getStatus());
    }

    List<OrderItem> sorted = order.getItems().stream()
        .sorted(Comparator.comparing(OrderItem::getProductId)).toList();

    for (OrderItem item : sorted) {
      Inventory inv = inventoryRepo.lockByProductId(item.getProductId())
          .orElseThrow(() -> new NotFoundException("Inventory not found for product: " + item.getProductId()));

      int newReserved = inv.getReservedStock() - item.getQuantity();
      if (newReserved < 0) newReserved = 0;
      inv.setReservedStock(newReserved);
      inventoryRepo.save(inv);
    }

    order.setStatus(OrderStatus.CANCELLED);
    order.touch();
    return orderRepo.save(order);
  }

  @Transactional(readOnly = true)
  public Order getOrderById(Long id) {
    return orderRepo.findById(id)
        .orElseThrow(() -> new NotFoundException("Order not found: " + id));
  }
}
