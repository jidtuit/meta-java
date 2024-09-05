package org.jid.metajava.model;

import java.util.List;
import java.util.SequencedCollection;
import java.util.Set;

public record MethodMeta(String name, String returnType, SequencedCollection<FieldMeta> params, Set<Modifier> modifiers,
                         Set<AnnotationMeta> annotations)
  implements AnnotationSupport, ModifierSupport {

}
