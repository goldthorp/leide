package com.wisebison.leide.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.ModuleDao;
import com.wisebison.leide.model.Module;
import com.wisebison.leide.util.BackgroundUtil;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class ModuleFragment extends Fragment {

  private final Module module;

  @Nullable
  @Override
  public View onCreateView(
    @NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
    @Nullable final Bundle savedInstanceState) {
    final View rootView = inflater.inflate(getLayoutResource(), container, false);

    final ModuleDao moduleDao = AppDatabase.getInstance(requireContext()).getModuleDao();

    setChildListener(rootView, v -> {
      BackgroundUtil.doInBackgroundNow(() -> moduleDao.delete(module));
      return false;
    });

    return rootView;
  }

  private void setChildListener(final View parent, final View.OnLongClickListener listener) {
    parent.setOnLongClickListener(listener);
    if (!(parent instanceof ViewGroup)) {
      return;
    }

    final ViewGroup parentGroup = (ViewGroup) parent;
    for (int i = 0; i < parentGroup.getChildCount(); i++) {
      setChildListener(parentGroup.getChildAt(i), listener);
    }
  }

  abstract int getLayoutResource();
}
