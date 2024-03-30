# kt-generator library
Library that generate kotlin file from file template

## Description

Library is used to generate kotlin language code from template files.
Template support three keywords:
* #foreach
* #if 
* #else

To generate code:
* prepare code template and save as [template_name].template
* use TemplateLoader class to load template from file (load method return object of TemplateClass): 
```kotlin
    TemplateLoader.load("[template_name]")
```
* To add some variable to TemplateClass use addVariable method: 
```kotlin
    val templateClass = TemplateLoader.load("[template_name]")
    templateClass.addVariable("name", "value")
```
* Call compile method to generate kotlin file
```kotlin
    templateClass.compile()
```

### Template file
Simple template file looks like:

```kotlin
// filename generated_class.template, placed in resources/template

package ${classPackage}.${className}Generated
        
class ${className}Generated {
    fun ${methodName}(): Bolean {
        return true
    }
}
```

To properly generate kotlin file we need variables: className, methodName, classPackage
Code to generate kotlin file from above template could by looks like:

```kotlin
    val templateClass = TemplateLoader.load("generated_class")
    templateClass.addVariable("className", "MyClass")
    templateClass.addVariable("classPackage", "com.example")
    templateClass.addVariable("methodName", "isOk")
    val generatedClass = templateClass.compile()
```

generated class:

```kotlin
package com.example.MyClassGenerated
        
class MyClassGenerated {
    fun isOk(): Bolean {
        return true
    }
}
```

### Foreach

Foreach is a command for more advance code generation 
Can be used to generate some part of code multiple time from some variable (map or list)
To end foreach statement use #end keyword
Foreach contains three main part:
* source - name of the variable use to generate code
* key -> value - name for key and value extracted from source
* separator - this parameter is optional, if will be empty there will be no separation sign

#### Example

Generate map from $fields variable

```kotlin
    val result = mapOf<String, Any?>(
    #foreach($fields as key -> value, separator=","):
        $key to "$value)"
    #end
    )
```

For fields declared as: 
```kotlin 
    val fields = mapOf(Pair(1, "Test"), Pair(2, "Test2"))
```

generated code will be looks like: 

```kotlin
    val result = mapOf<String, Any?>(
        1 to "Test",
        2 to "Test2"
    )
```


### if - else

Another advanced command is if else condition that will be called if some condition passed
To use it called 
```kotlin 
#if([your_condition]): [command] #else [some_other_command] #endif
```

#else is optional

#### Example

```kotlin
package ${classPackage}.${className}Generated
        
#if("$someImport" != ""): import $someImport #else import com.example.default #endif
        
class ${className}Generated {
    fun ${methodName}(): Bolean {
        return true
    }
}
```
