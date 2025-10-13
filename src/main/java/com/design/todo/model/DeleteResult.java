package com.design.todo.model;

public class DeleteResult {
  private final boolean deleted;
  private final int remaining;

  public DeleteResult(boolean deleted, int remaining) {
    this.deleted = deleted;
    this.remaining = remaining;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public int getRemaining() {
    return remaining;
  }
}
