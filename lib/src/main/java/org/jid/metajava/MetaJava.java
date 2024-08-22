package org.jid.metajava;

import static com.sun.source.tree.Tree.Kind.METHOD;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.unmodifiableSet;
import static org.jid.metajava.VisitorFactory.runClassVisitor;
import static org.jid.metajava.VisitorFactory.runMethodVisitor;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SimpleTreeVisitor;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.jid.metajava.model.AnnotationMeta;
import org.jid.metajava.model.ClassMeta;
import org.jid.metajava.model.ImportMeta;
import org.jid.metajava.model.MethodMeta;

public class MetaJava {

  public Set<ClassMeta> getMetaFrom(Collection<File> files) throws IOException {
    if (files == null || files.isEmpty()) {
      throw new IllegalArgumentException("Parameter files is null or empty");
    }

    Set<ClassMeta> classes = new HashSet<>();

    var compilationUnitTrees = getCompilationUnitTrees(files);

    for (CompilationUnitTree compilationUnitTree : compilationUnitTrees) {
      var compilationUnitMeta = getCompilationUnitMeta(compilationUnitTree);
      for (Tree tree : compilationUnitTree.getTypeDecls()) {
        getClassMetas(tree, classes, compilationUnitMeta);
      }
    }

    return classes;
  }

  private Iterable<? extends CompilationUnitTree> getCompilationUnitTrees(Collection<File> files) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, UTF_8);
    Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);

    // "-proc:full" compiler option needed to be able to process annotations
    JavacTask javacTask =
      (JavacTask) compiler.getTask(null, fileManager, null, List.of("-proc:full"), null, compilationUnits);

    return javacTask.parse();
  }

  private CompilationUnitMeta getCompilationUnitMeta(CompilationUnitTree compilationUnitTree) {
    String sourceFile = compilationUnitTree.getSourceFile().toUri().toString();
    String packageName = compilationUnitTree.getPackage().getPackageName().toString();
    List<ImportMeta> imports = compilationUnitTree.getImports().stream().map(this::parseImport).toList();
    return new CompilationUnitMeta(sourceFile, packageName, imports);
  }

  private void getClassMetas(Tree tree, Set<ClassMeta> classes, CompilationUnitMeta compilationUnitMeta) {
    String sourceFile = compilationUnitMeta.sourceFile();
    String packageName = compilationUnitMeta.packageName();
    List<ImportMeta> imports = compilationUnitMeta.imports();

    runClassVisitor(tree, classes, (classTree, classesAcc) -> {
      var methodsOfAClass = new HashSet<MethodMeta>();
      classTree.getMembers().forEach(classMember -> getMethodMetas(classMember, methodsOfAClass));
      classesAcc.add(new ClassMeta(classTree.getSimpleName().toString(), unmodifiableSet(methodsOfAClass), packageName, sourceFile, imports));
      return null;
    });
  }

  private void getMethodMetas(Tree methodInfoTree, HashSet<MethodMeta> methods) {
    runMethodVisitor(methodInfoTree, methods, (methodTree, methodAcc) -> {
      var annotations = getAnnotationMetas(methodTree);
      methodAcc.add(new MethodMeta(methodTree.getName().toString(), annotations));
      return null;
    });
  }

  private Set<AnnotationMeta> getAnnotationMetas(MethodTree methodTree) {
    var annotations = new HashSet<AnnotationMeta>();
    methodTree.getModifiers().getAnnotations()
      .forEach(annotationTree -> {
        Set<String> args = annotationTree.getArguments().stream().map(ExpressionTree::toString).collect(Collectors.toSet());
        annotations.add(new AnnotationMeta(annotationTree.getAnnotationType().toString(), args));
      });
    return unmodifiableSet(annotations);
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

  private record CompilationUnitMeta(String sourceFile, String packageName, List<ImportMeta> imports){}

}
