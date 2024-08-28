package org.jid.metajava;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jid.metajava.model.ClassMeta;

public class MetaJava {

  private final CompilationUnitTreesFactory compilationUnitTreesFactory = new CompilationUnitTreesFactory();
  private final CompilationUnitMetaProcessor compilationUnitMetaProcessor = new CompilationUnitMetaProcessor();
  private final ClassProcessor classProcessor;

  public MetaJava() {
    var annotationProcessor = new AnnotationProcessor();
    var methodProcessor = new MethodProcessor(annotationProcessor);
    classProcessor = new ClassProcessor(methodProcessor, annotationProcessor);
  }

  public Set<ClassMeta> getMetaFrom(Collection<File> files) {
    if (files == null || files.isEmpty()) {
      throw new IllegalArgumentException("ERROR: Parameter files is null or empty");
    }

    var compilationUnitTrees = compilationUnitTreesFactory.getCompilationUnitTrees(files);

    Set<ClassMeta> classes = new HashSet<>();
    compilationUnitTrees.forEach(compilationUnitTree -> {
      var compilationUnitMeta = compilationUnitMetaProcessor.getMeta(compilationUnitTree);
      compilationUnitTree.getTypeDecls().forEach(tree -> classProcessor.getMetas(tree, classes, compilationUnitMeta));
    });

    return classes;
  }

}
