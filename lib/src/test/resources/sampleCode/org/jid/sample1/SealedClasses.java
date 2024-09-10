package org.jid.sample1;

public sealed interface SealedClasses permits FinalChild, NonSealedChild {

  record FinalChild(Integer p1) {

  }

  non-sealed class NonSealedChild {

  }

}