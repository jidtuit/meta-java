package org.jid.metajava;

import java.util.List;
import org.jid.metajava.model.ImportMeta;

record CompilationUnitMeta(String sourceFile, String packageName, List<ImportMeta> imports) {

}
