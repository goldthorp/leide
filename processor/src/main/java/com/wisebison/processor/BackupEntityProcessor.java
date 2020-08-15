package com.wisebison.processor;

import androidx.room.Entity;

import com.wisebison.annotation.BackupEntity;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Process @BackupEntity annotations to validate usages and generate the BackupEntityGeneratedDao
 * which is used to read/write the local database from com.wisebison.leide.Backup
 */
@SupportedAnnotationTypes("com.wisebison.annotation.BackupEntity")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BackupEntityProcessor extends AbstractProcessor {
  @Override
  public boolean process(
    final Set<? extends TypeElement> set, final RoundEnvironment roundEnvironment) {
    // Find all @BackupEntity classes
    final Set<? extends Element> elements =
      roundEnvironment.getElementsAnnotatedWith(BackupEntity.class);
    final Set<TypeElement> types = ElementFilter.typesIn(elements);

    // Validate uses of @BackupEntity and create list of entity info for the velocity template
    final List<BackupEntityForm> backupEntityForms = new ArrayList<>();
    for (final TypeElement type : types) {
      final String backupEntityName = type.getAnnotation(BackupEntity.class).name();
      final Entity entityAnnotation = type.getAnnotation(Entity.class);
      if (entityAnnotation == null) {
        throw new RuntimeException("@BackupEntity " + backupEntityName + " is not an @Entity");
      }

      if (!StringUtils.equals(entityAnnotation.tableName(), backupEntityName)) {
        throw new RuntimeException("@Entity tableName " + entityAnnotation.tableName()
          + " does not match @BackupEntity name " + backupEntityName);
      }

      backupEntityForms.add(new BackupEntityForm(type.getSimpleName().toString(),
        type.getQualifiedName().toString(), entityAnnotation.tableName()));
    }

    // Initialize velocity
    final VelocityEngine velocityEngine = new VelocityEngine();
    velocityEngine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
      "./processor/src/main/resources/template");
    velocityEngine.init();

    // Put data for substitutions
    final VelocityContext velocityContext = new VelocityContext();
    velocityContext.internalPut("entities", backupEntityForms);

    // Find template
    final Template template = velocityEngine.getTemplate("dao.vm");

    // Generate java
    try {
      final JavaFileObject javaFileObject = processingEnv.getFiler()
        .createSourceFile("com.wisebison.leide.data.BackupEntityGeneratedDao");
      final Writer writer = javaFileObject.openWriter();
      template.merge(velocityContext, writer);
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Getter
  @AllArgsConstructor
  @SuppressWarnings("WeakerAccess")
  public class BackupEntityForm {
    private final String className;
    private final String qualifiedClassName;
    private final String tableName;
  }
}
