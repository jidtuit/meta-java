# meta-java

## TODO list

* ~~Refactor code to reduce cyclomatic complexity~~
* Create Interfaces to:
    * Traverse through the tree in a generic way.
    * ~~Identify elements that can have annotations~~
* Annotations:
    * Split annotation argument in name and value
    * Annotation arguments with real types instead of Strings
    * When annotation has argValue but no argName
        * ~~Remove extra quotation marks~~
        * Return "default argName" instead of returning directly the arg value
* ~~Class annotations~~
* Interfaces implemented by a class
    * Detect generic types?
* Class that inherits from another class
    * Detect generic types?
* Class fields
    * Field name
    * Field type
    * Accessor (public, private, etc)
    * isStatic?
    * Field annotations
* Imports
    * Split between package and class/wildcard
* Methods
    * Params, param types and param annotations
    * Return type
    * Detect generic types
    * Accessor (public, private, etc)
    * isStatic?
* Add search methods to look for an specific field or method or annotation or argument annotation.
* 
