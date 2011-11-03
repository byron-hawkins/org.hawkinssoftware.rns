
RNS Bytecode Instrumentation Agent
----------------------------------
The runtime component of [Role Normalization and Synthesis]
[parent], this bytecode instrumentation agent implements its 
aspect-oriented and message-oriented features.

[parent]: https://github.com/byron-hawkins/org.hawkinssoftware.rns/blob/master/rns/README.md

#### Artifact

A pure Java library, with premain-class 
`org.hawkinssoftware.rns.agent.RNSInstrumentationAgent`

#### Installation

A VM argument specifies loading of the agent:

    -javaagent:<path/to/rns-agent>.jar

The path is relative to the initial working directory of the JVM.

#### Dependent Features

The following features require the agent to be installed:

1. <code>[ExecutionPath]</code> and related functionality
2. <code>[@InitializationAspect]</code>
3. <code>[@ValidateRead]</code> and <code>[@ValidateWrite]</code>
4. <code>[ClassLoadObserver]</code>

[ExecutionPath]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/moa/ExecutionPath.java
[@InitializationAspect]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/aop/InitializationAspect.java
[@ValidateRead]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/validation/ValidateRead.java
[@ValidateWrite]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/validation/ValidateWrite.java
[ClassLoadObserver]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/aop/ClassLoadObserver.java

#### Known Issues

1. Profilers using bytecode instrumentation will usually have
   conflicts with the RNS agent and fail to connect.
2. Performance is quite poor for certain features, many of which
   would be more properly implemented in the [AST analyzer]
   [rns-ast-analyzer]; please consider that this is a prototype 
   implementation.
   
[rns-ast-analyzer]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-ast-analyzer/blob/master/rns-ast-analyzer/README.md