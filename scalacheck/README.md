# Karat 💜 Scalacheck

> Black-box testing of stateful systems using properties

This library provides the ability to generate _traces_ of actions to test _properties_ of a _stateful_ system.
`karat-scalacheck` builds upon the [property-based testing](https://github.com/typelevel/scalacheck/blob/main/doc/UserGuide.md)
capabilities of [Scalacheck](https://scalacheck.org/). In order to express how the system evolves over time, new 
_temporal_ operators have been introduced, such as `always` and `eventually`.

When we test using properties, the inputs are [generated](https://github.com/typelevel/scalacheck/blob/main/doc/UserGuide.md#generators)
randomly, and then fed to the test body, that performs the corresponding checks. In any case, generators represent a
_single_ value. With `karat-scalacheck` you gain the ability to produce arbitrary _traces_ of _actions_, which are applied
in sequence. At each step, properties can be checked.