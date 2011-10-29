 
Role Normalization and Synthesis
--------------------------------

The **rns** project is the parent of 3 modules that comprise 
the prototype Java implementation of 
[Role Normalization and Synthesis] [website]. 

[website]: http://www.hawkinssoftware.net/oss/rns

1. a pure Java library [rns-core]
2. an Eclipse AST analyzer [rns-ast-analyzer]
3. a bytecode instrumentation agent [rns-agent]

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

1. Groups Java types into domains using `@DomainRole.Join`
    * `DomainRole` supports sub-domain relationships
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
        + `@InvocationConstraint`: method invocation
        + `@VisibilityConstraint`: type visibility
        + `@ExtensionConstraint`: type extension
    * Permissions assigned by annotation entries specifying:
        + domains
        + package patterns
        + classnames
1. Enforce synchronized field access at runtime
    * Use field annotations `@ValidateRead` and/or `@ValidateWrite`
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
1. Global post-constructor AOP with `@InitializationAspect`
    * Supports interfaces
1. Observe class loading with `ClassLoadObserver`
    * Universal to all ClassLoader instances
    * Filter observation by:
    	+ type hierarchy
    	+ method pattern-matching 

#### Purpose and Philosophy 

      


