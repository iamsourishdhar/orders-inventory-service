
package com.example.shop.web.error;

public class BadRequestException extends RuntimeException {
  public BadRequestException(String msg) { super(msg); }
}
