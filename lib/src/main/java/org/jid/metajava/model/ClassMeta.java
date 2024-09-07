package org.jid.metajava.model;

import java.util.Set;

public record ClassMeta(String name, ClassType type, Set<MethodMeta> methods, Set<AnnotationMeta> annotations, String packageName,
                        String sourceFileUri, Set<ImportMeta> imports, Set<String> extendsFrom,
                        Set<String> implementsFrom, Set<VariableMeta> fields, Set<MethodMeta> constructors,
                        Set<ClassMeta> nestedClasses, Set<Modifier> modifiers) implements AnnotationSupport, ModifierSupport {

}
