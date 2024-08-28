package org.jid.metajava;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static org.jid.metajava.VisitorFactory.runAssignmentlVisitor;
import static org.jid.metajava.VisitorFactory.runLiteralVisitor;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree.Kind;
import java.util.HashSet;
import java.util.Set;
import org.jid.metajava.model.AnnotationArgument;
import org.jid.metajava.model.AnnotationMeta;

class AnnotationProcessor {

  public Set<AnnotationMeta> getMetas(ModifiersTree modifiersTree) {
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
    if (argTree.getKind() == Kind.MEMBER_SELECT) {
      return new AnnotationArgument(null, argTree.toString());
    }

    // argTree.getKind() == Kind.ASSIGNMENT
    return runAssignmentlVisitor(argTree, null, ((assignmentTree, param) -> {
      String argName = assignmentTree.getVariable().toString();
      String argValue = assignmentTree.getExpression().toString();
      return new AnnotationArgument(argName, argValue);
    }));

  }

}
