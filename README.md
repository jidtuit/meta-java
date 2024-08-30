# meta-java

## TODO list

* ~~Refactor code to reduce cyclomatic complexity~~
* ~~Remove throwing a checked exception~~
* Create Interfaces to:
    * Traverse through the tree in a generic way.
    * ~~Identify elements that can have annotations~~
* Annotations:
    * ~~Split annotation argument in name and value~~
    * Annotation arguments with real types instead of Strings --> Where do we take that info? From the imports? Is there a way to do a
      Class.forName()
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
* Class fields
    * Field name
    * Field type
    * Accessor (public, private, etc)
    * isStatic?
    * Field annotations
* Does record and enum fields are treated as regular fields??
* Imports
    * Split between package and class/wildcard
* Methods
    * Params, param types and param annotations
    * Return type
    * Detect generic types
    * Accessor (public, private, etc)
    * isStatic?
* Interface methods with default implementation
* Add search methods to look for an specific field or method or annotation or argument annotation.
* Enum: Parse fields of the enum
* Support sealed interfaces
* Check if everything works ok with several classes defined in the same java file
* Generics support in:
    * Methods
    * Classes
    * Interfaces
    * Records?
* Multiple class definition in one file

Limitations:

* Annotation of type class or type array returned as a String
* Extends and implements classes returned as a String including generics
* No synthetic methods returned (for records or enums). Only the ones explicitly defined in the source code
