package org.jid.metajava.model;

public enum Modifier {
  PUBLIC,
  PROTECTED,
  PRIVATE,
  ABSTRACT,
  DEFAULT,
  STATIC,
  SEALED,
  NON_SEALED,
  FINAL,
  TRANSIENT,
  VOLATILE,
  SYNCHRONIZED,
  NATIVE,
  STRICTFP;

  public static Modifier from(String modifier) {
    return switch (modifier) {
      case "public" -> PUBLIC;
      case "protected" -> PROTECTED;
      case "private" -> PRIVATE;
      case "abstract" -> ABSTRACT;
      case "static" -> STATIC;
      case "sealed" -> SEALED;
      case "non-sealed" -> NON_SEALED;
      case "final" -> FINAL;
      case "transient" -> TRANSIENT;
      case "volatile" -> VOLATILE;
      case "synchronized" -> SYNCHRONIZED;
      case "native" -> NATIVE;
      case "strictfp" -> STRICTFP;
      case "default" -> DEFAULT;
      case null -> throw new IllegalArgumentException("Unknown modifier " + modifier);
      default -> throw new IllegalArgumentException("Unknown modifier " + modifier);
    };
  }

}
