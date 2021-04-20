package com.wisebison.leide.util;

import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ArrayAdapterItem<T> {
  private T id;
  private String display;

  @NonNull
  @Override
  public String toString() {
    return display;
  }
}
