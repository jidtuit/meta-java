package org.jid.metajava;


import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.jid.metajava.VisitorFactory.runClassVisitor;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.jid.metajava.model.AnnotationMeta;
import org.jid.metajava.model.ClassMeta;
import org.jid.metajava.model.ClassType;
import org.jid.metajava.model.FieldMeta;
import org.jid.metajava.model.ImportMeta;
import org.jid.metajava.model.MethodMeta;

class ClassProcessor {

  private static final Set<Kind> SUPPORT_IMPLEMENTS = Set.of(Kind.CLASS, Kind.RECORD, Kind.ENUM);

  private final MethodProcessor methodProcessor;
  private final AnnotationProcessor annotationProcessor;
  private final FieldProcessor fieldProcessor;

  ClassProcessor(MethodProcessor methodProcessor, AnnotationProcessor annotationProcessor, FieldProcessor fieldProcessor) {
    this.methodProcessor = methodProcessor;
    this.annotationProcessor = annotationProcessor;
    this.fieldProcessor = fieldProcessor;
  }

  public void getMetas(Tree tree, Set<ClassMeta> classes, CompilationUnitMeta compilationUnitMeta) {
    String sourceFile = compilationUnitMeta.sourceFile();
    String packageName = compilationUnitMeta.packageName();
    Set<ImportMeta> imports = compilationUnitMeta.imports();

    runClassVisitor(tree, classes, (classTree, classesAcc) -> {
      var methodsOfAClass = new HashSet<MethodMeta>();
      Set<FieldMeta> fieldsOfAClass = new HashSet<>();
      classTree.getMembers().forEach(classMember -> {
        if (classMember.getKind() == Kind.METHOD) {
          methodProcessor.getMetas(classMember, methodsOfAClass);
        } else if (classMember.getKind() == Kind.VARIABLE) {
          fieldProcessor.getMetas(classMember, fieldsOfAClass);
        }
      });
      String className = classTree.getSimpleName().toString();
      Set<AnnotationMeta> annotations = annotationProcessor.getMetas(classTree.getModifiers());
      var classType = ClassType.from(classTree.getKind().name());
      Set<String> extendsFrom = getExtendsFrom(classTree);
      Set<String> implementsFrom = getImplementsFrom(classTree);

      classesAcc.add(
        new ClassMeta(className, classType, unmodifiableSet(methodsOfAClass), annotations, packageName, sourceFile, imports, extendsFrom,
          implementsFrom, unmodifiableSet(fieldsOfAClass))
      );
      return null;
    });
  }

  private Set<String> getExtendsFrom(ClassTree classTree) {
    if (classTree.getKind() != Kind.CLASS && classTree.getKind() != Kind.INTERFACE) {
      return Set.of();
    }

    if (classTree.getKind() == Kind.CLASS) {
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
