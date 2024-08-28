package org.jid.metajava;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import java.util.List;
import org.jid.metajava.model.ImportMeta;

class CompilationUnitMetaProcessor {

  public CompilationUnitMeta getMeta(CompilationUnitTree compilationUnitTree) {
    String sourceFile = compilationUnitTree.getSourceFile().toUri().toString();
    String packageName = compilationUnitTree.getPackage().getPackageName().toString();
    List<ImportMeta> imports = compilationUnitTree.getImports().stream().map(this::parseImport).toList();
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
