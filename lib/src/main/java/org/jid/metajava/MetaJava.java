package org.jid.metajava;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jid.metajava.model.ClassMeta;

public class MetaJava {

  private final CompilationUnitTreeFactory compilationUnitTreeFactory = new CompilationUnitTreeFactory();
  private final CompilationUnitMetaProcessor compilationUnitMetaProcessor = new CompilationUnitMetaProcessor();
  private final ClassProcessor classProcessor;

  public MetaJava() {
    var annotationProcessor = new AnnotationProcessor();
    var modifierProcessor = new ModifierProcessor();
    var variableProcessor = new VariableProcessor(annotationProcessor, modifierProcessor);
    var methodProcessor = new MethodProcessor(annotationProcessor, modifierProcessor, variableProcessor);
    classProcessor = new ClassProcessor(methodProcessor, annotationProcessor, variableProcessor, modifierProcessor);
  }

  public Set<ClassMeta> getMetaFrom(Collection<File> files) {
    if (files == null || files.isEmpty()) {
      throw new IllegalArgumentException("ERROR: Parameter files is null or empty");
    }

    var compilationUnitTrees = compilationUnitTreeFactory.getCompilationUnitTrees(files);

    Set<ClassMeta> classes = new HashSet<>();
    compilationUnitTrees.forEach(compilationUnitTree -> {
      var compilationUnitMeta = compilationUnitMetaProcessor.getMeta(compilationUnitTree);
      compilationUnitTree.getTypeDecls().forEach(tree -> classProcessor.getMetas(tree, classes, compilationUnitMeta));
    });

    return classes;
  }

}
