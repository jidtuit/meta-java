package org.jid.metajava;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.jid.metajava.exceptions.ClassNotParseableException;

class CompilationUnitTreeFactory {

  Iterable<? extends CompilationUnitTree> getCompilationUnitTrees(Collection<File> files) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
    Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);

    // "-proc:full" compiler option needed to be able to process annotations
    JavacTask javacTask =
      (JavacTask) compiler.getTask(null, fileManager, null, List.of("-proc:full"), null, compilationUnits);

    try {
      return javacTask.parse();
    } catch (IOException e) {
      throw new ClassNotParseableException(e);
    }
  }
}