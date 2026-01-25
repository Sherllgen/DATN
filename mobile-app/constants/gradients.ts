/**
 * Design System - Gradient Definitions
 * 
 * Centralized gradient configurations for the EV-Go mobile app.
 * These gradients are used with expo-linear-gradient's LinearGradient component.
 */

export const GRADIENTS = {
    /** 
     * Main app gradient - Primary to Black
     * Used for: Home screen, main layouts
     */
    main: {
        colors: ["#33404F", "#000000"] as const,
        start: { x: 0, y: 0 },
        end: { x: 0, y: 1 },
    },

    /**
     * Alternative naming for the same gradient
     */
    primaryDark: {
        colors: ["#33404F", "#000000"] as const,
        start: { x: 0, y: 0 },
        end: { x: 0, y: 1 },
    },
} as const;

/** Type for gradient keys */
export type GradientKey = keyof typeof GRADIENTS;

/** Default gradient used throughout the app */
export const DEFAULT_GRADIENT = GRADIENTS.main;
