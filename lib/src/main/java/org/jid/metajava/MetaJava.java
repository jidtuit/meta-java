package org.jid.metajava;

import static java.util.Collections.unmodifiableSet;
import static org.jid.metajava.VisitorFactory.runClassVisitor;
import static org.jid.metajava.VisitorFactory.runMethodVisitor;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    for (CompilationUnitTree compilationUnitTree : compilationUnitTrees) {
      var compilationUnitMeta = getCompilationUnitMeta(compilationUnitTree);
      for (Tree tree : compilationUnitTree.getTypeDecls()) {
        getClassMetas(tree, classes, compilationUnitMeta);
      }
    }

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
      classesAcc.add(new ClassMeta(className, unmodifiableSet(methodsOfAClass), packageName, sourceFile, imports));
      return null;
    });
  }

  private void getMethodMetas(Tree methodInfoTree, HashSet<MethodMeta> methods) {
    runMethodVisitor(methodInfoTree, methods, (methodTree, methodAcc) -> {
      var annotations = getAnnotationMetas(methodTree);
      String methodName = methodTree.getName().toString();
      methodAcc.add(new MethodMeta(methodName, annotations));
      return null;
    });
  }

  private Set<AnnotationMeta> getAnnotationMetas(MethodTree methodTree) {
    var annotations = new HashSet<AnnotationMeta>();
    methodTree.getModifiers().getAnnotations()
      .forEach(annotationTree -> {
        Set<String> args = annotationTree.getArguments().stream().map(ExpressionTree::toString).collect(Collectors.toSet());
        String annotationName = annotationTree.getAnnotationType().toString();
        annotations.add(new AnnotationMeta(annotationName, args));
      });
    return unmodifiableSet(annotations);
  }

  private record CompilationUnitMeta(String sourceFile, String packageName, List<ImportMeta> imports){}

}
