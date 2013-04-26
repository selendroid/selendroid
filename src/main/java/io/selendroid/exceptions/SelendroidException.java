package io.selendroid.exceptions;

public class SelendroidException extends RuntimeException {
  private static final long serialVersionUID = 268831360479853360L;

  public SelendroidException(String message) {
    super(message);
  }

  public SelendroidException(Throwable t) {
    super(t);
  }

  public SelendroidException(String message, Throwable t) {
    super(message, t);
  }
}
