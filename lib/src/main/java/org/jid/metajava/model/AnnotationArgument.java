package org.jid.metajava.model;

public record AnnotationArgument(String name, String value) {

  public boolean hasName() {
    return name != null && !name.isBlank();
  }
}
