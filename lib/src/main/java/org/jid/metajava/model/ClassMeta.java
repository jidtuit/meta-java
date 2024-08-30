package org.jid.metajava.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record ClassMeta(String name, ClassType type, Set<MethodMeta> methods, Set<AnnotationMeta> annotations, String packageName,
                        String sourceFileUri, Set<ImportMeta> imports, Set<String> extendsFrom,
                        Set<String> implementsFrom) implements Annotationable {

  public ClassMeta {
    List.of(name, type, sourceFileUri).forEach(Objects::requireNonNull);

    switch (type) {
      case CLASS -> validateClassExtendsFrom(extendsFrom);
      case RECORD, ENUM -> validateNotSupportExtendsFrom(extendsFrom);
      case INTERFACE -> validateNotSupportImplementsFrom(implementsFrom);
      case ANNOTATION -> {
        validateNotSupportExtendsFrom(extendsFrom);
        validateNotSupportImplementsFrom(implementsFrom);
      }
    }

  }

  private void validateClassExtendsFrom(Set<String> extendsFrom) {
    if (extendsFrom != null && extendsFrom.size() > 1) {
      throw new IllegalArgumentException("A class cannot extend from more than one class");
    }
  }

  private void validateNotSupportExtendsFrom(Set<String> extendsFrom) {
    if (extendsFrom != null && !extendsFrom.isEmpty()) {
      throw new IllegalArgumentException("Inheritance not supported for this type of class: " + type);
    }
  }

  private void validateNotSupportImplementsFrom(Set<String> implementsFrom) {
    if (implementsFrom != null && !implementsFrom.isEmpty()) {
      throw new IllegalArgumentException("Inheritance not supported for this type of class: " + type);
    }
  }

}
