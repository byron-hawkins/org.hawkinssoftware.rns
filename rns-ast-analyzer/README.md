RNS Java AST Analyzer for Eclipse
---------------------------------
The compile-time component of [Role Normalization and Synthesis]
[parent], this Eclipse plugin implements publication constraints
on public Java types and methods. 

[parent]: https://github.com/byron-hawkins/org.hawkinssoftware.rns/blob/master/rns/README.md

#### Artifact

An Eclipse plugin containing:

1. **RNS Build Analyzer**, a project builder
2. **RNS Project Nature**
    * extends the core Java Nature
3. Problem markers for calling out code that violates the 
   developer's architectural constraints

#### Installation

1. Install the plugin into your Eclipse workspace.
2. Activate the analyzer for any project by right-clicking it in
   the Package Explorer and selecting **Toggle RNS Nature**.

#### Usage

The developer specifies publication constraints using annotations:

1. <code>[@InvocationConstraint]</code> restricts invocation of 
   methods
2. <code>[@VisibilityConstraint]</code> restricts visibility of 
   types
3. <code>[@ExtensionConstraint]</code> restricts extension of 
   types (including implementation of interfaces)

Workspace errors will indicate any violations to RNS constraints 
(within any project having the RNS Nature)

[@InvocationConstraint]: https://github.com/byron-hawkins/org.hawkinssoftware.rns/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/publication/InvocationConstraint.java
[@InvocationConstraint]: https://github.com/byron-hawkins/org.hawkinssoftware.rns/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/publication/InvocationConstraint.java
[@VisibilityConstraint]: https://github.com/byron-hawkins/org.hawkinssoftware.rns/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/publication/VisibilityConstraint.java
[@VisibilityConstraint]: https://github.com/byron-hawkins/org.hawkinssoftware.rns/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/publication/VisibilityConstraint.java
[@ExtensionConstraint]: https://github.com/byron-hawkins/org.hawkinssoftware.rns/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/publication/ExtensionConstraint.java
[@ExtensionConstraint]: https://github.com/byron-hawkins/org.hawkinssoftware.rns/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/publication/ExtensionConstraint.java

#### Known Issues

1. Performance is quite poor, especially for large projects with
   heavily integrated class hierarchies. Many improvements can be
   made, but this is only a prototype and does not warrant the 
   effort.
2. The plugin currently is not able to identify every violation 
   during incremental builds. It overlooks a few, and will find 
   them when a directly related file is touched. This is a bug and 
   will be fixed.

