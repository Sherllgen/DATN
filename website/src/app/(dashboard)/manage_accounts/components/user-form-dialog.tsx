"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { Plus } from "lucide-react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";

const accountFormSchema = z.object({
    name: z.string().min(2, {
        message: "Name must be at least 2 characters.",
    }),
    email: z.string().email({
        message: "Please enter a valid email address.",
    }),
    role: z.string().min(1, {
        message: "Please select a role.",
    }),
    gender: z.string().min(1, {
        message: "Please select a gender.",
    }),
    birthday: z.string().min(1, {
        message: "Please select a birthday.",
    }),
    status: z.string().min(1, {
        message: "Please select a status.",
    }),
});

type AccountFormValues = z.infer<typeof accountFormSchema>;

interface AccountFormDialogProps {
    onAddAccount: (account: AccountFormValues) => void;
}

export function AccountFormDialog({ onAddAccount }: AccountFormDialogProps) {
    const [open, setOpen] = useState(false);

    const form = useForm<AccountFormValues>({
        resolver: zodResolver(accountFormSchema),
        defaultValues: {
            name: "",
            email: "",
            role: "",
            gender: "",
            birthday: "",
            status: "",
        },
    });

    function onSubmit(data: AccountFormValues) {
        onAddAccount(data);
        form.reset();
        setOpen(false);
    }

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button className="cursor-pointer">
                    <Plus className="mr-2 w-4 h-4" />
                    Add New User
                </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-2xl">
                <DialogHeader>
                    <DialogTitle>Add New User</DialogTitle>
                    <DialogDescription>
                        Create a new user account. Click save when you&apos;re
                        done.
                    </DialogDescription>
                </DialogHeader>
                <Form {...form}>
                    <form
                        onSubmit={form.handleSubmit(onSubmit)}
                        className="space-y-4"
                    >
                        <FormField
                            control={form.control}
                            name="name"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Name</FormLabel>
                                    <FormControl>
                                        <Input
                                            placeholder="Enter full name"
                                            {...field}
                                        />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="email"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Email</FormLabel>
                                    <FormControl>
                                        <Input
                                            placeholder="Enter email address"
                                            {...field}
                                        />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <div className="gap-4 grid grid-cols-2">
                            <FormField
                                control={form.control}
                                name="role"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Role</FormLabel>
                                        <Select
                                            onValueChange={field.onChange}
                                            defaultValue={field.value}
                                        >
                                            <FormControl>
                                                <SelectTrigger className="w-full cursor-pointer">
                                                    <SelectValue placeholder="Select role" />
                                                </SelectTrigger>
                                            </FormControl>
                                            <SelectContent>
                                                <SelectItem value="Admin">
                                                    Admin
                                                </SelectItem>
                                                <SelectItem value="Author">
                                                    Author
                                                </SelectItem>
                                                <SelectItem value="Editor">
                                                    Editor
                                                </SelectItem>
                                                <SelectItem value="Maintainer">
                                                    Maintainer
                                                </SelectItem>
                                                <SelectItem value="Subscriber">
                                                    Subscriber
                                                </SelectItem>
                                            </SelectContent>
                                        </Select>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="gender"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Gender</FormLabel>
                                        <Select
                                            onValueChange={field.onChange}
                                            defaultValue={field.value}
                                        >
                                            <FormControl>
                                                <SelectTrigger className="w-full cursor-pointer">
                                                    <SelectValue placeholder="Select plan" />
                                                </SelectTrigger>
                                            </FormControl>
                                            <SelectContent>
                                                <SelectItem value="Basic">
                                                    Basic
                                                </SelectItem>
                                                <SelectItem value="Professional">
                                                    Professional
                                                </SelectItem>
                                                <SelectItem value="Enterprise">
                                                    Enterprise
                                                </SelectItem>
                                            </SelectContent>
                                        </Select>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                        </div>
                        <div className="gap-4 grid grid-cols-2">
                            <FormField
                                control={form.control}
                                name="birthday"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Birthday</FormLabel>
                                        <Select
                                            onValueChange={field.onChange}
                                            defaultValue={field.value}
                                        >
                                            <FormControl>
                                                <SelectTrigger className="w-full cursor-pointer">
                                                    <SelectValue placeholder="Select billing" />
                                                </SelectTrigger>
                                            </FormControl>
                                            <SelectContent>
                                                <SelectItem value="Auto Debit">
                                                    Auto Debit
                                                </SelectItem>
                                                <SelectItem value="UPI">
                                                    UPI
                                                </SelectItem>
                                                <SelectItem value="Paypal">
                                                    Paypal
                                                </SelectItem>
                                            </SelectContent>
                                        </Select>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="status"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Status</FormLabel>
                                        <Select
                                            onValueChange={field.onChange}
                                            defaultValue={field.value}
                                        >
                                            <FormControl>
                                                <SelectTrigger className="w-full cursor-pointer">
                                                    <SelectValue placeholder="Select status" />
                                                </SelectTrigger>
                                            </FormControl>
                                            <SelectContent>
                                                <SelectItem value="Active">
                                                    Active
                                                </SelectItem>
                                                <SelectItem value="Pending">
                                                    Pending
                                                </SelectItem>
                                                <SelectItem value="Error">
                                                    Error
                                                </SelectItem>
                                                <SelectItem value="Inactive">
                                                    Inactive
                                                </SelectItem>
                                            </SelectContent>
                                        </Select>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                        </div>
                        <DialogFooter>
                            <Button type="submit" className="cursor-pointer">
                                Save User
                            </Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    );
}
