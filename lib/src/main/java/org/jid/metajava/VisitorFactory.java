package org.jid.metajava;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.SimpleTreeVisitor;
import java.util.function.BiFunction;

class VisitorFactory {

  static <R, P> SimpleTreeVisitor<R, P> getClassVisitor(BiFunction<ClassTree, P, R> f) {
    return new SimpleTreeVisitor<>() {
      @Override
      public R visitClass(ClassTree classTree, P param) {
        return f.apply(classTree, param);
      }
    };
  }

  static <R, P> SimpleTreeVisitor<R, P> getMethodVisitor(BiFunction<MethodTree, P, R> f) {
    return new SimpleTreeVisitor<R, P>() {
      @Override
      public R visitMethod(MethodTree methodTree, P param) {
        return f.apply(methodTree, param);
      }
    };
  }

}
