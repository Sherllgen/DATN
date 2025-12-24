package com.project.evgo;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Test class for generating Spring Modulith documentation.
 * <p>
 * This test generates:
 * <ul>
 *   <li>PlantUML component diagrams showing module relationships</li>
 *   <li>Application Module Canvases with detailed module information</li>
 *   <li>An aggregating document linking all generated documentation</li>
 * </ul>
 * <p>
 * Output location: {@code target/spring-modulith-docs/}
 * <p>
 * Run with: {@code mvn test -Dtest=DocumenterTests}
 */
class DocumenterTests {

    ApplicationModules modules = ApplicationModules.of(EvgoApplication.class);

    /**
     * Generates all documentation snippets for the application modules.
     * <p>
     * This includes:
     * <ul>
     *   <li>{@code components.puml} - Main PlantUML diagram with all modules</li>
     *   <li>Individual module PlantUML diagrams</li>
     *   <li>Module canvas Asciidoc files</li>
     *   <li>{@code all-docs.adoc} - Aggregating document</li>
     * </ul>
     */
    @Test
    void writeDocumentation() {
        new Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()
            .writeModuleCanvases()
            .writeAggregatingDocument();
    }
}
