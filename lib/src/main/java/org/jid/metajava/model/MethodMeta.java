package org.jid.metajava.model;

import java.util.Set;

public record MethodMeta(String name, Set<AnnotationMeta> annotations) implements AnnotationSupport {

}
