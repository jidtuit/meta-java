package org.jid.metajava;

import static org.jid.metajava.VisitorFactory.runMethodVisitor;

import com.sun.source.tree.Tree;
import java.util.HashSet;
import java.util.Set;
import org.jid.metajava.model.MethodMeta;
import org.jid.metajava.model.Modifier;
import org.jid.metajava.model.ModifierSupport;

class MethodProcessor {

  private final AnnotationProcessor annotationProcessor;
  private final ModifierProcessor modifierProcessor;

  MethodProcessor(AnnotationProcessor annotationProcessor, ModifierProcessor modifierProcessor) {
    this.annotationProcessor = annotationProcessor;
    this.modifierProcessor = modifierProcessor;
  }

  public void getMetas(Tree methodInfoTree, Set<MethodMeta> methods) {
    runMethodVisitor(methodInfoTree, methods, (methodTree, methodAcc) -> {
      String methodName = methodTree.getName().toString();
      Set<Modifier> modifierFlags = modifierProcessor.getModifierFlags(methodTree.getModifiers());
      var annotations = annotationProcessor.getMetas(methodTree.getModifiers());

      methodAcc.add(new MethodMeta(methodName, modifierFlags, annotations));
      return null;
    });
  }

}
