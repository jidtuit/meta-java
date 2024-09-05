package org.jid.metajava.model;

import java.util.Set;

public record VariableMeta(String name, String type, String initializer, Set<Modifier> modifiers,
                           Set<AnnotationMeta> annotations) implements AnnotationSupport, ModifierSupport {

}
