# meta-java

## TODO list

* ~~Refactor code to reduce cyclomatic complexity~~
* Create Interfaces to:
    * Traverse through the tree in a generic way.
    * ~~Identify elements that can have annotations~~
* Annotations:
    * ~~Split annotation argument in name and value~~
    * Annotation arguments with real types instead of Strings
    * When annotation has argValue but no argName
        * ~~Remove extra quotation marks~~
        * Return "default argName" instead of returning directly the arg value?? --> There's no info of default param in the compilation
          unit
* ~~Class annotations~~
* Interfaces implemented by a class
* Detect generic types in interfaces?
* Class that inherits from another class
    * Detect generic types in inherited classes?
* Class fields
    * Field name
    * Field type
    * Accessor (public, private, etc)
    * isStatic?
    * Field annotations
* Does record fields are treated as regular fields??
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

Limitations:

* Annotation of type class or type array returned as a String
* No synthetic methods returned (for records or any other class). Only the ones explicitly defined 
