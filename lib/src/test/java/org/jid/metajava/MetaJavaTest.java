package org.jid.metajava;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import org.jid.metajava.model.ClassMeta;
import org.jid.metajava.model.ImportMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetaJavaTest {

  private MetaJava metaJava = new MetaJava();
  private File sample1;
  private File sample2;

  @BeforeEach
  void setup() {
    Path sampleRootPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "sampleCode", "org", "jid");
    sample1 = sampleRootPath.resolve("sample1").resolve("Class1.java").toFile();
    sample2 = sampleRootPath.resolve("sample2").resolve("Class2.java").toFile();
  }

  @Test
  void readClassName() throws IOException {

    Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

    assertThat(actual).map(ClassMeta::name).containsExactlyInAnyOrder("Class1", "Class2");
  }

  @Test
  void readPackageInfo() throws IOException {

    Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

    assertThat(actual).map(ClassMeta::packageName).containsExactlyInAnyOrder("org.jid.sample1", "org.jid.sample2");
  }

  @Test
  void readSourceFile() throws IOException {

    Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

    String source1 = actual.stream().map(ClassMeta::sourceFileUri).filter(s -> s.endsWith("Class1.java")).findFirst().orElseThrow();
    assertThat(source1).containsSubsequence("meta-java", "lib", "src", "test", "resources", "sampleCode", "org", "jid", "sample1",
      "Class1.java");

    String source2 = actual.stream().map(ClassMeta::sourceFileUri).filter(s -> s.endsWith("Class2.java")).findFirst().orElseThrow();
    assertThat(source2).containsSubsequence("meta-java", "lib", "src", "test", "resources", "sampleCode", "org", "jid", "sample2",
      "Class2.java");
  }

  @Test
  void readStaticImportInfo() throws IOException {

    Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

    ClassMeta class1 = actual.stream().filter(c -> c.name().equals("Class1")).findFirst().orElseThrow();
    List<String> staticImports = class1.imports().stream().filter(ImportMeta::isStatic).map(ImportMeta::importString).toList();
    assertThat(staticImports).containsExactlyInAnyOrder(
      "sample.staticimport.Class11.method1", "sample.staticimport.Class11.method2"
    );

    ClassMeta class2 = actual.stream().filter(c -> c.name().equals("Class2")).findFirst().orElseThrow();
    assertThat(class2.imports()).isEmpty();
  }

  @Test
  void readNonStaticImportInfo() throws IOException {

    Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

    ClassMeta class1 = actual.stream().filter(c -> c.name().equals("Class1")).findFirst().orElseThrow();
    List<String> staticImports = class1.imports().stream().filter(not(ImportMeta::isStatic)).map(ImportMeta::importString).toList();
    assertThat(staticImports).containsExactlyInAnyOrder(
      "sample.nonstaticimport.Class12", "sample.nonstaticimport.Class13"
    );

    ClassMeta class2 = actual.stream().filter(c -> c.name().equals("Class2")).findFirst().orElseThrow();
    assertThat(class2.imports()).isEmpty();
  }


  @Test
  void throwWhenFilesParameterIsNull() {
    assertThatThrownBy(() -> metaJava.getMetaFrom(null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void throwWhenFilesParameterIsEmpty() {
    assertThatThrownBy(() -> metaJava.getMetaFrom(Set.of())).isInstanceOf(IllegalArgumentException.class);
  }


}
