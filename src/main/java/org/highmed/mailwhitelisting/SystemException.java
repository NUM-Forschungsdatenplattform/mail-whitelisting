package org.highmed.mailwhitelisting;

public class SystemException extends RuntimeException {

  public SystemException(String message, Throwable cause) {
    super(message, cause);
  }

  public SystemException(String message) {
    super(message);
  }
}
