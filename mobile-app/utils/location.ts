/**
 * Calculates the distance between two coordinates in kilometers using the Haversine formula.
 * @param lat1 Latitude of first point
 * @param lon1 Longitude of first point
 * @param lat2 Latitude of second point
 * @param lon2 Longitude of second point
 * @returns Distance in kilometers
 */
export const haversineDistance = (
    lat1: number,
    lon1: number,
    lat2: number,
    lon2: number
): number => {
    const toRad = (value: number) => (value * Math.PI) / 180;

    const R = 6371; // Radius of Earth in km
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(toRad(lat1)) *
            Math.cos(toRad(lat2)) *
            Math.sin(dLon / 2) *
            Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
};

/**
 * Interface for coordinates
 */
interface Point {
    latitude: number;
    longitude: number;
}

/**
 * Calculates perpendicular distance from a point to a line segment
 */
const getPerpendicularDistance = (point: Point, lineStart: Point, lineEnd: Point): number => {
    const x = point.longitude;
    const y = point.latitude;
    const x1 = lineStart.longitude;
    const y1 = lineStart.latitude;
    const x2 = lineEnd.longitude;
    const y2 = lineEnd.latitude;

    const A = x - x1;
    const B = y - y1;
    const C = x2 - x1;
    const D = y2 - y1;

    const dot = A * C + B * D;
    const lenSq = C * C + D * D;
    
    let param = -1;
    if (lenSq !== 0) param = dot / lenSq;

    let xx, yy;

    if (param < 0) {
        xx = x1;
        yy = y1;
    } else if (param > 1) {
        xx = x2;
        yy = y2;
    } else {
        xx = x1 + param * C;
        yy = y1 + param * D;
    }

    const dx = x - xx;
    const dy = y - yy;

    return Math.sqrt(dx * dx + dy * dy);
};

/**
 * Simplifies a polyline using the Douglas-Peucker algorithm
 * @param points Array of coordinates
 * @param tolerance Tolerance in degrees (default 0.0001)
 * @returns Simplified array of coordinates
 */
export const simplifyPolyline = (points: Point[], tolerance: number = 0.0001): Point[] => {
    if (points.length <= 2) return points;

    let maxDist = 0;
    let index = 0;
    const end = points.length - 1;

    for (let i = 1; i < end; i++) {
        const dist = getPerpendicularDistance(points[i], points[0], points[end]);
        if (dist > maxDist) {
            maxDist = dist;
            index = i;
        }
    }

    if (maxDist > tolerance) {
        const left = simplifyPolyline(points.slice(0, index + 1), tolerance);
        const right = simplifyPolyline(points.slice(index), tolerance);
        return [...left.slice(0, -1), ...right];
    } else {
        return [points[0], points[end]];
    }
};
