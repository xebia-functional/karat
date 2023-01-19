# 💎 Karat

> Alloy within Kotlin

Karat is a DSL for writing and executing [Alloy](https://alloytools.org/) specifications from within Kotlin. The main features are:

- 🕰️ Complete coverage of Alloy's temporal formula language,
- 🧱 _Reflection_ of Kotlin classes and objects as Alloy signatures,
- 🤖 _State machine_ builder which removes the need of manual handling.

There's not much documentation, but you can look at the [examples](https://github.com/47degrees/karat/tree/main/examples/src/main/kotlin/karat/reflect) to get an idea.
In particular, the [`leader_election` folder](https://github.com/47degrees/karat/tree/main/examples/src/main/kotlin/karat/reflect/leader_election)
implements the model from [this tutorial](https://haslab.github.io/formal-software-design/protocol-design/index.html).

## 📖 Alloy documentation

- [Formal Software Design with Alloy 6](https://haslab.github.io/formal-software-design/index.html)
- [Alloy Docs](https://alloy.readthedocs.io/en/latest/)