export interface Accounts {
    id: number;
    name: string;
    email: string;
    role: string;
    gender: string;
    birthday: string;
    status: string;
}

export interface AccountsFormValues {
    name: string;
    email: string;
    role: string;
    gender: string;
    birthday: string;
    status: string;
}

export type AccountStatus = "ACTIVE" | "INACTIVE" | "BLOCKED" | "DELETED";
