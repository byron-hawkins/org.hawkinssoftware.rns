Core Library for RNS
--------------------
Provides the API and internal implementation for 
[Role Normalization and Synthesis][parent].

[parent]: https://github.com/byron-hawkins/org.hawkinssoftware.rns/blob/master/rns/README.md

#### Artifact

A pure Java library.

#### Installation

Add the jar as a dependency in your Java project. Most features 
also depend on the [Eclipse AST analyzer][rns-ast-analyzer] and 
the [bytecode instrumentation agent][rns-agent].

[rns-ast-analyzer]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-ast-analyzer/blob/master/rns-ast-analyzer/README.md
[rns-agent]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-agent/blob/master/rns-agent/README.md

#### Usage

1. Group types into domains by annotating each domain member
   with <code>@[DomainRole].Join</code>
    * Domain membership is inherited by all subtypes, including
      implementations of an interface that is a domain member
1. To define domain relationships:
    * create a file `<project-name>.domains.xml` in 
      `src/main/resources/rns`
    * specify the domain-scope [DTD] in the XML header
    * Enforce domain orthogonality with `<orthogonal-set>` 
    * Enforce domain containment with `<domain-containment>`
1. Domain features are more fully explained in the 
   [RNS documentation][website]

[DomainRole]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/role/DomainRole.java
[DTD]: http://www.hawkinssoftware.net/dtd/domain-scope-1.4.dtd
[website]: http://www.hawkinssoftware.net/oss/rns
