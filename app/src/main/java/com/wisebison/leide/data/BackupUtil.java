package com.wisebison.leide.data;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.wisebison.annotation.BackupEntity;
import com.wisebison.leide.R;
import com.wisebison.leide.view.MainActivity;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dalvik.system.DexFile;

/**
 * Handles backup functions between the local SQLite database and the remote Firebase database.
 *
 * TODO: handle exceptions
 */
public class BackupUtil {

  private static final String TAG = "BackupUtil";

  private final DatabaseReference remoteDb;
  private final AppDatabase localDb;
  private final MainActivity mainActivity;

  private boolean paused;

  public BackupUtil(final MainActivity mainActivity) {
    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    if (currentUser == null) {
      throw new IllegalStateException("must be logged in");
    }
    // Get a reference to the logged in user's node in the remote database
    remoteDb = FirebaseDatabase.getInstance().getReference(currentUser.getUid());

    localDb = AppDatabase.getInstance(mainActivity);

    this.mainActivity = mainActivity;
  }

  /**
   * Backs up the logged in user's local database to the remote firebase database.
   *
   * Will only ever add data to the backup, never remove. This is done by reading the remote
   * database first and checking that all the entries there are present locally (i.e. we check
   * that the remote entries list is a subset of the local entries list). Any entries that are
   * present locally but not remotely will be backed up (they must appear at the end of the
   * local list; if there is somehow a new entry in the middle of the list, backup will fail).
   *
   * Performs backup on a background thread.
   */
  public void start() {
    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    if (currentUser == null) {
      throw new IllegalStateException("must be logged in");
    }
    // Get a reference to the logged in user's node in the remote database
    final DatabaseReference databaseReference =
      FirebaseDatabase.getInstance().getReference(currentUser.getUid());

    final List<LiveData<List<?>>> dataToBackUp = getDataToBackUp();
    for (final LiveData<List<?>> liveData : dataToBackUp) {
      liveData.observe(mainActivity, data -> {
        if (paused) {
          return;
        }
        if (!CollectionUtils.isEmpty(data)) {
          final String nodeName =
            Objects.requireNonNull(data.get(0).getClass().getAnnotation(BackupEntity.class)).name();
          databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
              new BackupTask<>(snapshot.child(nodeName), databaseReference.child(nodeName), data, success -> {
                if (!success) {
                  // Remote entries list was not a subset of local entries list. Backup failed.
                  new AlertDialog.Builder(mainActivity)
                    .setMessage(mainActivity.getString(R.string.backup_failed))
                    .show();
                }
              }).execute();
            }

            @Override
            public void onCancelled(@NonNull final DatabaseError error) {
              Log.e(TAG, "Remote database error: " + error.getMessage(), error.toException());
            }
          });
        }
      });
    }
  }

  /**
   * Get the LiveDatas we need to subscribe to to back up.
   *
   * @return a list of LiveData we need to subscribe to and back up on change
   */
  @SuppressWarnings("unchecked")
  private List<LiveData<List<?>>> getDataToBackUp() {
    final List<LiveData<List<?>>> data = new ArrayList<>();
    final BackupEntityGeneratedDao dao = localDb.getBackupEntityGeneratedDao();
    final Method[] methods = Objects.requireNonNull(dao.getClass()).getMethods();
    for (final Method method : methods) {
      if (StringUtils.startsWith(method.getName(), "_get")) {
        try {
          data.add((LiveData) method.invoke(dao));
        } catch (final IllegalAccessException | InvocationTargetException e) {
          Log.e(TAG, "failed to invoke method " + method, e);
        }
      }
    }
    return data;
  }

  /**
   * Task to read contents of remote database, make sure they are a subset of the contents
   * of the local database, and backup any local entries that are not present in remote.
   */
  private static class BackupTask<T> extends AsyncTask<Void, Void, Boolean> {

    /**
     * Reference to the remote database - for writing the data to. We don't read data from this
     * in doInBackground (we pass in the DataSnapshot instead) because
     * ValueEventListener::onDataChange would end up executing on the main thread.
     */
    private final DatabaseReference dbRef;

    /**
     * The current state of the remote database - for checking that the local data is a superset
     * of the remote data.
     */
    private final DataSnapshot dataSnapshot;

    /**
     * To inform caller of backup success or failure.
     */
    private final BackupTaskCallback callback;

    /**
     * The data to back up. Includes all data from this table, must be a superset of the data
     * currently stored remotely for this table.
     */
    private final List<T> localData;

    BackupTask(final DataSnapshot dataSnapshot, final DatabaseReference dbRef,
               final List<T> localData, final BackupTaskCallback callback) {
      this.dbRef = dbRef;
      this.dataSnapshot = dataSnapshot;
      this.callback = callback;
      this.localData = localData;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final Boolean doInBackground(final Void... voids) {
      // Get the entries in the remote database
      final GenericTypeIndicator<List<Map<String, Object>>> genericTypeIndicator =
        new GenericTypeIndicator<List<Map<String, Object>>>() {};

      final List<Map<String, Object>> snapshotValue = dataSnapshot.getValue(genericTypeIndicator);
      final List<T> remoteData = new ArrayList<>();
      // List is null if remote database is empty
      if (snapshotValue != null) {
        remoteData.addAll(deserialize(snapshotValue, (Class<T>) localData.get(0).getClass()));
      }
      return processBackup(localData, remoteData);
    }

    /**
     * Check that remoteData is a subset of localData, and if so, write any values present
     * in localData and absent in remoteData to the remote database.
     *
     * @param localData  locally saved data (array due to varargs)
     * @param remoteData remotely saved data
     * @return false if remoteData is not a subset of localData; true if backup succeeded
     */
    private boolean processBackup(final List<T> localData, final List<T> remoteData) {
      if (remoteData.size() > localData.size()) {
        Log.d(TAG, "Remote size " + remoteData.size() + ", local size " + localData.size());
        return false;
      }
      for (int i = 0; i < remoteData.size(); i++) {
        final T local = localData.get(i);
        final T remote = remoteData.get(i);
        if (!local.equals(remote)) {
          Log.d(TAG, "Local entity " + local + " does not match remote entity " + remote);
          return false;
        }
      }
      // Add the entries present locally but absent remotely to the remote database
      for (int i = 0; i < localData.size(); i++) {
        dbRef.child(String.valueOf(i)).setValue(localData.get(i));
      }
      return true;
    }

    /**
     * Convert the data in the specified maps to instances of the specified class. The keys in
     * the map must match the field names in the class.
     *
     * @param data   data for new object
     * @param tClass class of new object
     * @return a list of objects of the specified type
     */
    private List<T> deserialize(final List<Map<String, Object>> data, final Class<T> tClass) {
      final List<T> results = new ArrayList<>();
      for (final Map<String, Object> object : data) {
        final ObjectMapper om = new ObjectMapper();
        results.add(om.convertValue(object, tClass));
      }
      return results;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
      callback.resolve(success);
    }
  }

  private interface BackupTaskCallback {
    void resolve(boolean success);
  }

  /**
   * Restore the backup from the remote Firebase database. This will delete all local data for
   * entities annotated with @BackupEntity and replace it with data from the Firebase backup.
   */
  public void restore() {
    paused = true;
    remoteDb.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull final DataSnapshot snapshot) {
        // Load the data from the Firebase backup for this user. Firebase gives us this data in a
        // generic form using maps of field code to value for the entities which we deserialize
        // to Java types using Jackson. The outer map is @BackupEntity name to list of entities.
        final GenericTypeIndicator<Map<String, List<Map<String, Object>>>> genericTypeIndicator =
          new GenericTypeIndicator<Map<String, List<Map<String, Object>>>>() {};
        final Map<String, List<Map<String, Object>>> value =
          snapshot.getValue(genericTypeIndicator);
        // The snapshot value is null if there is no data for this user in the Firebase database.
        if (value != null) {
          // Deserialize the data to our native Java types
          final List<List<Object>> data = deserialize(value);
          // Sort by @BackupEntity.index()
          Collections.sort(data, new BackupEntityListComparator<>());
          // For each type of entity, wipe the current local data for this type and insert the
          // data retrieved from Firebase.
          final ExecutorService executor = Executors.newSingleThreadExecutor();
          executor.execute(() -> {
            for (final List<Object> objects : data) {
              final String type = objects.get(0).getClass().getSimpleName();
              final Method deleteMethod = findDaoMethod("_delete" + type);
              final Method insertMethod = findDaoMethod("_insert" + type);
              try {
                deleteMethod.invoke(localDb.getBackupEntityGeneratedDao());
                insertMethod.invoke(localDb.getBackupEntityGeneratedDao(), objects);
                paused = false;
              } catch (final IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
              }
            }
          });
        } else {
          paused = false;
          // TODO: handle empty snapshot?
        }
      }

      @Override
      public void onCancelled(@NonNull final DatabaseError error) {
        Log.e(TAG, error.getMessage(), error.toException());
        paused = false;
      }
    });
  }

  /**
   * Comparator for sorting List<List<Object>>s where the Objects are @BackupEntities to be sorted
   * by @BackupEntity.index(). It is assumed that the inner lists will all contain at least one
   * element and all elements will be the same type of @BackupEntity.
   */
  static class BackupEntityListComparator<T extends List<Object>> implements Comparator<T> {
    @Override
    public int compare(final List o1, final List o2) {
      // Find the @BackupEntity annotation for each
      final BackupEntity class1Annotation =
        Objects.requireNonNull(o1.get(0).getClass().getAnnotation(BackupEntity.class));
      final BackupEntity class2Annotation =
        Objects.requireNonNull(o2.get(0).getClass().getAnnotation(BackupEntity.class));
      // Sort by index on the annotation
      return Integer.compare(class1Annotation.index(), class2Annotation.index());
    }
  }

  /**
   * Convert the provided data to Java objects of the correct type.
   *
   * @param data map of @BackupEntity.name to a list of data of that entity type. Items in the list
   *             are maps where the key is the field name and the value is the value for that field
   * @return the deserialized data
   */
  private List<List<Object>> deserialize(
    final Map<String, List<Map<String, Object>>> data) {
    final ObjectMapper om = new ObjectMapper();
    final List<List<Object>> result = new ArrayList<>();
    // Get the types of data to convert to
    final Iterable<Class> classes = getTypesToBackUp();
    for (final Map.Entry<String, List<Map<String, Object>>> entityNode : data.entrySet()) {
      // Find the type for this entity based on the @BackupEntity.name
      final Class<?> aClass = findTypeForBackupEntityName(classes, entityNode.getKey());
      final List<Object> entities = new ArrayList<>();
      for (final Map<String, Object> entityData : entityNode.getValue()) {
        final Object entity = om.convertValue(entityData, aClass);
        if (entity != null) {
          entities.add(entity);
        }
      }
      result.add(entities);
    }
    return result;
  }

  /**
   * Find the @BackupEntity class from the specified list that has the specified value for name.
   *
   * @param classes         classes that are annotated with @BackupEntity
   * @param annotationName  the @BackupEntity.name value to look for
   * @return the class for the specified entity type
   */
  private Class<?> findTypeForBackupEntityName(
    final Iterable<Class> classes, final String annotationName) {
    for (final Class aClass : classes) {
      final BackupEntity annotation =
        Objects.requireNonNull((BackupEntity) aClass.getAnnotation(BackupEntity.class));
      if (StringUtils.equals(annotation.name(), annotationName)) {
        return aClass;
      }
    }
    throw new IllegalArgumentException("No BackupEntity class found for name " + annotationName);
  }

  /**
   * Find all classes in `com.wisebison.leide.model` annotated with @BackupEntity.
   *
   * @return all classes that can be backed up
   */
  private Iterable<Class> getTypesToBackUp() {
    final ArrayList<Class> classes = new ArrayList<>();
    try {
      final String packageCodePath = mainActivity.getPackageCodePath();
      final DexFile df = new DexFile(packageCodePath);
      final Enumeration<String> iter = df.entries();
      while (iter.hasMoreElements()) {
        final String className = iter.nextElement();
        if (className.contains("com.wisebison.leide.model")) {
          final Class<?> aClass = Class.forName(className);
          if (aClass.isAnnotationPresent(BackupEntity.class)) {
            classes.add(aClass);
          }
        }
      }
    } catch (final IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    return classes;
  }

  /**
   * Find the method with the specified name from the BackupEntityGeneratedDao.
   *
   * @param methodName name of method to find
   * @return the method in the DAO with that name
   */
  private Method findDaoMethod(final String methodName) {
    final BackupEntityGeneratedDao dao = localDb.getBackupEntityGeneratedDao();
    final Method[] methods = dao.getClass().getMethods();
    for (final Method method : methods) {
      if (StringUtils.equals(methodName, method.getName())) {
        return method;
      }
    }

    throw new IllegalArgumentException(
      "No method found in BackupEntityGeneratedDao with name " + methodName);
  }
}
