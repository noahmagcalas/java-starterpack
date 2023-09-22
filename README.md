<div align="center">

<a href="https://mechmania.org"><img width="25%" src="https://github.com/MechMania-29/Website/blob/main/images/mm29_logo.png" alt="MechMania 29"></a>

### [website](https://mechmania.org) | [python-starterpack](https://github.com/MechMania-29/python-starterpack) | java-starterpack | [visualizer](https://github.com/MechMania-29/visualizer) | [engine](https://github.com/MechMania-29/engine) | [wiki](https://github.com/MechMania-29/Wiki)

# MechMania Java Starterpack

Welcome to MechMania! The java starterpack will allow you to write a java bot to compete against others.
Two bots will be faced against each other, and then the [engine](https://github.com/MechMania-29/engine) will run a simulation to see who wins!
After the engine runs you'll get a gamelog (a large json file) that you can use in the [visualizer](https://github.com/MechMania-29/visualizer) to
visualize the game's progress and end result.

</div>

---

# Installation

To start, make sure you have Java 17+ installed. You can check by running:

```sh
java --version
```

You'll also need Python 3.9+, which you can check by running:

```sh
python --version
```

To install the engine, run:

```sh
python engine.py
```

and you should see an engine.jar appear in engine/engine.jar!

If you don't, you can manually install it by following the instructions on the [engine](https://github.com/MechMania-29/engine) page.

# Usage

To modify your strategy, you'll want to edit `strategy/ChooseStrategy.java`.
You should only need to edit files in the strategy directory.

To run your client, you have two options:

You can build & then run directly with java, like:

```sh
./gradlew build
java -jar build/libs/starterpack.jar [commands here]
```

or run with gradle directly:
```sh
./gradlew run --args "[commands here]"
```

We'll show the longer version in these examples but keep in mind you can do both.

### Run your bot against itself

```sh
./gradlew build
java -jar build/libs/starterpack.jar run self
```

### Run your bot against the human computer (your bot plays zombies)

```sh
./gradlew build
java -jar build/libs/starterpack.jar run humanComputer
```

### Run your bot against the zombie computer (your bot plays humans)

```sh
./gradlew build
java -jar build/libs/starterpack.jar run zombieComputer
```

### Serve your bot to a port

You shouldn't need to do this, unless none of the other methods work.
<details>
<summary>Expand instructions</summary>

To serve your bot to a port, you can run it like this:

```sh
./gradlew build
java -jar build/libs/starterpack.jar serve [port]
```

Where port is the port you want to serve to, like 9001 for example:

```sh
./gradlew build
java -jar build/libs/starterpack.jar serve 9001
```

A full setup with the engine might look like (all 3 commands in separate terminal windows):

```sh
java -jar build/libs/starterpack.jar serve 9001
java -jar build/libs/starterpack.jar serve 9001
java -jar engine.jar 9001 9002
```

</details>
