@org.springframework.modulith.ApplicationModule(displayName = "OCPP 1.6J Protocol",
        allowedDependencies = {
                "sharedkernel",
                "sharedkernel::events",
                "charger",
                "charger::response",
                "booking"
        })
package com.project.evgo.ocpp;
