export interface Profile {
    profileId: number;
    registrationCode: string;
    ownerType: string;
    email: string;
    pdfFileUrl: string;
    submittedAt: string;
    status: ProfileStatus;
}

export interface ProfileFormValues {
    registrationCode: string;
    ownerType: string;
    email: string;
    pdfFileUrl: string;
}

export type ProfileStatus =
    | "SUBMITTED"
    | "UNDER_REVIEW"
    | "APPROVED"
    | "REJECTED";

export type OwnerType = "ENTERPRISE" | "INDIVIDUAL";
