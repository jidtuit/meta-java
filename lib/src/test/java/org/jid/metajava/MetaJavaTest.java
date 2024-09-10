package org.jid.metajava;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jid.metajava.model.ClassType.ANNOTATION;
import static org.jid.metajava.model.ClassType.CLASS;
import static org.jid.metajava.model.ClassType.ENUM;
import static org.jid.metajava.model.ClassType.INTERFACE;
import static org.jid.metajava.model.ClassType.RECORD;
import static org.jid.metajava.model.Modifier.*;
import static org.jid.metajava.model.Modifier.PUBLIC;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import org.jid.metajava.model.AnnotationArgument;
import org.jid.metajava.model.AnnotationMeta;
import org.jid.metajava.model.AnnotationSupport;
import org.jid.metajava.model.ClassMeta;
import org.jid.metajava.model.VariableMeta;
import org.jid.metajava.model.ImportMeta;
import org.jid.metajava.model.MethodMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MetaJavaTest {

  private MetaJava metaJava = new MetaJava();
  private Path sampleRootPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "sampleCode", "org", "jid");
  private File sampleClass1;
  private File sampleClass2;
  private File sampleInterface1;
  private File sampleInterface2;
  private File sampleRecord1;
  private File sampleEnum1;
  private File sampleAnnotation1;
  private File sampleMethod;
  private List<File> sampleClasses;
  private List<File> sampleClassTypes;

  @BeforeEach
  void setup() {
    sampleClass1 = sampleRootPath.resolve("sample1").resolve("Class1.java").toFile();
    sampleClass2 = sampleRootPath.resolve("sample2").resolve("ClassEmpty.java").toFile();
    sampleInterface1 = sampleRootPath.resolve("sample1").resolve("Interface1.java").toFile();
    sampleInterface2 = sampleRootPath.resolve("sample2").resolve("Interface2.java").toFile();
    sampleRecord1 = sampleRootPath.resolve("sample1").resolve("Record1.java").toFile();
    sampleEnum1 = sampleRootPath.resolve("sample1").resolve("Enum1.java").toFile();
    sampleAnnotation1 = sampleRootPath.resolve("sample1").resolve("Annotation1.java").toFile();
    sampleMethod = sampleRootPath.resolve("sample1").resolve("MethodSample.java").toFile();
    sampleClasses = List.of(sampleClass1, sampleClass2);
    sampleClassTypes = List.of(sampleClass1, sampleInterface1, sampleRecord1, sampleEnum1, sampleAnnotation1);
  }

  @Nested
  class ClassMetaTests {

    @Nested
    class CommonForAllTypes {


      @Test
      void readClassName() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        assertThat(actual).map(ClassMeta::name).containsExactlyInAnyOrder("Class1", "ClassEmpty");
      }

      @Test
      void readPackageInfo() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        assertThat(actual).map(ClassMeta::packageName)
          .containsExactlyInAnyOrder("org.jid.sample1", "org.jid.sample2");
      }

      @Test
      void readSourceFile() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        String source1 = actual.stream().map(ClassMeta::sourceFileUri).filter(s -> s.endsWith("Class1.java")).findFirst().orElseThrow();
        assertThat(source1).containsSubsequence("meta-java", "lib", "src", "test", "resources", "sampleCode", "org", "jid", "sample1",
          "Class1.java");

        String source2 = actual.stream().map(ClassMeta::sourceFileUri).filter(s -> s.endsWith("ClassEmpty.java")).findFirst().orElseThrow();
        assertThat(source2).containsSubsequence("meta-java", "lib", "src", "test", "resources", "sampleCode", "org", "jid", "sample2",
          "ClassEmpty.java");
      }

      @Test
      void readStaticImportInfo() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        ClassMeta class1 = getClassMeta(actual, "Class1");
        List<String> staticImports = class1.imports().stream().filter(ImportMeta::isStatic).map(ImportMeta::importString).toList();
        assertThat(staticImports).containsExactlyInAnyOrder(
          "sample.staticimport.Class11.method1", "sample.staticimport.Class11.method2", "sample.staticimport.ClassWildcard1.*"
        );

        ClassMeta classEmpty = getClassMeta(actual, "ClassEmpty");
        assertThat(classEmpty.imports()).isEmpty();
      }

      @Test
      void readNonStaticImportInfo() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        ClassMeta class1 = getClassMeta(actual, "Class1");
        List<String> staticImports = class1.imports().stream().filter(not(ImportMeta::isStatic)).map(ImportMeta::importString).toList();
        assertThat(staticImports).containsExactlyInAnyOrder(
          "sample.nonstaticimport.Class12", "sample.nonstaticimport.Class13", "sample.nonstaticimport.wildcard.*"
        );

        ClassMeta classEmpty = getClassMeta(actual, "ClassEmpty");
        assertThat(classEmpty.imports()).isEmpty();
      }

      @Test
      void readClassAnnotationsWithNoArguments() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        ClassMeta class1 = getClassMeta(actual, "Class1");
        AnnotationMeta annotation = getAnnotationMeta(class1, "MyClassAnnotation1");
        assertThat(annotation.name()).isEqualTo("MyClassAnnotation1");
        assertThat(annotation.args()).isEmpty();
      }

      @Test
      void readClassAnnotationsWithDefaultArgumentAsAString() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        ClassMeta class1 = getClassMeta(actual, "Class1");
        AnnotationMeta annotation = getAnnotationMeta(class1, "MyClassAnnotation2");
        assertThat(annotation.name()).isEqualTo("MyClassAnnotation2");
        assertThat(annotation.args()).containsExactly(new AnnotationArgument(null, "default param 1"));
      }

      @Test
      void readClassAnnotationsWithDefaultArgumentAsAClassReference() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleAnnotation1));

        ClassMeta class1 = getClassMeta(actual, "Annotation1");
        AnnotationMeta annotation = getAnnotationMeta(class1, "Retention");
        assertThat(annotation.name()).isEqualTo("Retention");
        assertThat(annotation.args()).containsExactly(new AnnotationArgument(null, "RetentionPolicy.RUNTIME"));
      }

      @Test
      void readClassAnnotationsWithMultipleArguments() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        ClassMeta class1 = getClassMeta(actual, "Class1");
        AnnotationMeta annotation = getAnnotationMeta(class1, "MyClassAnnotation3");
        assertThat(annotation.name()).isEqualTo("MyClassAnnotation3");
        assertThat(annotation.args()).containsExactlyInAnyOrder(
          new AnnotationArgument("arg1", "{3, 2, 1}"),
          new AnnotationArgument("arg2", "24")
        );
      }

      @Test
      void readClassAnnotationsWhenThereIsNoAnnotation() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        ClassMeta classEmpty = getClassMeta(actual, "ClassEmpty");
        assertThat(classEmpty.annotations()).isEmpty();
      }

      @Test
      void readMultipleClassesDefinedInOneFile() {
        File multiClassFile = sampleRootPath.resolve("sample1").resolve("MultipleClassIn1File.java").toFile();

        Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(multiClassFile));

        ClassMeta class1 = getClassMeta(actual, "MultipleClassIn1File");
        assertThat(class1.methods()).map(MethodMeta::name).containsExactly("m1");

        ClassMeta class2 = getClassMeta(actual, "MultipleClassIn1File2");
        assertThat(class2.methods()).map(MethodMeta::name).containsExactly("m2");

        ClassMeta record1 = getClassMeta(actual, "MultipleClassFileRecord");
        assertThat(record1.fields()).map(VariableMeta::name).containsExactly("field1");

        ClassMeta enum1 = getClassMeta(actual, "MultipleClassFileEnum");
        assertThat(enum1.fields()).map(VariableMeta::name).containsExactlyInAnyOrder("VAR1", "VAR2");

        ClassMeta interface1 = getClassMeta(actual, "MultipleClassFileInterface");
        assertThat(interface1.methods()).map(MethodMeta::name).containsExactly("mInterface1");

        ClassMeta annotation1 = getClassMeta(actual, "MultipleClassFileAnnotation");
        assertThat(annotation1.methods()).map(MethodMeta::name).containsExactly("value");
      }

      @Test
      void readConstructors() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClassTypes);

        ClassMeta class1 = getClassMeta(actual, "Class1");
        assertThat(class1.constructors()).isEmpty();
        assertThat(class1.methods()).isNotEmpty();

        ClassMeta enum1 = getClassMeta(actual, "Enum1");
        assertThat(enum1.constructors()).hasSize(2);
        assertThat(enum1.methods()).isNotEmpty();

        ClassMeta record1 = getClassMeta(actual, "Record1");
        assertThat(record1.constructors()).hasSize(1);
        assertThat(record1.methods()).isNotEmpty();

        ClassMeta interface1 = getClassMeta(actual, "Interface1");
        assertThat(interface1.constructors()).isEmpty();
        assertThat(interface1.methods()).isNotEmpty();

        ClassMeta annotation1 = getClassMeta(actual, "Annotation1");
        assertThat(annotation1.constructors()).isEmpty();
        assertThat(annotation1.methods()).isNotEmpty();
      }

      @Test
      void readNestedClasses() {

        File nestedClassFile = sampleRootPath.resolve("sample1").resolve("NestedClasses.java").toFile();

        Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(nestedClassFile));

        ClassMeta parentClass = getClassMeta(actual, "NestedClasses");
        assertThat(parentClass.nestedClasses()).isNotEmpty();
        ClassMeta nestedClass = getClassMeta(parentClass.nestedClasses(), "StaticNestedClass");
        assertThat(nestedClass.type()).isEqualTo(CLASS);
        assertThat(nestedClass.modifiers()).contains(STATIC);

        ClassMeta innerClass = getClassMeta(parentClass.nestedClasses(), "InnerClass");
        assertThat(innerClass.type()).isEqualTo(CLASS);
        assertThat(innerClass.modifiers()).doesNotContain(STATIC);

        ClassMeta innerEnum = getClassMeta(parentClass.nestedClasses(), "InnerEnum");
        assertThat(innerEnum.type()).isEqualTo(ENUM);
        assertThat(innerClass.modifiers()).doesNotContain(STATIC);

        ClassMeta innerRecord = getClassMeta(parentClass.nestedClasses(), "InnerRecord");
        assertThat(innerRecord.type()).isEqualTo(RECORD);
        assertThat(innerRecord.fields()).map(VariableMeta::name).containsExactly("p1");
        assertThat(innerClass.modifiers()).doesNotContain(STATIC);

        ClassMeta innerInterface = getClassMeta(parentClass.nestedClasses(), "InnerInterface");
        assertThat(innerInterface.type()).isEqualTo(INTERFACE);
        assertThat(innerClass.modifiers()).doesNotContain(STATIC);

        ClassMeta innerAnnotation = getClassMeta(parentClass.nestedClasses(), "InnerAnnotation");
        assertThat(innerAnnotation.type()).isEqualTo(ANNOTATION);
        assertThat(innerClass.modifiers()).doesNotContain(STATIC);
      }

      @Test
      void read2LevelNestedClasses() {

        File nestedClassFile = sampleRootPath.resolve("sample1").resolve("NestedClasses.java").toFile();

        Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(nestedClassFile));

        ClassMeta parentClass = getClassMeta(actual, "NestedClasses");
        assertThat(parentClass.nestedClasses()).isNotEmpty();

        ClassMeta nestedClass = getClassMeta(parentClass.nestedClasses(), "InnerClass2");
        assertThat(nestedClass.type()).isEqualTo(CLASS);

        ClassMeta childInnerClass2 = getClassMeta(nestedClass.nestedClasses(), "ChildInnerClass2");
        assertThat(childInnerClass2.fields()).map(VariableMeta::name).containsExactly("innerParam");
        assertThat(childInnerClass2.type()).isEqualTo(RECORD);
      }

      @Test
      void readSealedInterface() {
        File sealedInterface = sampleRootPath.resolve("sample1").resolve("SealedClasses.java").toFile();

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sealedInterface));

        ClassMeta parentClass = getClassMeta(actual, "SealedClasses");
        assertThat(parentClass.modifiers()).contains(SEALED);
        assertThat(parentClass.permits()).containsExactlyInAnyOrder("FinalChild", "NonSealedChild");
        assertThat(parentClass.nestedClasses()).hasSize(2);

        ClassMeta nonSealedChild = getClassMeta(parentClass.nestedClasses(), "NonSealedChild");
        assertThat(nonSealedChild.modifiers()).contains(NON_SEALED);

        ClassMeta finalChild = getClassMeta(parentClass.nestedClasses(), "FinalChild");
        assertThat(finalChild.type()).isEqualTo(RECORD);

      }

    }


    @Nested
    class ClassType {

      @Test
      void readClassType() {
        Set<ClassMeta> actual = metaJava.getMetaFrom(
          List.of(sampleClass1, sampleInterface1, sampleEnum1, sampleAnnotation1, sampleRecord1));

        ClassMeta clazz = getClassMeta(actual, "Class1");
        assertThat(clazz.type()).isEqualTo(CLASS);
      }

      @Test
      void readInheritanceInfoWhenClassExtendsFromAnotherClass() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleClass1));

        ClassMeta class1 = getClassMeta(actual, "Class1");
        assertThat(class1.extendsFrom()).containsExactly("ClassParent1");
      }

      @Test
      void readInheritanceInfoWhenClassExtendsFromAnotherClassWithGenerics() {

        File sampleClass = sampleRootPath.resolve("sample1").resolve("ClassInheritanceWithGenerics.java").toFile();
        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(
          sampleClass));

        ClassMeta class1 = getClassMeta(actual, "ClassInheritanceWithGenerics");
        assertThat(class1.extendsFrom()).containsExactly("ClassParent2<ClassGeneric1>");
      }

      @Test
      void readNoInheritanceInfo() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleClass2));

        ClassMeta class2 = getClassMeta(actual, "ClassEmpty");
        assertThat(class2.extendsFrom()).isEmpty();
      }

      @Test
      void readInheritanceInfoWhenClassImplementsFromInterfaces() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleClass1));

        ClassMeta class1 = getClassMeta(actual, "Class1");
        assertThat(class1.implementsFrom()).containsExactlyInAnyOrder("I1", "I2");
      }

      @Test
      void readInheritanceInfoWhenClassImplementsFromInterfacesWithGenerics() {

        File sampleClass = sampleRootPath.resolve("sample1").resolve("ClassInheritanceWithGenerics.java").toFile();
        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(
          sampleClass));

        ClassMeta class1 = getClassMeta(actual, "ClassInheritanceWithGenerics");
        assertThat(class1.implementsFrom()).containsExactlyInAnyOrder("I3<IGeneric1>", "I4<IGeneric2>");
      }

      @Test
      void readNoInterfaceInheritanceInfo() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleClass2));

        ClassMeta class2 = getClassMeta(actual, "ClassEmpty");
        assertThat(class2.implementsFrom()).isEmpty();
      }
    }

    @Nested
    class InterfaceType {

      @Test
      void readInterfaceType() {
        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClassTypes);

        ClassMeta clazz = getClassMeta(actual, "Interface1");
        assertThat(clazz.type()).isEqualTo(INTERFACE);
      }

      @Test
      void readInheritanceInfoWhenInterfaceExtendsFromOtherInterfaces() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleInterface1));

        ClassMeta interface1 = getClassMeta(actual, "Interface1");
        assertThat(interface1.extendsFrom()).containsExactlyInAnyOrder("InterfaceParent1", "InterfaceParent2");
      }

      @Test
      void readInheritanceInfoWhenInterfaceExtendsFromOtherInterfacesWithGenerics() {

        File sampleInterface = sampleRootPath.resolve("sample1").resolve("Interface2ExtendWithGenerics.java").toFile();

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleInterface));

        ClassMeta interface1 = getClassMeta(actual, "Interface2ExtendWithGenerics");
        assertThat(interface1.extendsFrom()).containsExactlyInAnyOrder("InterfaceParent1", "InterfaceParent2<GenericInterface1>");
      }

      @Test
      void readNoInheritanceInfo() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleInterface2));

        ClassMeta interface2 = getClassMeta(actual, "Interface2");
        assertThat(interface2.extendsFrom()).isEmpty();
      }

      @Test
      void readNoInterfaceInheritanceInfo() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleInterface2));

        ClassMeta interface2 = getClassMeta(actual, "Interface2");
        assertThat(interface2.implementsFrom()).isEmpty();
      }

    }

    @Nested
    class RecordType {

      @Test
      void readRecordType() {
        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClassTypes);

        ClassMeta clazz = getClassMeta(actual, "Record1");
        assertThat(clazz.type()).isEqualTo(RECORD);
      }

      @Test
      void readNoInheritanceInfo() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleRecord1));

        ClassMeta record1 = getClassMeta(actual, "Record1");
        assertThat(record1.extendsFrom()).isEmpty();
      }

      @Test
      void readInheritanceInfoWhenClassImplementsFromInterfaces() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleRecord1));

        ClassMeta record1 = getClassMeta(actual, "Record1");
        assertThat(record1.implementsFrom()).containsExactlyInAnyOrder("IR1", "IR2");
      }

      @Test
      void readInheritanceInfoWhenClassImplementsFromInterfacesWithGenerics() {

        File sampleClass = sampleRootPath.resolve("sample1").resolve("Record2ImplementsWithGenerics.java").toFile();
        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(
          sampleClass));

        ClassMeta record1 = getClassMeta(actual, "Record2ImplementsWithGenerics");
        assertThat(record1.implementsFrom()).containsExactlyInAnyOrder("IR1<Generic1>", "IR2<Generic2>");
      }

      @Test
      void readNoInterfaceInheritanceInfo() {

        File sampleClass = sampleRootPath.resolve("sample1").resolve("Record3NoImplements.java").toFile();
        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(
          sampleClass));

        ClassMeta record1 = getClassMeta(actual, "Record3NoImplements");
        assertThat(record1.implementsFrom()).isEmpty();
      }
    }

    @Nested
    class EnumType {

      @Test
      void readEnumType() {
        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClassTypes);

        ClassMeta clazz = getClassMeta(actual, "Enum1");
        assertThat(clazz.type()).isEqualTo(ENUM);
      }

      @Test
      void readNoInheritanceInfo() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleEnum1));

        ClassMeta enum1 = getClassMeta(actual, "Enum1");
        assertThat(enum1.extendsFrom()).isEmpty();
      }

    }

    @Nested
    class AnnotationType {

      @Test
      void readAnnotationType() {
        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClassTypes);

        ClassMeta clazz = getClassMeta(actual, "Annotation1");
        assertThat(clazz.type()).isEqualTo(ANNOTATION);
      }

      @Test
      void readNoInheritanceInfo() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleAnnotation1));

        ClassMeta annotation1 = getClassMeta(actual, "Annotation1");
        assertThat(annotation1.extendsFrom()).isEmpty();
      }

      @Test
      void readNoInterfaceInheritanceInfo() {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleAnnotation1));

        ClassMeta annotation1 = getClassMeta(actual, "Annotation1");
        assertThat(annotation1.implementsFrom()).isEmpty();
      }
    }

  }

  @Nested
  class MethodMetaTests {

    @Test
    void readMethodNames() {

      Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

      ClassMeta class1 = getClassMeta(actual, "Class1");
      assertThat(class1.methods()).map(MethodMeta::name).containsExactlyInAnyOrder("m11", "m12", "noAnnotationMethod");
    }

    @Test
    void returnEmptyWhenNoMethodNames() {

      Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

      ClassMeta classEmpty = getClassMeta(actual, "ClassEmpty");
      assertThat(classEmpty.methods()).isEmpty();
    }

    @Test
    void readPublicModifiers() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "m1");
      assertThat(methodMeta.modifiers()).containsExactlyInAnyOrder(PUBLIC);
    }

    @Test
    void readFriendlyModifiers() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "m2");
      assertThat(methodMeta.modifiers()).isEmpty();
    }

    @Test
    void readProtectedModifiers() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "m4");
      assertThat(methodMeta.modifiers()).containsExactlyInAnyOrder(PROTECTED);
    }

    @Test
    void readPrivateModifiers() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "m3");
      assertThat(methodMeta.modifiers()).containsExactlyInAnyOrder(PRIVATE);
    }

    @Test
    void readStaticModifiers() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "staticMethod");
      assertThat(methodMeta.modifiers()).containsExactlyInAnyOrder(STATIC, PUBLIC);
    }

    @Test
    void readSynchronizedModifiers() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "synchMethod");
      assertThat(methodMeta.modifiers()).containsExactlyInAnyOrder(SYNCHRONIZED, PUBLIC);
    }

    @Test
    void readMethodAnnotation() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "annotatedMethod");
      assertThat(methodMeta.annotations()).map(AnnotationMeta::name).containsExactlyInAnyOrder("Annotated");
    }

    @Test
    void readVoidReturnType() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "m1");
      assertThat(methodMeta.returnType()).isEqualTo("void");
    }

    @Test
    void readPrimitiveReturnType() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "m2");
      assertThat(methodMeta.returnType()).isEqualTo("int");
    }

    @Test
    void readObjectReturnType() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "m3");
      assertThat(methodMeta.returnType()).isEqualTo("StringBuilder");
    }

    @Test
    void ignoreConstructors() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      assertThat(classMeta.methods()).map(MethodMeta::name).doesNotContainSubsequence("<init>");
    }

    @Test
    void readMethodWithNoParams() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "m3");
      assertThat(methodMeta.params()).isEmpty();
    }

    @Test
    void readMethodWithParams() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "m4");
      // Order is important since it is a sequenced collection
      assertThat(methodMeta.params()).map(VariableMeta::name).containsExactly("p1", "p2");

      VariableMeta p1 = getParamMeta(methodMeta, "p1");
      assertThat(p1.name()).isEqualTo("p1");
      assertThat(p1.type()).isEqualTo("Integer");
      assertThat(p1.annotations()).isEmpty();
      assertThat(p1.modifiers()).isEmpty();
      assertThat(p1.initializer()).isNull();

      VariableMeta p2 = getParamMeta(methodMeta, "p2");
      assertThat(p2.name()).isEqualTo("p2");
      assertThat(p2.type()).isEqualTo("int");
      assertThat(p2.annotations()).map(AnnotationMeta::name).containsExactly("Deprecated");
      assertThat(p2.modifiers()).isEmpty();
      assertThat(p2.initializer()).isNull();
    }

    @Test
    void readMethodWithVarArgParams() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "varArgsMethod");
      assertThat(methodMeta.params()).map(VariableMeta::name).containsExactly("p1", "p2");

      VariableMeta p1 = getParamMeta(methodMeta, "p1");
      assertThat(p1.type()).isEqualTo("int");

      VariableMeta p2 = getParamMeta(methodMeta, "p2");
      assertThat(p2.type()).isEqualTo("String[]");
    }

    @Test
    void readMethodThatThrowsException() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "throwsExceptionMethod");
      assertThat(methodMeta.exceptions()).containsExactlyInAnyOrder("MyException1", "MyException2");
    }

    @Test
    void readMethodThatDoesNotThrowExceptions() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      MethodMeta methodMeta = getMethodMeta(classMeta, "m1");
      assertThat(methodMeta.exceptions()).isEmpty();
    }

    @Test
    void readConstructors() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleMethod));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      List<MethodMeta> constructors = classMeta.constructors().stream().filter(MethodMeta::isConstructor).toList();
      assertThat(constructors).hasSize(2);

      MethodMeta constructor1 = constructors.stream().filter(c -> c.params().isEmpty()).findFirst().orElseThrow();
      assertThat(constructor1.name()).isEqualTo("<init>");
      assertThat(constructor1.returnType()).isNull();
      assertThat(constructor1.params()).isEmpty();
      assertThat(constructor1.modifiers()).containsExactly(PUBLIC);
      assertThat(constructor1.exceptions()).isEmpty();
      assertThat(constructor1.annotations()).isEmpty();

      MethodMeta constructor2 = constructors.stream().filter(c -> !c.params().isEmpty()).findFirst().orElseThrow();
      assertThat(constructor2.name()).isEqualTo("<init>");
      assertThat(constructor2.returnType()).isNull();
      assertThat(constructor2.params()).map(VariableMeta::name).containsExactly("s1");
      assertThat(constructor2.modifiers()).isEmpty();
      assertThat(constructor2.exceptions()).containsExactlyInAnyOrder("MyException1");
      assertThat(constructor2.annotations()).map(AnnotationMeta::name).containsExactly("Deprecated");
    }

    @Test
    void readInterfaceWithDefaultMethods() {

      File interface3File = sampleRootPath.resolve("sample1").resolve("Interface3WithDefaultMethods.java").toFile();

      Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(interface3File));

      ClassMeta interface3 = getClassMeta(actual, "Interface3WithDefaultMethods");
      MethodMeta m31 = getMethodMeta(interface3, "method31");
      assertThat(m31.name()).isEqualTo("method31");
      assertThat(m31.modifiers()).contains(DEFAULT);

      MethodMeta m32 = getMethodMeta(interface3, "method32");
      assertThat(m32.name()).isEqualTo("method32");
      assertThat(m32.exceptions()).containsExactly("IOException");
      assertThat(m32.modifiers()).contains(DEFAULT);
    }

    @Test
    void readInterfaceWithoutDefaultMethods() {

      File interface3File = sampleRootPath.resolve("sample1").resolve("Interface3WithDefaultMethods.java").toFile();

      Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(interface3File));

      ClassMeta interface3 = getClassMeta(actual, "Interface3WithDefaultMethods");
      MethodMeta method = getMethodMeta(interface3, "methodWithNoDefaultImplementation");
      assertThat(method.name()).isEqualTo("methodWithNoDefaultImplementation");
      assertThat(method.modifiers()).doesNotContain(DEFAULT);
    }

  }

  @Nested
  class AnnotationsMetaTests {

    @Test
    void readAnnotationsWithoutArguments() {

      Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

      ClassMeta class1 = getClassMeta(actual, "Class1");
      MethodMeta methodMeta12 = getMethodMeta(class1, "m12");
      AnnotationMeta myMethodAnnotation13 = getAnnotationMeta(methodMeta12, "MyMethodAnnotation13");

      assertThat(myMethodAnnotation13.name()).isEqualTo("MyMethodAnnotation13");
      assertThat(myMethodAnnotation13.args()).isEmpty();
    }

    @Test
    void readAnnotationsWithDefaultArgument() {

      Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

      ClassMeta class1 = getClassMeta(actual, "Class1");
      MethodMeta methodMeta12 = getMethodMeta(class1, "m11");
      AnnotationMeta myMethodAnnotation11 = getAnnotationMeta(methodMeta12, "MyMethodAnnotation11");

      assertThat(myMethodAnnotation11.name()).isEqualTo("MyMethodAnnotation11");
      assertThat(myMethodAnnotation11.args()).hasSize(1);

      AnnotationArgument arg = myMethodAnnotation11.args().stream().findFirst().orElseThrow();
      assertThat(arg).isEqualTo(new AnnotationArgument(null, "annotation param 11"));
    }

    @Test
    void readAnnotationsWithSeveralArguments() {

      Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

      ClassMeta class1 = getClassMeta(actual, "Class1");
      MethodMeta methodMeta12 = getMethodMeta(class1, "m12");
      AnnotationMeta MyMethodAnnotation12 = getAnnotationMeta(methodMeta12, "MyMethodAnnotation12");

      assertThat(MyMethodAnnotation12.name()).isEqualTo("MyMethodAnnotation12");
      assertThat(MyMethodAnnotation12.args()).hasSize(2);

      AnnotationArgument arg1Value = getAnnotationArgsMeta(MyMethodAnnotation12, "arg1");
      assertThat(arg1Value).isEqualTo(new AnnotationArgument("arg1", "42"));

      AnnotationArgument arg2Value = getAnnotationArgsMeta(MyMethodAnnotation12, "arg2");
      assertThat(arg2Value).isEqualTo(new AnnotationArgument("arg2", "{1, 2, 3}"));
    }

    @Test
    void readMethodWithNoAnnotations() {

      Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

      ClassMeta class1 = getClassMeta(actual, "Class1");
      MethodMeta methodMeta12 = getMethodMeta(class1, "noAnnotationMethod");

      assertThat(methodMeta12.annotations()).isEmpty();
    }

  }

  @Nested
  class VariableMetaTests {

    @Test
    void readFieldNames() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleClass1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      assertThat(classMeta.fields()).map(VariableMeta::name)
        .containsExactlyInAnyOrder("CONSTANT_1_1", "answer", "notInitVar", "expressionVar");
    }

    @Test
    void readTypes() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleClass1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      assertThat(classMeta.fields()).map(VariableMeta::type)
        .containsExactlyInAnyOrder("String", "int", "Double", "float");
    }

    @Test
    void readInitialValue() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleClass1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      assertThat(classMeta.fields()).map(VariableMeta::initializer)
        .containsExactlyInAnyOrder("constant1-1", "42", null, "1.0F + 1.0F");
    }

    @Test
    void readAnnotationsWhenPresent() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleClass1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      VariableMeta field = getFieldMeta(classMeta, "answer");
      assertThat(field.annotations()).map(AnnotationMeta::name).containsExactly("MeaningOfLifeUniverseAndEverythingElse");
    }

    @Test
    void readNoAnnotationsWhenNotPresent() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleClass1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      VariableMeta field = getFieldMeta(classMeta, "expressionVar");
      assertThat(field.annotations()).isEmpty();
    }

    @Test
    void readClassWithNoFields() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleClass2));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      assertThat(classMeta.fields()).isEmpty();
    }

    @Test
    void readEnumValuesAsFields() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleEnum1));

      ClassMeta clazz = getClassMeta(actual, "Enum1");

      VariableMeta value1 = getFieldMeta(clazz, "VAR1");
      assertThat(value1.name()).isEqualTo("VAR1");
      assertThat(value1.type()).isEqualTo("Enum1");
      assertThat(value1.initializer()).isEqualTo("new Enum1(\"hello\")");
      assertThat(value1.modifiers()).containsExactlyInAnyOrder(PUBLIC, STATIC, FINAL);
      assertThat(value1.annotations()).map(AnnotationMeta::name).containsExactly("Deprecated");

      VariableMeta value2 = getFieldMeta(clazz, "VAR2");
      assertThat(value2.name()).isEqualTo("VAR2");
      assertThat(value2.type()).isEqualTo("Enum1");
      assertThat(value2.initializer()).isEqualTo("new Enum1()");
      assertThat(value2.modifiers()).containsExactlyInAnyOrder(PUBLIC, STATIC, FINAL);
      assertThat(value2.annotations()).isEmpty();

      VariableMeta enumField = getFieldMeta(clazz, "initVar");
      assertThat(enumField.name()).isEqualTo("initVar");
      assertThat(enumField.type()).isEqualTo("String");
      assertThat(enumField.initializer()).isNull();
      assertThat(enumField.modifiers()).containsExactlyInAnyOrder(PRIVATE, FINAL);
      assertThat(enumField.annotations()).isEmpty();
    }

    @Test
    void readRecordParamsAsFields() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleRecord1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();

      VariableMeta param1 = getFieldMeta(classMeta, "param1");
      assertThat(param1.name()).isEqualTo("param1");
      assertThat(param1.type()).isEqualTo("String");
      assertThat(param1.initializer()).isNull();
      assertThat(param1.modifiers()).containsExactlyInAnyOrder(FINAL, PRIVATE);
      assertThat(param1.annotations()).isEmpty();

      VariableMeta param2 = getFieldMeta(classMeta, "param2");
      assertThat(param2.name()).isEqualTo("param2");
      assertThat(param2.type()).isEqualTo("int");
      assertThat(param2.initializer()).isNull();
      assertThat(param2.modifiers()).containsExactlyInAnyOrder(FINAL, PRIVATE);
      assertThat(param2.annotations()).map(AnnotationMeta::name).containsExactly("Deprecated");

      VariableMeta myConst = getFieldMeta(classMeta, "MY_CONST");
      assertThat(myConst.name()).isEqualTo("MY_CONST");
      assertThat(myConst.type()).isEqualTo("Integer");
      assertThat(myConst.initializer()).isEqualTo("42");
      assertThat(myConst.modifiers()).containsExactlyInAnyOrder(PUBLIC, FINAL, STATIC);
      assertThat(myConst.annotations()).isEmpty();
    }

    @Test
    void readInterfaceConstantsAsFields() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleInterface1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();

      VariableMeta myConst = getFieldMeta(classMeta, "MY_CONST_I");
      assertThat(myConst.name()).isEqualTo("MY_CONST_I");
      assertThat(myConst.type()).isEqualTo("Float");
      assertThat(myConst.initializer()).isEqualTo("42.0F");
      assertThat(myConst.modifiers()).isEmpty();
      assertThat(myConst.annotations()).map(AnnotationMeta::name).containsExactly("Deprecated");
    }

  }

  @Nested
  class ModifierMetaTests {

    @Test
    void readMultipleModifiers() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleClass1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      assertThat(classMeta.modifiers()).containsExactlyInAnyOrder(PUBLIC, ABSTRACT);

      VariableMeta field1 = getFieldMeta(classMeta, "CONSTANT_1_1");
      assertThat(field1.modifiers()).containsExactlyInAnyOrder(PUBLIC, STATIC, FINAL);

      VariableMeta field2 = getFieldMeta(classMeta, "answer");
      assertThat(field2.modifiers()).containsExactlyInAnyOrder(PRIVATE, TRANSIENT);

      VariableMeta field3 = getFieldMeta(classMeta, "expressionVar");
      assertThat(field3.modifiers()).containsExactlyInAnyOrder(PROTECTED, VOLATILE, STRICTFP);
    }

    @Test
    void readClassWithNoModifiers() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleClass1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();

      VariableMeta field3 = getFieldMeta(classMeta, "notInitVar");
      assertThat(field3.modifiers()).isEmpty();
    }

  }

  @Nested
  class InputParamTests {

    @Test
    void throwWhenFilesParameterIsNull() {
      assertThatThrownBy(() -> metaJava.getMetaFrom(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwWhenFilesParameterIsEmpty() {
      assertThatThrownBy(() -> metaJava.getMetaFrom(Set.of())).isInstanceOf(IllegalArgumentException.class);
    }

  }

  private ClassMeta getClassMeta(Set<ClassMeta> actual, String name) {
    return actual.stream().filter(c -> c.name().equals(name)).findFirst().orElseThrow();
  }

  private MethodMeta getMethodMeta(ClassMeta class1, String methodName) {
    return class1.methods().stream().filter(m -> methodName.equals(m.name())).findFirst().orElseThrow();
  }

  private AnnotationMeta getAnnotationMeta(AnnotationSupport element1, String annotationName) {
    return element1.annotations().stream().filter(a -> annotationName.equals(a.name())).findFirst().orElseThrow();
  }

  private VariableMeta getFieldMeta(ClassMeta class1, String fieldName) {
    return class1.fields().stream().filter(f -> fieldName.equals(f.name())).findFirst().orElseThrow();
  }

  private VariableMeta getParamMeta(MethodMeta method1, String fieldName) {
    return method1.params().stream().filter(f -> fieldName.equals(f.name())).findFirst().orElseThrow();
  }

  private AnnotationArgument getAnnotationArgsMeta(AnnotationMeta annotationMeta, String argName) {
    return annotationMeta.args().stream()
      .filter(AnnotationArgument::hasName)
      .filter(arg -> arg.name().startsWith(argName))
      .findFirst()
      .orElseThrow();
  }


}
