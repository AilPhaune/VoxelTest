# VoxelTest

VoxelTest is a simple multiplayer 3D voxel game that contains basic blocks, including dirt, stone, grass, sand, and an assortment of RGB and white lamp variants. Notably, it introduces the innovative concept of vertical slabs. Please note that "VoxelTest" is a temporary project name and may change as the development progresses and the vision for the game evolves.

## Features

- Infinite world generation
- Basic multiplayer functionality, ensuring synchronized world interaction
- Vertical slabs

## Planed features

- Make the world save code more optimized for faster world loading and saving times
- Comprehensive modding support for enhanced customizability
- An actual user interface

## Installation

To get started, download the JAR file from the releases section. VoxelTest operates with Java 8 or later.

## Building from source

If you prefer building the project from the source code, ensure you have a JDK installed. Follow these steps:
1. Create a directory for the project and navigate to it.
2. Clone the repository: `git clone https://github.com/AilPhaune/VoxelTest.git`
3. For Linux: Run `./gradlew desktop:dist`, and for Windows: Use `gradlew desktop:dist`.
4. Locate the generated JAR in `desktop/build/libs`.

## Contribution

VoxelTest welcomes contributions from the community.

### Contributing to the Core Game

If you wish to contribute to the core game, please adhere to the following guidelines:

1. Fork the repository.
2. Create your feature branch.
3. Commit your changes with a descriptive message.
4. Push the branch to your forked repository.
5. Initiate a pull request to propose the incorporation of your changes.

### Creating Mods

While VoxelTest currently does not support mods, rest assured that efforts are underway to implement this feature. Stay tuned for updates as modding capabilities are developed and integrated into the game.

## License

This project is licensed under the [MIT License](LICENSE)