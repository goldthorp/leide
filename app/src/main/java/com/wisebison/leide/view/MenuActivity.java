package com.wisebison.leide.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.print.PDFPrint;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.tejpratapsingh.pdfcreator.utils.PDFUtil;
import com.wisebison.leide.R;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.EntryDao;
import com.wisebison.leide.model.EntryForm;
import com.wisebison.leide.util.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MenuActivity extends AppCompatActivity {

  private static final String TAG = MenuActivity.class.getSimpleName();

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_menu);

    final TextView exportEntries = findViewById(R.id.export_entries_text_view);
    exportEntries.setOnClickListener(v -> {
      final File externalFilesDir = getApplicationContext().getExternalFilesDir(null);
      final EntryDao entryDao = AppDatabase.getInstance(getApplicationContext()).getEntryDao();
      final SpannableStringBuilder ssb = new SpannableStringBuilder();
      final Snackbar snackbar =
        Snackbar.make(findViewById(R.id.menu_layout), R.string.loading, BaseTransientBottomBar.LENGTH_INDEFINITE);
      snackbar.show();
      final SimpleDateFormat sdf = new SimpleDateFormat("M-d-yy_HHmm", Locale.US);
      entryDao.getListForExport().then(entries -> {
        for (final EntryForm entry : entries) {
          ssb.append(entry.getEntryForDisplay(getApplicationContext(), false));
          ssb.append("\n\n");
        }
        snackbar.setText(R.string.creating_pdf);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          final String html = Html.toHtml(ssb, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
          PDFUtil.generatePDFFromHTML(getApplicationContext(), new File(externalFilesDir,
              "export_" + sdf.format(new Date(System.currentTimeMillis())) + ".pdf"),
            html, new PDFPrint.OnPDFPrintListener() {
              @Override
              public void onSuccess(final File file) {
                snackbar.dismiss();
                try {
                  final Uri pdfUri = Uri.fromFile(file);
                  final Intent intent = new Intent(getApplicationContext(), PdfViewerActivity.class);
                  intent.putExtra(PdfViewerActivity.PDF_FILE_URI, pdfUri);
                  startActivity(intent);
                } catch (final Exception e) {
                  e.printStackTrace();
                }
              }

              @Override
              public void onError(final Exception exception) {
                snackbar.setText(R.string.generic_error_message);
                Utils.doAfter(snackbar::dismiss, 3000);
              }
            });
        } else {
          new AlertDialog.Builder(getApplicationContext())
            .setTitle(R.string.sorry)
            .setMessage(R.string.version_error_min_7)
            .create();
        }
      });
    });

    final ImageView exit = findViewById(R.id.exit);
    exit.setOnClickListener(v -> {
      finish();
    });
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.menu_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
    if (item.getItemId() == R.id.action_close) {
      finish();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void finish() {
    super.finish();
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
  }
}
