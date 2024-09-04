package org.jid.metajava.model;

import java.util.Set;

public record MethodMeta(String name, String returnType, Set<Modifier> modifiers, Set<AnnotationMeta> annotations)
  implements AnnotationSupport, ModifierSupport {

}
