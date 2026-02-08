import { Redirect } from "expo-router";

/**
 * Root Index - Entry point for the app
 * Redirects to the main home screen
 * Users can access the app without authentication
 */
export default function Index() {
    // Use declarative Redirect instead of imperative router.replace
    return <Redirect href="/(tabs)/home" />;
}
