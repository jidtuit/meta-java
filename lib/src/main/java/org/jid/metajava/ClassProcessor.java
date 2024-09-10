package org.jid.metajava;


import static com.sun.source.tree.Tree.Kind.*;
import static com.sun.source.tree.Tree.Kind.CLASS;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.jid.metajava.VisitorFactory.runClassVisitor;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.jid.metajava.model.AnnotationMeta;
import org.jid.metajava.model.ClassMeta;
import org.jid.metajava.model.ClassType;
import org.jid.metajava.model.Modifier;
import org.jid.metajava.model.VariableMeta;
import org.jid.metajava.model.ImportMeta;
import org.jid.metajava.model.MethodMeta;

class ClassProcessor {

  private static final Set<Kind> SUPPORT_IMPLEMENTS = Set.of(CLASS, RECORD, ENUM);
  private static final Set<Kind> SUPPORT_NESTED_CLASSES = Set.of(CLASS, RECORD, ENUM, INTERFACE, ANNOTATION_TYPE);

  private final MethodProcessor methodProcessor;
  private final AnnotationProcessor annotationProcessor;
  private final VariableProcessor variableProcessor;
  private final ModifierProcessor modifierProcessor;

  ClassProcessor(MethodProcessor methodProcessor, AnnotationProcessor annotationProcessor, VariableProcessor variableProcessor,
    ModifierProcessor modifierProcessor) {
    this.methodProcessor = methodProcessor;
    this.annotationProcessor = annotationProcessor;
    this.variableProcessor = variableProcessor;
    this.modifierProcessor = modifierProcessor;
  }

  public void getMetas(Tree tree, Set<ClassMeta> classes, CompilationUnitMeta compilationUnitMeta) {
    String sourceFile = compilationUnitMeta.sourceFile();
    String packageName = compilationUnitMeta.packageName();
    Set<ImportMeta> imports = compilationUnitMeta.imports();

    runClassVisitor(tree, classes, (classTree, classesAcc) -> {
      String className = classTree.getSimpleName().toString();
      var classType = ClassType.from(classTree.getKind().name());

      var methodsOfAClass = new HashSet<MethodMeta>();
      var fieldsOfAClass = new HashSet<VariableMeta>();
      var nestedClassesOfAClass = new HashSet<ClassMeta>();

      classTree.getMembers().forEach(classMember -> {
        if (classMember.getKind() == METHOD) {
          methodProcessor.getMetas(classMember, methodsOfAClass);
        } else if (classMember.getKind() == VARIABLE) {
          variableProcessor.getMetas(classMember, fieldsOfAClass);
        } else if (SUPPORT_NESTED_CLASSES.contains(classMember.getKind())) {
          String nestedPackageName = compilationUnitMeta.packageName() + "." + className;
          var nestedCompilationUnitMeta = new CompilationUnitMeta(compilationUnitMeta.sourceFile(), nestedPackageName,
            compilationUnitMeta.imports());
          // New ClassProcessor instance to avoid stackoverflow because of recursion
          new ClassProcessor(methodProcessor, annotationProcessor, variableProcessor, modifierProcessor).getMetas(classMember,
            nestedClassesOfAClass, nestedCompilationUnitMeta);
        }
      });

      Map<Boolean, Set<MethodMeta>> methodsByType = methodsOfAClass.stream()
        .collect(groupingBy(MethodMeta::isConstructor, mapping(i -> i, toSet())));
      Set<MethodMeta> methods = unmodifiableSet(methodsByType.getOrDefault(false, Set.of()));
      Set<MethodMeta> constructors = unmodifiableSet(methodsByType.getOrDefault(true, Set.of()));

      Set<AnnotationMeta> annotations = annotationProcessor.getMetas(classTree.getModifiers());
      Set<String> extendsFrom = getExtendsFrom(classTree);
      Set<String> implementsFrom = getImplementsFrom(classTree);
      Set<Modifier> modifierFlags = modifierProcessor.getModifierFlags(classTree.getModifiers());
      Set<String> permits = classTree.getPermitsClause() == null ? Set.of()
        : classTree.getPermitsClause().stream().map(Object::toString).collect(toUnmodifiableSet());

      classesAcc.add(
        new ClassMeta(className, classType, methods, annotations, packageName, sourceFile, imports, extendsFrom, implementsFrom,
          unmodifiableSet(fieldsOfAClass), constructors, unmodifiableSet(nestedClassesOfAClass), modifierFlags, permits)
      );
      return null;
    });
  }

  private Set<String> getExtendsFrom(ClassTree classTree) {
    if (classTree.getKind() != CLASS && classTree.getKind() != INTERFACE) {
      return Set.of();
    }

    if (classTree.getKind() == CLASS) {
      return classTree.getExtendsClause() == null ? Set.of() : Set.of(classTree.getExtendsClause().toString());
    }
    // Kind == INTERFACE
    if (classTree.getImplementsClause() == null || classTree.getImplementsClause().toString().isBlank()) {
      return Set.of();
    }

    var interfaceExtendsRaw = classTree.getImplementsClause().toString().split(",");
    return Stream.of(interfaceExtendsRaw).map(String::trim).collect(toUnmodifiableSet());
  }

  private Set<String> getImplementsFrom(ClassTree classTree) {
    if (!SUPPORT_IMPLEMENTS.contains(classTree.getKind())) {
      return Set.of();
    }

    if (classTree.getImplementsClause() == null || classTree.getImplementsClause().toString().isBlank()) {
      return Set.of();
    }

    var implementsFromRaw = classTree.getImplementsClause().toString().split(",");
    return Stream.of(implementsFromRaw).map(String::trim).collect(toUnmodifiableSet());
  }

}
