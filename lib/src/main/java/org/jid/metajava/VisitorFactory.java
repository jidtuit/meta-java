package org.jid.metajava;

import static com.sun.source.tree.Tree.Kind.CLASS;
import static com.sun.source.tree.Tree.Kind.METHOD;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SimpleTreeVisitor;
import java.util.function.BiFunction;

class VisitorFactory {

  static <R, P> R runClassVisitor(Tree tree, P param, BiFunction<ClassTree, P, R> f) {
    if(tree.getKind() != CLASS) {
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
    if(tree.getKind() != METHOD) {
      return null;
    }
    return tree.accept(new SimpleTreeVisitor<>() {
      @Override
      public R visitMethod(MethodTree methodTree, P param) {
        return f.apply(methodTree, param);
      }
    }, param);
  }

}
