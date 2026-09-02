# SMF Core

**SMF Core** - A Modern Industrialization addon focused on custom multiblock machines

## Features

- Custom multiblock machines built on Modern Industrialization's API
- Written in Kotlin for modern, concise code
- Full integration with MI's recipe and power systems

## Dependencies

- Minecraft 1.21.1
- NeoForge 21.1.244
- Modern Industrialization 2.5.4+
- Kotlin for Forge 5.5.0

## Development

This project uses:
- **Kotlin 2.0.0** for all source code
- **Gradle** for build automation
- **Java 21** as the target JVM

### Building

```bash
./gradlew build
```

### Running

```bash
./gradlew runClient
```

## Project Structure

```
src/main/kotlin/com/smf/core/
├── SMFCore.kt           # Main mod class
├── machines/            # Multiblock machine definitions
├── blocks/              # Block registrations
├── items/               # Item registrations  
└── datagen/             # Data generation utilities
```

## Adding Multiblock Machines

See [MULTIBLOCK_GUIDE.md](MULTIBLOCK_GUIDE.md) for detailed instructions on creating custom multiblock machines.

## License

All Rights Reserved

## Authors

SMF Team

## 📚 Documentation

- [MULTIBLOCK_GUIDE.md](MULTIBLOCK_GUIDE.md) - How to add multiblock machines (API usage)
- [MULTIBLOCK_DESIGN_TO_CODE.md](MULTIBLOCK_DESIGN_TO_CODE.md) - From in-game design to code implementation

## 🎮 Quick Start

1. Design your multiblock structure in-game using MI blocks
2. Follow the guide in `MULTIBLOCK_DESIGN_TO_CODE.md` to convert it to code
3. Test in creative mode
4. Add recipes and textures

