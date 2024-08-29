package org.jid.metajava;

import static java.util.stream.Collectors.toUnmodifiableSet;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import java.util.Set;
import org.jid.metajava.model.ImportMeta;

class CompilationUnitMetaProcessor {

  public CompilationUnitMeta getMeta(CompilationUnitTree compilationUnitTree) {
    String sourceFile = compilationUnitTree.getSourceFile().toUri().toString();
    String packageName = compilationUnitTree.getPackage().getPackageName().toString();
    Set<ImportMeta> imports = compilationUnitTree.getImports().stream().map(this::parseImport).collect(toUnmodifiableSet());
    return new CompilationUnitMeta(sourceFile, packageName, imports);
  }

  private ImportMeta parseImport(ImportTree importTree) {
    boolean isStatic = importTree.isStatic();
    int length = "import ".length();
    if (isStatic) {
      length += "static ".length();
    }
    var importName = importTree.toString().substring(length).replace(";", "").trim();
    return new ImportMeta(importName, isStatic);
  }

}
