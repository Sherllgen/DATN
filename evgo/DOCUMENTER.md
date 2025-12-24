# Spring Modulith Documenter

This project uses **Spring Modulith Documenter** to automatically generate documentation for the modular architecture.

## What Gets Generated

| Output | Description |
|--------|-------------|
| `components.puml` | PlantUML C4 component diagram showing all modules and their relationships |
| `module-{name}.puml` | Individual PlantUML diagrams for each module |
| `module-{name}.adoc` | Application Module Canvas with beans, events, and properties |
| `all-docs.adoc` | Aggregating document linking all diagrams and canvases |

## How to Generate Documentation

Run the documenter test:

```bash
mvn test -Dtest=DocumenterTests
```

## Output Location

All generated files are placed in:

```
target/spring-modulith-docs/
```

## Viewing PlantUML Diagrams

You can view `.puml` files using:

- **IntelliJ IDEA**: Install the "PlantUML Integration" plugin
- **VS Code**: Install the "PlantUML" extension
- **Online**: Paste content into [PlantUML Web Server](http://www.plantuml.com/plantuml/uml)

## Understanding Module Canvases

Each module canvas (`.adoc` file) includes:

| Section | Description |
|---------|-------------|
| **Base Package** | The module's root package |
| **Spring Components** | Services, Repositories, Controllers, and other beans |
| **Aggregate Roots** | JPA entities that are aggregate roots |
| **Published Events** | Events this module publishes |
| **Events Listened To** | Events this module consumes |
| **Configuration Properties** | Spring properties used by this module |

## Integration with Build

To generate documentation during the Maven build, add a profile to `pom.xml`:

```xml
<profile>
    <id>docs</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <includes>
                        <include>**/DocumenterTests.java</include>
                    </includes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

Then run: `mvn test -Pdocs`

## References

- [Spring Modulith Documentation](https://docs.spring.io/spring-modulith/reference/documentation.html)
- [PlantUML C4 Model](https://github.com/plantuml-stdlib/C4-PlantUML)
