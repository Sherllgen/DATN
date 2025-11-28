package com.project.evgo;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    @Test
    void verifyModularity() {
        ApplicationModules modules = ApplicationModules.of(EvgoApplication.class);
        modules.verify();
    }
}

