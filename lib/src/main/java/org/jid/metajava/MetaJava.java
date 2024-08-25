package org.jid.metajava;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static org.jid.metajava.VisitorFactory.runAssignmentlVisitor;
import static org.jid.metajava.VisitorFactory.runClassVisitor;
import static org.jid.metajava.VisitorFactory.runLiteralVisitor;
import static org.jid.metajava.VisitorFactory.runMethodVisitor;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jid.metajava.model.AnnotationArgument;
import org.jid.metajava.model.AnnotationMeta;
import org.jid.metajava.model.ClassMeta;
import org.jid.metajava.model.ImportMeta;
import org.jid.metajava.model.MethodMeta;

public class MetaJava {

  private final CompilationUnitTreesFactory compilationUnitTreesFactory = new CompilationUnitTreesFactory();

  public Set<ClassMeta> getMetaFrom(Collection<File> files) throws IOException {
    if (files == null || files.isEmpty()) {
      throw new IllegalArgumentException("ERROR: Parameter files is null or empty");
    }

    Set<ClassMeta> classes = new HashSet<>();

    var compilationUnitTrees = compilationUnitTreesFactory.getCompilationUnitTrees(files);

    compilationUnitTrees.forEach(compilationUnitTree -> {
      var compilationUnitMeta = getCompilationUnitMeta(compilationUnitTree);
      compilationUnitTree.getTypeDecls().forEach(tree -> getClassMetas(tree, classes, compilationUnitMeta));
    });

    return classes;
  }


  private CompilationUnitMeta getCompilationUnitMeta(CompilationUnitTree compilationUnitTree) {
    String sourceFile = compilationUnitTree.getSourceFile().toUri().toString();
    String packageName = compilationUnitTree.getPackage().getPackageName().toString();
    List<ImportMeta> imports = compilationUnitTree.getImports().stream().map(this::parseImport).toList();
    return new CompilationUnitMeta(sourceFile, packageName, imports);
  }

  private ImportMeta parseImport(ImportTree importTree) {
    boolean isStatic = importTree.isStatic();
    int length = "import ".length();
    if (isStatic) {
      length += "static ".length();
    }
    var importName = importTree.toString().substring(length).replace(";", "").trim();
    return new ImportMeta(importName, isStatic);
  }

  private void getClassMetas(Tree tree, Set<ClassMeta> classes, CompilationUnitMeta compilationUnitMeta) {
    String sourceFile = compilationUnitMeta.sourceFile();
    String packageName = compilationUnitMeta.packageName();
    List<ImportMeta> imports = compilationUnitMeta.imports();

    runClassVisitor(tree, classes, (classTree, classesAcc) -> {
      var methodsOfAClass = new HashSet<MethodMeta>();
      classTree.getMembers().forEach(classMember -> getMethodMetas(classMember, methodsOfAClass));
      String className = classTree.getSimpleName().toString();
      Set<AnnotationMeta> annotations = getAnnotationMetas(classTree.getModifiers());
      classesAcc.add(new ClassMeta(className, unmodifiableSet(methodsOfAClass), annotations, packageName, sourceFile, imports));
      return null;
    });
  }

  private void getMethodMetas(Tree methodInfoTree, HashSet<MethodMeta> methods) {
    runMethodVisitor(methodInfoTree, methods, (methodTree, methodAcc) -> {
      var annotations = getAnnotationMetas(methodTree.getModifiers());
      String methodName = methodTree.getName().toString();
      methodAcc.add(new MethodMeta(methodName, annotations));
      return null;
    });
  }

  private Set<AnnotationMeta> getAnnotationMetas(ModifiersTree modifiersTree) {
    var annotations = new HashSet<AnnotationMeta>();
    modifiersTree.getAnnotations()
      .forEach(annotationTree -> {
        Set<AnnotationArgument> args = getAnnotationArguments(annotationTree);
        String annotationName = annotationTree.getAnnotationType().toString();
        annotations.add(new AnnotationMeta(annotationName, args));
      });
    return unmodifiableSet(annotations);
  }

  private Set<AnnotationArgument> getAnnotationArguments(AnnotationTree annotationTree) {
    return annotationTree.getArguments().stream()
      .map(this::parseAnnotationArg)
      .collect(toSet());
  }

  private AnnotationArgument parseAnnotationArg(ExpressionTree argTree) {
    if (argTree.getKind() == Kind.STRING_LITERAL) {
      String argValue = runLiteralVisitor(argTree, null, (literalTree, param) -> literalTree.getValue().toString());
      return new AnnotationArgument(null, argValue);
    }

    // argTree.getKind() == Kind.ASSIGNMENT
    return runAssignmentlVisitor(argTree, null, ((assignmentTree, param) -> {
      String argName = assignmentTree.getVariable().toString();
      String argValue = assignmentTree.getExpression().toString();
      return new AnnotationArgument(argName, argValue);
    }));

  }


  private record CompilationUnitMeta(String sourceFile, String packageName, List<ImportMeta> imports) {

  }

}
