package org.jid.metajava;

import java.util.Set;
import org.jid.metajava.model.ImportMeta;

record CompilationUnitMeta(String sourceFile, String packageName, Set<ImportMeta> imports) {

}
