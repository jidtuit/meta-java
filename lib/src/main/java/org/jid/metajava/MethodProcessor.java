package org.jid.metajava;

import static org.jid.metajava.VisitorFactory.runMethodVisitor;

import com.sun.source.tree.Tree;
import java.util.HashSet;
import java.util.Set;
import org.jid.metajava.model.MethodMeta;

class MethodProcessor {

  private final AnnotationProcessor annotationProcessor;

  MethodProcessor(AnnotationProcessor annotationProcessor) {
    this.annotationProcessor = annotationProcessor;
  }

  public void getMetas(Tree methodInfoTree, Set<MethodMeta> methods) {
    runMethodVisitor(methodInfoTree, methods, (methodTree, methodAcc) -> {
      var annotations = annotationProcessor.getMetas(methodTree.getModifiers());
      String methodName = methodTree.getName().toString();
      methodAcc.add(new MethodMeta(methodName, annotations));
      return null;
    });
  }

}
