package org.jid.metajava.model;

import java.util.List;
import java.util.Set;

public record ClassMeta(String name, ClassType type, Set<MethodMeta> methods, Set<AnnotationMeta> annotations, String packageName,
                        String sourceFileUri,
                        List<ImportMeta> imports, Set<String> extendsFrom) implements Annotationable {

}
