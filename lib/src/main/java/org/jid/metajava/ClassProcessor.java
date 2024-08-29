package org.jid.metajava;

import static com.sun.source.tree.Tree.Kind.CLASS;
import static com.sun.source.tree.Tree.Kind.INTERFACE;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.jid.metajava.VisitorFactory.runClassVisitor;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.jid.metajava.model.AnnotationMeta;
import org.jid.metajava.model.ClassMeta;
import org.jid.metajava.model.ClassType;
import org.jid.metajava.model.ImportMeta;
import org.jid.metajava.model.MethodMeta;

class ClassProcessor {

  private final MethodProcessor methodProcessor;
  private final AnnotationProcessor annotationProcessor;

  ClassProcessor(MethodProcessor methodProcessor, AnnotationProcessor annotationProcessor) {
    this.methodProcessor = methodProcessor;
    this.annotationProcessor = annotationProcessor;
  }

  public void getMetas(Tree tree, Set<ClassMeta> classes, CompilationUnitMeta compilationUnitMeta) {
    String sourceFile = compilationUnitMeta.sourceFile();
    String packageName = compilationUnitMeta.packageName();
    Set<ImportMeta> imports = compilationUnitMeta.imports();

    runClassVisitor(tree, classes, (classTree, classesAcc) -> {
      var methodsOfAClass = new HashSet<MethodMeta>();
      classTree.getMembers().forEach(classMember -> methodProcessor.getMetas(classMember, methodsOfAClass));
      String className = classTree.getSimpleName().toString();
      Set<AnnotationMeta> annotations = annotationProcessor.getMetas(classTree.getModifiers());
      var classType = ClassType.from(classTree.getKind().name());
      Set<String> extendsFrom = getExtendsFrom(classTree);

      classesAcc.add(
        new ClassMeta(className, classType, unmodifiableSet(methodsOfAClass), annotations, packageName, sourceFile, imports, extendsFrom)
      );
      return null;
    });
  }

  private static Set<String> getExtendsFrom(ClassTree classTree) {
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

}
