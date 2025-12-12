import React, { createContext, ReactNode, useContext, useState } from "react";

type AppState = "auth" | "authenticated";

interface AuthContextType {
    appState: AppState;
    setAppState: (state: AppState) => void;
    login: () => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [appState, setAppState] = useState<AppState>("auth");

    const login = () => {
        console.log("User logged in");
        setAppState("authenticated");
    };

    const logout = () => {
        setAppState("auth");
    };

    return (
        <AuthContext.Provider
            value={{
                appState,
                setAppState,
                login,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
};
