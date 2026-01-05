
package com.example.shop.web.error;

import java.time.OffsetDateTime;

public class ApiError {
  public OffsetDateTime timestamp = OffsetDateTime.now();
  public int status;
  public String error;
  public String message;
  public String path;
}
