package org.jid.metajava;

import static com.sun.source.tree.Tree.Kind.ANNOTATION;
import static com.sun.source.tree.Tree.Kind.ANNOTATION_TYPE;
import static com.sun.source.tree.Tree.Kind.ASSIGNMENT;
import static com.sun.source.tree.Tree.Kind.CLASS;
import static com.sun.source.tree.Tree.Kind.ENUM;
import static com.sun.source.tree.Tree.Kind.INTERFACE;
import static com.sun.source.tree.Tree.Kind.METHOD;
import static com.sun.source.tree.Tree.Kind.RECORD;
import static com.sun.source.tree.Tree.Kind.STRING_LITERAL;
import static com.sun.source.tree.Tree.Kind.VARIABLE;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SimpleTreeVisitor;
import java.util.Set;
import java.util.function.BiFunction;

class VisitorFactory {

  private final static Set<Tree.Kind> CLASS_TYPES = Set.of(CLASS, INTERFACE, RECORD, ENUM, ANNOTATION_TYPE);

  static <R, P> R runClassVisitor(Tree tree, P param, BiFunction<ClassTree, P, R> f) {
    if (!CLASS_TYPES.contains(tree.getKind())) {
      return null;
    }
    return tree.accept(new SimpleTreeVisitor<>() {
      @Override
      public R visitClass(ClassTree classTree, P param) {
        return f.apply(classTree, param);
      }
    }, param);
  }

  static <R, P> R runMethodVisitor(Tree tree, P param, BiFunction<MethodTree, P, R> f) {
    if (tree.getKind() != METHOD) {
      return null;
    }
    return tree.accept(new SimpleTreeVisitor<>() {
      @Override
      public R visitMethod(MethodTree methodTree, P param) {
        return f.apply(methodTree, param);
      }
    }, param);
  }

  static <R, P> R runLiteralVisitor(Tree tree, P param, BiFunction<LiteralTree, P, R> f) {
    if (tree.getKind() != STRING_LITERAL) {
      return null;
    }
    return tree.accept(new SimpleTreeVisitor<>() {
      @Override
      public R visitLiteral(LiteralTree literalTree, P p) {
        return f.apply(literalTree, p);
      }
    }, param);
  }

  static <R, P> R runAssignmentlVisitor(Tree tree, P param, BiFunction<AssignmentTree, P, R> f) {
    if (tree.getKind() != ASSIGNMENT) {
      return null;
    }
    return tree.accept(new SimpleTreeVisitor<>() {
      @Override
      public R visitAssignment(AssignmentTree assignmentTree, P p) {
        return f.apply(assignmentTree, p);
      }
    }, param);
  }

  static <R, P> R runVariableVisitor(Tree tree, P param, BiFunction<VariableTree, P, R> f) {
    if (tree.getKind() != VARIABLE) {
      return null;
    }
    return tree.accept(new SimpleTreeVisitor<>() {
      @Override
      public R visitVariable(VariableTree tree, P p) {
        return f.apply(tree, p);
      }
    }, param);
  }

}
