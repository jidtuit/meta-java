package org.jid.metajava.model;

import java.util.Set;

public sealed interface ModifierSupport permits MethodMeta, VariableMeta {

  Set<Modifier> modifiers();
}
