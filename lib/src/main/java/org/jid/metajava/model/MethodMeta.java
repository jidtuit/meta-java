package org.jid.metajava.model;

import java.util.SequencedCollection;
import java.util.Set;

public record MethodMeta(String name, String returnType, SequencedCollection<VariableMeta> params, Set<String> exceptions,
                         Set<Modifier> modifiers, Set<AnnotationMeta> annotations) implements AnnotationSupport, ModifierSupport {

}
