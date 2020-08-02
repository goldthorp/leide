package com.goldthorp.wisebison.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldthorp.annotation.BackupEntity;
import com.goldthorp.wisebison.view.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

  private static final String TAG = "BACKUP";

  private final DatabaseReference remoteDb;
  private final AppDatabase localDb;
  private final MainActivity mainActivity;

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
   * Restore the backup from the remote Firebase database. This will delete all local data for
   * entities annotated with @BackupEntity and replace it with data from the Firebase backup.
   */
  public void restore() {
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
          // For each type of entity, wipe the current local data for this type and insert the
          // data retrieved from Firebase.
          for (final List<Object> objects : data) {
            final String type = objects.get(0).getClass().getSimpleName();
            final Method deleteMethod = findDaoMethod("_delete" + type);
            final Method insertMethod = findDaoMethod("_insert" + type);
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
              try {
                deleteMethod.invoke(localDb.getBackupEntityGeneratedDao());
                insertMethod.invoke(localDb.getBackupEntityGeneratedDao(), objects);
              } catch (final IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
              }
            });
          }
        } else {
          // TODO: handle empty snapshot?
        }
      }

      @Override
      public void onCancelled(@NonNull final DatabaseError error) {
        Log.e(TAG, error.getMessage(), error.toException());
      }
    });
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
        entities.add(om.convertValue(entityData, aClass));
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
   * Find all classes in `com.goldthorp.wisebison.model` annotated with @BackupEntity.
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
        if (className.contains("com.goldthorp.wisebison.model")) {
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
    final Method[] methods = Objects.requireNonNull(dao.getClass()).getMethods();
    for (final Method method : methods) {
      if (StringUtils.equals(methodName, method.getName())) {
        return method;
      }
    }

    throw new IllegalArgumentException(
      "No method found in BackupEntityGeneratedDao with name " + methodName);
  }
}
