# 💎 Karat

> Temporal Logic for JVM

Karat is a DSL to specify systems using [linear temporal logic](https://en.wikipedia.org/wiki/Linear_temporal_logic).
From a single specification you can pursue two different avenues:

- Check the implementation against the model,
  - Using our [integration with Kotest](https://github.com/xebia-functional/karat/tree/main/kotest) if you use Kotlin,
  - Or with our [integration with Scalacheck](https://github.com/xebia-functional/karat-scalacheck) if you use Scala;
- Verify properties of the model using our [integration with Alloy](https://github.com/xebia-functional/karat/tree/main/alloy).

## References

The use of temporal logic to describe program properties has a long history. Some interesting pointers are:

- [Quickstrom's Specification Language](https://docs.quickstrom.io/en/0.5.0/topics/specification-language.html).
  Quickstrom is a tool for testing how a web application behaves when some events like clicking occur. It uses
  temporal logic to express the desired outcome.
- [Formal Software Design with Alloy 6](https://haslab.github.io/formal-software-design/index.html).
  [Alloy](https://alloytools.org/) is a tool for formal modeling and exploration of systems. In Alloy you use temporal 
  logic to both describe the system and specify properties which should be verified.