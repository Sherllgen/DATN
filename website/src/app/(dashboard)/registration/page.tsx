"use client";

import { useEffect, useState } from "react";
import { DataTable } from "./components/data-table";

import { getListRegistration } from "@/apis/admin/adminApi";
import { Profile } from "@/types/profileRegistration";

export default function RegistrationPage() {
    const [profiles, setProfiles] = useState<Profile[]>([
        {
            profileId: 1,
            registrationCode: "REG123",
            ownerType: "Individual",
            email: "trunganh@gmail.com",
            pdfFileUrl: "http://example.com/document1.pdf",
            submittedAt: "2024-01-15T10:00:00Z",
            status: "APPROVED",
        },
    ]);

    const handleEditProfile = (profile: Profile) => {
        console.log("Edit profile:", profile);
    };

    const fetchRegistrations = async () => {
        try {
            const res = await getListRegistration();

            setProfiles(res.data.content);
        } catch (error) {
            console.log(error);
        }
    };

    useEffect(() => {
        fetchRegistrations();
    }, []);

    return (
        <div className="flex flex-col gap-4">
            <div className="@container/main px-4 lg:px-6">
                <DataTable
                    profiles={profiles}
                    onEditProfile={handleEditProfile}
                    onStatusChange={fetchRegistrations}
                />
            </div>
        </div>
    );
}
