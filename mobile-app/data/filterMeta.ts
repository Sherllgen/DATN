import { FilterMetadata, StationSearchResult, StationStatus } from "@/types/station.types";

export const mockFilterMetaData = {
  status: 200,
  message: "Success",
  data: {
    minPower: 7.4,
    maxPower: 150,
    connectorTypes: [
      "IEC_TYPE_2",
      "VINFAST_STD"
    ],
    statuses: [
      "ACTIVE",
      "INACTIVE",
      "SUSPENDED"
    ]
  } as FilterMetadata
};

export const mockFilterResponse = {
    status: 200,
    message: "Success",
    data: [
        {
            id: 1,
            ownerId: 101,
            name: "VinFast Landmark 81",
            description: "Premium charging station at Landmark 81",
            address: "720A Dien Bien Phu, Binh Thanh District, HCMC",
            latitude: 10.796,
            longitude: 106.722,
            rate: 4.8,
            status: StationStatus.ACTIVE,
            imageUrls: ["https://example.com/lm81-1.jpg"],
            isFlaggedLowQuality: false,
            availableChargersCount: 4,
            totalChargersCount: 10,
            chargers: [
                { connectorType: "VINFAST_STD", available: 2, total: 5 },
                { connectorType: "IEC_TYPE_2", available: 2, total: 5 }
            ],
            openingHours: [
                { id: 1, dayOfWeek: "MONDAY", openTime: null, closeTime: null, isOpen: true }
            ],
            createdAt: "2026-01-01T00:00:00Z",
            updatedAt: "2026-03-01T00:00:00Z",
            distanceKm: 2.5
        }
    ] as StationSearchResult[]
};
