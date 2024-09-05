package org.jid.metajava;

import static java.util.Collections.unmodifiableSequencedCollection;
import static org.jid.metajava.VisitorFactory.runMethodVisitor;

import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jid.metajava.model.VariableMeta;
import org.jid.metajava.model.MethodMeta;
import org.jid.metajava.model.Modifier;

class MethodProcessor {

  private final AnnotationProcessor annotationProcessor;
  private final ModifierProcessor modifierProcessor;
  private final VariableProcessor variableProcessor;

  MethodProcessor(AnnotationProcessor annotationProcessor, ModifierProcessor modifierProcessor, VariableProcessor variableProcessor) {
    this.annotationProcessor = annotationProcessor;
    this.modifierProcessor = modifierProcessor;
    this.variableProcessor = variableProcessor;
  }

  public void getMetas(Tree methodInfoTree, Collection<MethodMeta> methods) {
    runMethodVisitor(methodInfoTree, methods, (methodTree, methodAcc) -> {
      //  methodTree.getReturnType() is null in constructors which are currently NOT supported
      if (methodTree.getReturnType() == null) {
        return null;
      }

      String methodName = methodTree.getName().toString();
      String returnType = methodTree.getReturnType().toString();
      var parameters = new ArrayList<VariableMeta>();
      methodTree.getParameters().forEach(param -> variableProcessor.getMetas(param, parameters));
      Set<String> exceptions = methodTree.getThrows().stream().map(Object::toString).collect(Collectors.toUnmodifiableSet());
      Set<Modifier> modifierFlags = modifierProcessor.getModifierFlags(methodTree.getModifiers());
      var annotations = annotationProcessor.getMetas(methodTree.getModifiers());

      methodAcc.add(
        new MethodMeta(methodName, returnType, unmodifiableSequencedCollection(parameters), exceptions, modifierFlags, annotations));
      return null;
    });
  }

}
