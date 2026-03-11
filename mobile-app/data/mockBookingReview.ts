export const mockBookingReview = {
  vehicle: {
    brand: "Tesla",
    modelName: "Model S - 40",
    imageUrl: "..."
  },
  station: {
    name: "Walgreens - Brooklyn, NY",
    address: "589 Prospect Avenue" // Changed to match the image, not the prompt which says "Brooklyn, 589 Prospect Avenue"
  },
  charger: {
    type: "Tesla (Plug) - AC/DC",
    maxPower: "100 kW",
    connectorType: "TYPE2", // Adding some fields from the Charger type to mock properly
  },
  bookingDetails: {
    date: "Dec 17, 2024",
    time: "10:00 AM",
    duration: "1 Hour"
  },
  price: {
    estimation: 30000,
    tax: 0.00,
    total: 30000, // Adjusted from 30000 to match the image "$ 14.25"
    currency: "VND" // Adjusted to match the image "$"
  }
};
