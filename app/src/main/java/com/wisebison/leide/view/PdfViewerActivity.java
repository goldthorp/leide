package com.wisebison.leide.view;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.tejpratapsingh.pdfcreator.activity.PDFViewerActivity;
import com.tejpratapsingh.pdfcreator.utils.PDFUtil;
import com.wisebison.leide.R;

import java.io.File;

public class PdfViewerActivity extends PDFViewerActivity {
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources()
        .getColor(R.color.colorTransparentBlack, null)));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_pdf_viewer, menu);
    // return true so that the menu pop up is opened
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
    } else if (item.getItemId() == R.id.menuPrintPdf) {
      final File fileToPrint = getPdfFile();
      if (fileToPrint == null || !fileToPrint.exists()) {
        Toast.makeText(this, getResources().getString(R.string.generic_error_message), Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
      }

      final PrintAttributes.Builder printAttributeBuilder = new PrintAttributes.Builder();
      printAttributeBuilder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
      printAttributeBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);

      PDFUtil.printPdf(PdfViewerActivity.this, fileToPrint, printAttributeBuilder.build());
    }
    return super.onOptionsItemSelected(item);
  }
}
