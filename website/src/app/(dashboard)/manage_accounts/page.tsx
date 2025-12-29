"use client";

import { useEffect, useState } from "react";
import { DataTable } from "./components/data-table";

import { Accounts, AccountsFormValues } from "@/types/accounts";
import { getListAccounts } from "@/apis/admin/adminApi";

export default function MangeAccountsPage() {
    const [accounts, setAccounts] = useState<Accounts[]>([
        {
            id: 1,
            name: "John Doe",
            email: "trunganh@gmail.com",
            role: "ADMIN",
            gender: "Male",
            birthday: "1990-01-01",
            status: "ACTIVE",
        },
    ]);

    const handleAddAccount = (accountData: AccountsFormValues) => {
        const newAccount: Accounts = {
            id: Math.max(...accounts.map((u) => u.id)) + 1,
            name: accountData.name,
            email: accountData.email,
            role: accountData.role,
            gender: accountData.gender,
            birthday: accountData.birthday,
            status: accountData.status,
        };
        setAccounts((prev) => [newAccount, ...prev]);
    };

    const handleDeleteAccount = (id: number) => {
        setAccounts((prev) => prev.filter((account) => account.id !== id));
    };

    const handleEditAccount = (account: Accounts) => {
        console.log("Edit account:", account);
    };

    const fetchAccounts = async () => {
        try {
            const res = await getListAccounts();

            const data = res.data.content.map((item: any) => ({
                id: item.id,
                name: item.fullName,
                email: item.email,
                role: item.roles[0],
                gender: item.gender,
                birthday: item.birthday,
                status: item.status,
            }));

            console.log("data", data);

            setAccounts(data);
        } catch (error) {
            console.log(error);
        }
    };

    useEffect(() => {
        fetchAccounts();
    }, []);

    return (
        <div className="flex flex-col gap-4">
            <div className="@container/main px-4 lg:px-6">
                <DataTable
                    accounts={accounts}
                    onDeleteAccount={handleDeleteAccount}
                    onEditAccount={handleEditAccount}
                    onAddAccount={handleAddAccount}
                />
            </div>
        </div>
    );
}
