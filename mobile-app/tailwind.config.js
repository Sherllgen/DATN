/** @type {import('tailwindcss').Config} */
module.exports = {
    // NOTE: Update this to include the paths to all files that contain Nativewind classes.
    content: [
        "./App.tsx",
        "./app/**/*.{js,jsx,ts,tsx}",
        "./components/**/*.{js,jsx,ts,tsx}",
    ],
    presets: [require("nativewind/preset")],
    theme: {
        extend: {
            colors: {
                //Brand
                primary: "#33404F",
                secondary: "#00A452",
                navBarColor: "#131315",
                accent: '#4CAF50',

                // Semantic
                success: '#4CAF50',
                error: '#EF4444',
                warning: '#F59E0B', // NEW
                info: '#3B82F6',    // NEW

                // Surfaces
                surface: {
                    DEFAULT: '#2e2e2e',
                    light: '#3a3a3a',
                    dark: '#131315',
                },

                // Text
                text: {
                    primary: '#ECEDEE',
                    secondary: '#9BA1A6',
                    placeholder: '#999',
                    muted: '#ccc',
                },

                // Borders
                border: {
                    DEFAULT: '#4A5568',
                    light: 'rgba(255,255,255,0.3)',
                }
            },

            // Gradients
            backgroundImage: {
                'gradient-main': 'linear-gradient(to bottom, #33404F, #000000)',
                'gradient-primary-dark': 'linear-gradient(180deg, #33404F 0%, #000000 100%)',
            },
            // Typography
            fontSize: {
                'heading-xl': ['36px', { lineHeight: '40px', fontWeight: '700' }],
                'heading-lg': ['24px', { lineHeight: '32px', fontWeight: '600' }],
                'heading-md': ['20px', { lineHeight: '28px', fontWeight: '600' }],
                'body-lg': ['18px', { lineHeight: '28px' }],
                'body-md': ['16px', { lineHeight: '24px' }],
                'body-sm': ['14px', { lineHeight: '20px' }],
                'caption': ['12px', { lineHeight: '16px' }],
            },

            // Shadows (NEW)
            boxShadow: {
                'sm': '0 1px 2px rgba(0, 0, 0, 0.2)',
                'md': '0 4px 6px rgba(0, 0, 0, 0.3)',
                'lg': '0 10px 15px rgba(0, 0, 0, 0.4)',
            }
        },
    },
    plugins: [],
};
