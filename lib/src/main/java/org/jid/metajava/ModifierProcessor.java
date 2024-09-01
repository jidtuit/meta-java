package org.jid.metajava;

import com.sun.source.tree.ModifiersTree;
import java.util.Set;
import java.util.stream.Collectors;
import org.jid.metajava.model.Modifier;

class ModifierProcessor {

  ModifierProcessor() {
  }

  Set<Modifier> getModifierFlags(ModifiersTree modifierTree) {
    return modifierTree.getFlags().stream()
      .map(m -> Modifier.from(m.toString()))
      .collect(Collectors.toUnmodifiableSet());
  }
}