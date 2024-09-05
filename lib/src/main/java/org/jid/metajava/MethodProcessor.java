package org.jid.metajava;

import static java.util.Collections.unmodifiableSequencedCollection;
import static org.jid.metajava.VisitorFactory.runMethodVisitor;

import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Set;
import org.jid.metajava.model.FieldMeta;
import org.jid.metajava.model.MethodMeta;
import org.jid.metajava.model.Modifier;

class MethodProcessor {

  private final AnnotationProcessor annotationProcessor;
  private final ModifierProcessor modifierProcessor;
  private final FieldProcessor fieldProcessor;

  MethodProcessor(AnnotationProcessor annotationProcessor, ModifierProcessor modifierProcessor, FieldProcessor fieldProcessor) {
    this.annotationProcessor = annotationProcessor;
    this.modifierProcessor = modifierProcessor;
    this.fieldProcessor = fieldProcessor;
  }

  public void getMetas(Tree methodInfoTree, Set<MethodMeta> methods) {
    runMethodVisitor(methodInfoTree, methods, (methodTree, methodAcc) -> {
      //  methodTree.getReturnType() is null in constructors which are currently NOT supported
      if (methodTree.getReturnType() == null) {
        return null;
      }

      String methodName = methodTree.getName().toString();
      String returnType = methodTree.getReturnType().toString();
      var parameters = new ArrayList<FieldMeta>();
      methodTree.getParameters().forEach(param -> fieldProcessor.getMetas(param, parameters));
      Set<Modifier> modifierFlags = modifierProcessor.getModifierFlags(methodTree.getModifiers());
      var annotations = annotationProcessor.getMetas(methodTree.getModifiers());

      methodAcc.add(new MethodMeta(methodName, returnType, unmodifiableSequencedCollection(parameters), modifierFlags, annotations));
      return null;
    });
  }

}
