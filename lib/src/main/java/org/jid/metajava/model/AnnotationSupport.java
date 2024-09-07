package org.jid.metajava.model;

import java.util.Set;

public sealed interface AnnotationSupport permits ClassMeta, MethodMeta, VariableMeta {

  Set<AnnotationMeta> annotations();

}
