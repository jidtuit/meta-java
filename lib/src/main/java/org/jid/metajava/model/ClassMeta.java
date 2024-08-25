package org.jid.metajava.model;

import java.util.List;
import java.util.Set;

public record ClassMeta(String name, Set<MethodMeta> methods, Set<AnnotationMeta> annotations, String packageName, String sourceFileUri,
                        List<ImportMeta> imports) implements Annotationable {

}
