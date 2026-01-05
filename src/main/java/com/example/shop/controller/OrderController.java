
package com.example.shop.controller;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderItem;
import com.example.shop.service.OrderService;
import com.example.shop.web.dto.CreateOrderRequest;
import com.example.shop.web.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@EnableAsync
public class OrderController {
  private final OrderService orderService;
  public OrderController(OrderService orderService) { this.orderService = orderService; }

  @PostMapping
  @Async
  public CompletableFuture<ResponseEntity<OrderResponse>> create(@Valid @RequestBody CreateOrderRequest req) {
    List<OrderItem> items = req.getItems().stream()
            .map(i -> new OrderItem(i.getProductId(), i.getQuantity()))
            .collect(Collectors.toList());

    return orderService.createOrder(req.getUserId(), items)
            .thenApply(order -> {
              OrderResponse body = toResponse(order);
              return ResponseEntity.created(URI.create("/orders/" + order.getId())).body(body);
            })
            .exceptionally(ex -> {
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                      .body(null);
            });
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> get(@PathVariable Long id) {
    Order order = orderService.getOrderById(id);
    return ResponseEntity.ok(toResponse(order));
  }

  @PostMapping("/{id}/confirm")
  public ResponseEntity<OrderResponse> confirm(@PathVariable Long id) {
    Order order = orderService.confirmOrder(id);
    return ResponseEntity.ok(toResponse(order));
  }

  @PostMapping("/{id}/cancel")
  public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
    Order order = orderService.cancelOrder(id);
    return ResponseEntity.ok(toResponse(order));
  }

  private OrderResponse toResponse(Order order) {
    OrderResponse res = new OrderResponse();
    res.id = order.getId();
    res.userId = order.getUser().getId();
    res.status = order.getStatus();
    res.items = order.getItems().stream()
            .map(i -> new OrderResponse.Item(i.getProductId(), i.getQuantity()))
            .collect(Collectors.toList());
    res.createdAt = order.getCreatedAt();
    res.updatedAt = order.getUpdatedAt();
    return res;
  }

  private Throwable unwrap(Throwable t) {
    return (t instanceof CompletionException || t instanceof ExecutionException) && t.getCause() != null
            ? t.getCause() : t;
  }
}
