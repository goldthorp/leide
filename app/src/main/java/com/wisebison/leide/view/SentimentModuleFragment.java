package com.wisebison.leide.view;

import com.wisebison.leide.R;
import com.wisebison.leide.model.Module;

public class SentimentModuleFragment extends ModuleFragment {
  public SentimentModuleFragment(final Module module) {
    super(module);
  }

  @Override
  int getLayoutResource() {
    return R.layout.fragment_sentiment_module;
  }
}
