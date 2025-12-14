package com.project.evgo.charger.internal;

import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.SlotResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for Charger and Slot entities to DTOs.
 */
@Component
public class ChargerDtoConverter {

    public ChargerResponse toChargerResponse(Charger charger) {
        return ChargerResponse.builder()
                .id(charger.getId())
                .name(charger.getName())
                .powerOutput(charger.getPowerOutput())
                .status(charger.getStatus())
                .stationId(charger.getStationId())
                .slots(charger.getSlots().stream()
                        .map(this::toSlotResponse)
                        .toList())
                .createdAt(charger.getCreatedAt())
                .build();
    }

    public SlotResponse toSlotResponse(Slot slot) {
        return SlotResponse.builder()
                .id(slot.getId())
                .slotNumber(slot.getSlotNumber())
                .status(slot.getStatus())
                .chargerId(slot.getCharger().getId())
                .createdAt(slot.getCreatedAt())
                .build();
    }

    public List<ChargerResponse> toChargerResponseList(List<Charger> chargers) {
        return chargers.stream()
                .map(this::toChargerResponse)
                .toList();
    }

    public List<SlotResponse> toSlotResponseList(List<Slot> slots) {
        return slots.stream()
                .map(this::toSlotResponse)
                .toList();
    }

    public Optional<ChargerResponse> toChargerResponse(Optional<Charger> charger) {
        return charger.map(this::toChargerResponse);
    }

    public Optional<SlotResponse> toSlotResponse(Optional<Slot> slot) {
        return slot.map(this::toSlotResponse);
    }
}
