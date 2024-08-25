package org.jid.metajava.model;

public enum ClassType {
  CLASS, INTERFACE, RECORD, ENUM, ANNOTATION;

  public static ClassType from(String rawClassType) {
    return switch (rawClassType) {
      case "CLASS" -> CLASS;
      case "INTERFACE" -> INTERFACE;
      case "RECORD" -> RECORD;
      case "ENUM" -> ENUM;
      case "ANNOTATION_TYPE" -> ANNOTATION;
      case null, default -> throw new IllegalArgumentException("Type " + rawClassType + " not supported");
    };
  }

}
