package com.wisebison.leide.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.wisebison.leide.R;
import com.wisebison.leide.data.AppDatabase;
import com.wisebison.leide.data.EntryComponentDao;
import com.wisebison.leide.model.EntryComponent;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class MenuActivity extends AppCompatActivity {

  private static final String TAG = "MenuActivity";

  private static final int CREATE_FILE_REQUEST_CODE = 1;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_menu);

    final TextView exportEntries = findViewById(R.id.export_entries_text_view);
    exportEntries.setOnClickListener(v -> {
      final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("text/plain");
      final SimpleDateFormat sdf = new SimpleDateFormat("M-d-yy_HHmm", Locale.US);
      intent.putExtra(Intent.EXTRA_TITLE,
        "export_" + sdf.format(new Date(System.currentTimeMillis())) + ".txt");

      startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    });

    final ImageView exit = findViewById(R.id.exit);
    exit.setOnClickListener(v -> {
      finish();
    });
  }

  @Override
  public void onActivityResult(
    final int requestCode, final int resultCode, final Intent resultData) {
    super.onActivityResult(requestCode, resultCode, resultData);
    if (requestCode == CREATE_FILE_REQUEST_CODE
      && resultCode == Activity.RESULT_OK) {
      if (resultData != null) {
        final Uri uri = Objects.requireNonNull(resultData.getData());
        try {
          final ParcelFileDescriptor pfd =  getContentResolver().openFileDescriptor(uri, "w");
          writeEntryTextToFile(Objects.requireNonNull(pfd));
        } catch (final FileNotFoundException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void writeEntryTextToFile(final ParcelFileDescriptor pfd) {
    final FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());

    final StringBuilder stringBuilder = new StringBuilder();
    final AppDatabase appDatabase = AppDatabase.getInstance(this);
    final EntryComponentDao entryComponentDao = appDatabase.getEntryComponentDao();
    entryComponentDao.getAll().then(components -> {
      if (CollectionUtils.isEmpty(components)) {
        Log.d(TAG, "Attempting to export empty components " + components);
        return;
      }
      try {
        final long firstEntryId = components.get(0).getEntryId();
        for (final EntryComponent component : components) {
          if (component.getEntryId() != firstEntryId && component.getListSeq() == 0) {
            stringBuilder.append("\n");
          }
          try {
            switch (component.getType()) {
              case TEXT:
                stringBuilder.append(component.getValue());
                break;
              case DATE:
                final SimpleDateFormat sdf = new SimpleDateFormat("E, MMM dd yyyy h:mm a", Locale.US);
                final JSONObject dateJson = new JSONObject(component.getValue());
                if (dateJson.has("timeZone") &&
                  StringUtils.isNotBlank(dateJson.getString("timeZone"))) {
                  sdf.setTimeZone(TimeZone.getTimeZone(dateJson.getString("timeZone")));
                }
                stringBuilder.append(sdf.format(new Date(dateJson.getLong("millis"))));
                break;
              case LOCATION:
                final JSONObject locationJson = new JSONObject(component.getValue());
                stringBuilder.append(locationJson.getString("display"));
                break;
            }
          } catch (final JSONException e) {
            e.printStackTrace();
          }
          stringBuilder.append("\n");
        }
        fos.write(stringBuilder.toString().getBytes());
        fos.close();
        pfd.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
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
