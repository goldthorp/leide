package com.wisebison.leide.view;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.ModuleDao;
import com.wisebison.leide.model.Module;
import com.wisebison.leide.util.BackgroundUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public abstract class ModuleFragment extends Fragment {

  private static final String TAG = "ModuleFragment";

  @Getter
  private int color;

  @Getter
  private View rootView;

  ModuleFragment(final Module module) {
    final Bundle args = new Bundle();
    args.putSerializable("module", module);
    setArguments(args);
  }

  @Nullable
  @Override
  public View onCreateView(
    @NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
    @Nullable final Bundle savedInstanceState) {
    rootView = inflater.inflate(getLayoutResource(), container, false);

    final ModuleDao moduleDao = AppDatabase.getInstance(requireContext()).getModuleDao();

    setChildListener(rootView, v -> {
      final Module module = getModule();
//      BackgroundUtil.doInBackgroundNow(() -> moduleDao.delete(module));
      ColorPickerDialog.newBuilder().setDialogId(module.getModuleType().ordinal())
        .show(getActivity());
      return false;
    });

    setColor(getModule().getColor());

    return rootView;
  }

  public void setColor(int color) {
    this.color = color;
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setColor(color);
    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
      getResources().getDisplayMetrics());
    gradientDrawable.setCornerRadius(px);
    rootView.setBackground(gradientDrawable);
  }

  private Module getModule() {
    final Bundle args = getArguments();
    if (args == null) {
      throw new IllegalStateException("Args not found");
    }
    final Module module = (Module) args.getSerializable("module");
    if (module == null) {
      throw new IllegalStateException("Module not found");
    }
    return module;
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
