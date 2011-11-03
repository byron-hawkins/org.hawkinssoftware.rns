 
Role Normalization and Synthesis
--------------------------------
#### Reliability Tools and Techniques for Java

The **rns** project is the parent of 3 modules that comprise 
the prototype Java implementation of 
[Role Normalization and Synthesis] [website]. 

[website]: http://www.hawkinssoftware.net/oss/rns

1. [rns-core], a pure Java library 
2. [rns-ast-analyzer], an Eclipse AST analyzer 
3. [rns-agent], a bytecode instrumentation agent 

[rns-core]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/README.md
[rns-ast-analyzer]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-ast-analyzer/blob/master/rns-ast-analyzer/README.md
[rns-agent]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-agent/blob/master/rns-agent/README.md

#### Usage

A developer incorporates the functionality of **RNS** into a
project by:

1. Including a compile-time dependency on the [rns-core]
2. Installing the [rns-ast-analyzer] in their Eclipse workspace
3. Deploying the [rns-agent] with any product that uses the 
   runtime features of **RNS**

For an example of RNS usage, see the [Azia User Interface Library]
[azia].

[azia]: ...

#### Resources

1. All artifacts are made available under the accompanying
   [Eclipse Public License v1.0][License]
2. Source code may be obtained from [GitHub]
3. Snapshots are available from my [Maven repository][snapshots]
4. Documentation can be found at [HawkinsSoftware][website]   

[License]: http://www.eclipse.org/legal/epl-v10.html
[GitHub]: https://www.github.com/byron-hawkins
[snapshots]: https://www.github.com/byron-hawkins/snapshots
   
#### Features

1. Groups Java types into domains using <code>@[DomainRole].Join</code>
    * <code>[DomainRole]</code> supports sub-domain relationships
    * Use domains to enforce orthogonality
        + *Membership*: specify domains which must have no common 
          members
        + *Collaboration* specify domains which must never appear
          on the same call stack
    * Enforce containment: e.g., `DomainA` contains `DomainB`
        + *Membership*: require `B` to have no members outside `A`
        + *Collaboration*: require execution of a call stack 
          entering `B` to remain within `A` until it exits `B`
1. Restrict access to public classes and methods at compile time
    * Restrictions specified by annotation:
        + <code>[@InvocationConstraint]</code>: method invocation
        + <code>[@VisibilityConstraint]</code>: type visibility
        + <code>[@ExtensionConstraint]</code>: type extension
    * Permissions assigned by annotation entries specifying:
        + domains
        + package patterns
        + classnames
1. Enforce synchronized field access at runtime
    * Use field annotations <code>[@ValidateRead]</code> and/or 
      <code>[@ValidateWrite]</code>
    * Wrap collections with access restrictions
1. Object message meta-processing (MOA)
    * Query the call stack for instances by classname
    * Observe every object message on a thread
        + Analyze collaborations among domains
        + Restrict the set of classes contacted by:
            - a method body (shallow or deep)
            - a thread while it holds a certain semaphore
    * Push properties onto the call stack
        + Retract properties explicitly or on method exit
1. Global post-constructor AOP with <code>[@InitializationAspect]</code>
    * Supports interfaces
1. Observe class loading with <code>[ClassLoadObserver]</code>
    * Universal to all ClassLoader instances
    * Filter observation by:
    	+ type hierarchy
    	+ method pattern-matching 

[DomainRole]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/role/DomainRole.java
[@InvocationConstraint]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/publication/InvocationConstraint.java
[@VisibilityConstraint]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/publication/VisibilityConstraint.java
[@ExtensionConstraint]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/publication/ExtensionConstraint.java
[ExecutionPath]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/moa/ExecutionPath.java
[@InitializationAspect]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/aop/InitializationAspect.java
[@ValidateRead]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/validation/ValidateRead.java
[@ValidateWrite]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/validation/ValidateWrite.java
[ClassLoadObserver]: https://github.com/byron-hawkins/org.hawkinssoftware.rns-core/blob/master/rns-core/src/main/java/org/hawkinssoftware/rns/core/aop/ClassLoadObserver.java


      


