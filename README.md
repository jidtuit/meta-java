# meta-java

## TODO list

* ~~Refactor code to reduce cyclomatic complexity~~
* ~~Remove throwing a checked exception~~
* ~~Create Interfaces to Identify elements that can have annotations~~
* ~~Annotations~~:
    * ~~Split annotation argument in name and value~~
    * ~~When annotation has argValue but no argName~~
        * ~~Remove extra quotation marks~~
* ~~Class annotations~~
* ~~Interfaces implemented by a class~~
    * ~~Do not support implements:~~
        * ~~Interfaces, annotations~~
    * ~~Support implements:~~
        * ~~Classes, records, enums~~
* ~~Detect generic types in interfaces? --> As Strings~~
* ~~Class that inherits from another class~~
* ~~Detect generic types in inherited classes? --> Yes as Strings~~
* ~~Class fields~~
    * ~~Field name~~
    * ~~Field type~~
    * ~~Accessor (public, private, etc)~~
    * ~~isStatic?~~
    * ~~Field annotations~~
* ~~Does record and enum fields are treated as regular fields?? --> YES~~
* ~~Methods~~
    * ~~Params, param types and param annotations. Also includes varArgs~~
    * ~~Return type~~
    * ~~Throws exceptions~~
    * ~~Class/Record/Enum constructors -> Constructors are methods with returnType == null~~
    * ~~Modifiers:~~
        * ~~Accessor (public, private, etc)~~
        * ~~isStatic?~~
* ~~Enum: Parse fields of the enum~~
* ~~Multiple class definition in one file~~
* ~~Interface methods with default implementation marker~~
* Nested classes
* Support sealed interfaces -> Sealed should be one of the modifiers of an interface
* Traverse through the meta information tree in a generic way.
* Add search methods to look for an specific field or method or annotation or argument annotation.
* Test missing modifiers (strictfp, native, abstract, etc)
* Split MetaJavaTest into different classes
* Generics support in:
    * Methods
    * Classes
    * Interfaces
    * Records
* Imports: Split between package and class/wildcard -> Breaks backward compatibility
* Annotation arguments with real types instead of Strings --> Where do we take that info? From the imports? Is there a way to do a
  Class.forName()

Limitations:

* Annotation of type class or type array returned as a String
* Extends and implements classes returned as a String including generics
* No synthetic methods returned (for records or enums). Only the ones explicitly defined in the source code
* ... but there are synthetic modifiers for fields
* Float values in initializers are transformed by the java compiler. For example:
    * Source code: Double myVar = 42;
    * Is returned by the library as a field with the initializer that equals to 42.0F.
* It doesn't read the body of constructors, methods, default implementations or static blocks for initializing.
    * If there is a method of an interface with default implementation then it will also have the DEFAULT modifier.
* Constructors are regular methods with return type == null. There is a isConstructor() convenience method.
    * All the constructor of a class has the same name (`"<init>"`) but different parameters.
* Local and anonymous classes not supported. Static and inner (non-static) classes are supported.

