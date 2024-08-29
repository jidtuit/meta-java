package org.jid.metajava.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jid.metajava.model.ClassType.CLASS;
import static org.jid.metajava.model.ClassType.INTERFACE;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class ClassMetaTest {

  @Test
  void buildClassMeta() {
    var actual = new ClassMeta("name1", CLASS, Set.of(), Set.of(), "p1", "uri", Set.of(), Set.of());
    assertThat(actual).isNotNull();
  }

  @Test
  void failWhenNoName() {
    assertThatThrownBy(() -> new ClassMeta(null, CLASS, Set.of(), Set.of(), "p1", "uri", Set.of(), Set.of()))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void failWhenNoType() {
    assertThatThrownBy(() -> new ClassMeta("name1", null, Set.of(), Set.of(), "p1", "uri", Set.of(), Set.of()))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void failWhenNoSourceFileUri() {
    assertThatThrownBy(() -> new ClassMeta("name1", CLASS, Set.of(), Set.of(), "p1", null, Set.of(), Set.of()))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void buildInterfaceMetaWithMultipleExtendsFrom() {
    var actual = new ClassMeta("name1", INTERFACE, Set.of(), Set.of(), "p1", "uri", Set.of(), Set.of("P1", "P2"));
    assertThat(actual).isNotNull();
  }

  @Test
  void failWhenClassHasMultipleExtendsFrom() {
    assertThatThrownBy(() -> new ClassMeta("name1", CLASS, Set.of(), Set.of(), "p1", "uri", Set.of(), Set.of("P1", "P2")))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @EnumSource(value = ClassType.class, mode = Mode.EXCLUDE, names = {"CLASS", "INTERFACE"})
  void failWhenClassTypeHasExtendsFrom(ClassType classType) {
    assertThatThrownBy(() -> new ClassMeta("name1", classType, Set.of(), Set.of(), "p1", "uri", Set.of(), Set.of("P1")))
      .isInstanceOf(IllegalArgumentException.class);
  }


}