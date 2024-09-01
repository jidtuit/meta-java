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
import org.jid.metajava.model.FieldMeta;
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
  private List<File> sampleClasses;
  private List<File> samplesClassTypes;

  @BeforeEach
  void setup() {
    sampleClass1 = sampleRootPath.resolve("sample1").resolve("Class1.java").toFile();
    sampleClass2 = sampleRootPath.resolve("sample2").resolve("ClassEmpty.java").toFile();
    sampleInterface1 = sampleRootPath.resolve("sample1").resolve("Interface1.java").toFile();
    sampleInterface2 = sampleRootPath.resolve("sample2").resolve("Interface2.java").toFile();
    sampleRecord1 = sampleRootPath.resolve("sample1").resolve("Record1.java").toFile();
    sampleEnum1 = sampleRootPath.resolve("sample1").resolve("Enum1.java").toFile();
    sampleAnnotation1 = sampleRootPath.resolve("sample1").resolve("Annotation1.java").toFile();
    sampleClasses = List.of(sampleClass1, sampleClass2);
    samplesClassTypes = List.of(sampleClass1, sampleInterface1, sampleRecord1, sampleEnum1, sampleAnnotation1);
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
        assertThat(record1.fields()).map(FieldMeta::name).containsExactly("field1");

        ClassMeta enum1 = getClassMeta(actual, "MultipleClassFileEnum");
        assertThat(enum1.fields()).map(FieldMeta::name).containsExactlyInAnyOrder("VAR1", "VAR2");

        ClassMeta interface1 = getClassMeta(actual, "MultipleClassFileInterface");
        assertThat(interface1.methods()).map(MethodMeta::name).containsExactly("mInterface1");

        ClassMeta annotation1 = getClassMeta(actual, "MultipleClassFileAnnotation");
        assertThat(annotation1.methods()).map(MethodMeta::name).containsExactly("value");
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
        Set<ClassMeta> actual = metaJava.getMetaFrom(samplesClassTypes);

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
        Set<ClassMeta> actual = metaJava.getMetaFrom(samplesClassTypes);

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
        Set<ClassMeta> actual = metaJava.getMetaFrom(samplesClassTypes);

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
        Set<ClassMeta> actual = metaJava.getMetaFrom(samplesClassTypes);

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
  class FieldMetaTests {

    @Test
    void readFieldNames() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleClass1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      assertThat(classMeta.fields()).map(FieldMeta::name)
        .containsExactlyInAnyOrder("CONSTANT_1_1", "answer", "notInitVar", "expressionVar");
    }

    @Test
    void readTypes() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleClass1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      assertThat(classMeta.fields()).map(FieldMeta::type)
        .containsExactlyInAnyOrder("String", "int", "Double", "float");
    }

    @Test
    void readInitialValue() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleClass1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      assertThat(classMeta.fields()).map(FieldMeta::initializer)
        .containsExactlyInAnyOrder("constant1-1", "42", null, "1.0F + 1.0F");
    }

    @Test
    void readAnnotationsWhenPresent() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleClass1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      FieldMeta field = getFieldMeta(classMeta, "answer");
      assertThat(field.annotations()).map(AnnotationMeta::name).containsExactly("MeaningOfLifeUniverseAndEverythingElse");
    }

    @Test
    void readNoAnnotationsWhenNotPresent() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleClass1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();
      FieldMeta field = getFieldMeta(classMeta, "expressionVar");
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

      FieldMeta value1 = getFieldMeta(clazz, "VAR1");
      assertThat(value1.name()).isEqualTo("VAR1");
      assertThat(value1.type()).isEqualTo("Enum1");
      assertThat(value1.initializer()).isEqualTo("new Enum1(\"hello\")");
      assertThat(value1.modifiers()).containsExactlyInAnyOrder(PUBLIC, STATIC, FINAL);
      assertThat(value1.annotations()).map(AnnotationMeta::name).containsExactly("Deprecated");

      FieldMeta value2 = getFieldMeta(clazz, "VAR2");
      assertThat(value2.name()).isEqualTo("VAR2");
      assertThat(value2.type()).isEqualTo("Enum1");
      assertThat(value2.initializer()).isEqualTo("new Enum1()");
      assertThat(value2.modifiers()).containsExactlyInAnyOrder(PUBLIC, STATIC, FINAL);
      assertThat(value2.annotations()).isEmpty();

      FieldMeta enumField = getFieldMeta(clazz, "initVar");
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

      FieldMeta param1 = getFieldMeta(classMeta, "param1");
      assertThat(param1.name()).isEqualTo("param1");
      assertThat(param1.type()).isEqualTo("String");
      assertThat(param1.initializer()).isNull();
      assertThat(param1.modifiers()).containsExactlyInAnyOrder(FINAL, PRIVATE);
      assertThat(param1.annotations()).isEmpty();

      FieldMeta param2 = getFieldMeta(classMeta, "param2");
      assertThat(param2.name()).isEqualTo("param2");
      assertThat(param2.type()).isEqualTo("int");
      assertThat(param2.initializer()).isNull();
      assertThat(param2.modifiers()).containsExactlyInAnyOrder(FINAL, PRIVATE);
      assertThat(param2.annotations()).map(AnnotationMeta::name).containsExactly("Deprecated");

      FieldMeta myConst = getFieldMeta(classMeta, "MY_CONST");
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

      FieldMeta myConst = getFieldMeta(classMeta, "MY_CONST_I");
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

      FieldMeta field1 = getFieldMeta(classMeta, "CONSTANT_1_1");
      assertThat(field1.modifiers()).containsExactlyInAnyOrder(PUBLIC, STATIC, FINAL);

      FieldMeta field2 = getFieldMeta(classMeta, "answer");
      assertThat(field2.modifiers()).containsExactlyInAnyOrder(PRIVATE);

      FieldMeta field3 = getFieldMeta(classMeta, "expressionVar");
      assertThat(field3.modifiers()).containsExactlyInAnyOrder(PROTECTED, VOLATILE);
    }

    @Test
    void readClassWithNoModifiers() {
      Set<ClassMeta> actual = metaJava.getMetaFrom(Set.of(sampleClass1));

      ClassMeta classMeta = actual.stream().findFirst().orElseThrow();

      FieldMeta field3 = getFieldMeta(classMeta, "notInitVar");
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

  private FieldMeta getFieldMeta(ClassMeta class1, String fieldName) {
    return class1.fields().stream().filter(f -> fieldName.equals(f.name())).findFirst().orElseThrow();
  }

  private AnnotationArgument getAnnotationArgsMeta(AnnotationMeta annotationMeta, String argName) {
    return annotationMeta.args().stream()
      .filter(AnnotationArgument::hasName)
      .filter(arg -> arg.name().startsWith(argName))
      .findFirst()
      .orElseThrow();
  }


}
