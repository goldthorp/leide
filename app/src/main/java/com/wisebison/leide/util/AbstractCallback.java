package com.wisebison.leide.util;

public interface AbstractCallback<T> {
  void resolve(T result);
}
