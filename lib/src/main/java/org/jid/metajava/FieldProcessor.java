package org.jid.metajava;

import static org.jid.metajava.VisitorFactory.runVariableVisitor;

import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.util.Collection;
import java.util.Set;
import org.jid.metajava.model.AnnotationMeta;
import org.jid.metajava.model.FieldMeta;
import org.jid.metajava.model.Modifier;

class FieldProcessor {

  private final AnnotationProcessor annotationProcessor;
  private final ModifierProcessor modifierProcessor;

  FieldProcessor(AnnotationProcessor annotationProcessor, ModifierProcessor modifierProcessor) {
    this.annotationProcessor = annotationProcessor;
    this.modifierProcessor = modifierProcessor;
  }

  public void getMetas(Tree fieldInfoTree, Collection<FieldMeta> fields) {

    runVariableVisitor(fieldInfoTree, fields, ((variableTree, fieldAcc) -> {
      String name = variableTree.getName().toString();
      String type = variableTree.getType().toString();
      String initialValue = getInitialValue(variableTree);
      Set<Modifier> modifiers = modifierProcessor.getModifierFlags(variableTree.getModifiers());
      Set<AnnotationMeta> annotations = annotationProcessor.getMetas(variableTree.getModifiers());

      fieldAcc.add(new FieldMeta(name, type, initialValue, modifiers, annotations));
      return null;
    }));

  }

  private static String getInitialValue(VariableTree variableTree) {
    if (variableTree.getInitializer() == null) {
      return null;
    }
    String intializer = variableTree.getInitializer().toString();
    if (intializer.startsWith("\"") && intializer.endsWith("\"")) {
      intializer = intializer.substring(1, intializer.length() - 1);
    }
    return intializer;
  }


}
