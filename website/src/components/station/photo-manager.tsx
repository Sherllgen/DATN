"use client";

import { useState, useRef } from "react";
import axios from "axios";
import { toast } from "react-toastify";
import { Plus, Trash2, Image, Loader2, Upload } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
} from "@/components/ui/dialog";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog";

export interface StationPhoto {
    id: number;
    stationId: number;
    imageUrl: string;
    caption: string | null;
    displayOrder: number;
    createdAt: string;
}

interface PhotoManagerProps {
    stationId: number;
    photos: StationPhoto[];
    onPhotosChange: () => void;
}

const MAX_FILE_SIZE_MB = 5;

export function PhotoManager({ stationId, photos, onPhotosChange }: PhotoManagerProps) {
    const [showAddDialog, setShowAddDialog] = useState(false);
    const [deleteTarget, setDeleteTarget] = useState<StationPhoto | null>(null);
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);
    const [caption, setCaption] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
        const file = e.target.files?.[0];
        if (!file) return;

        // Validate file type
        if (!file.type.startsWith("image/")) {
            toast.error("Please select an image file (JPG, PNG, WebP, etc.)");
            return;
        }

        // Validate file size
        if (file.size > MAX_FILE_SIZE_MB * 1024 * 1024) {
            toast.error(`File size must be under ${MAX_FILE_SIZE_MB}MB`);
            return;
        }

        setSelectedFile(file);

        // Create local preview
        const url = URL.createObjectURL(file);
        setPreviewUrl(url);
    }

    function resetDialog() {
        setSelectedFile(null);
        if (previewUrl) URL.revokeObjectURL(previewUrl);
        setPreviewUrl(null);
        setCaption("");
        if (fileInputRef.current) fileInputRef.current.value = "";
    }

    async function handleAddPhoto() {
        if (!selectedFile) {
            toast.error("Please select an image file");
            return;
        }
        try {
            setSubmitting(true);

            const formData = new FormData();
            formData.append("file", selectedFile);
            if (caption.trim()) {
                formData.append("caption", caption.trim());
            }

            await axios.post(`/api/stations/${stationId}/photos`, formData, {
                withCredentials: true,
                headers: { "Content-Type": "multipart/form-data" },
            });

            toast.success("Photo uploaded successfully");
            setShowAddDialog(false);
            resetDialog();
            onPhotosChange();
        } catch (error: any) {
            toast.error(error.response?.data?.message || "Failed to upload photo");
        } finally {
            setSubmitting(false);
        }
    }

    async function handleDeletePhoto() {
        if (!deleteTarget) return;
        try {
            await axios.delete(`/api/stations/${stationId}/photos/${deleteTarget.id}`, {
                withCredentials: true,
            });
            toast.success("Photo deleted");
            setDeleteTarget(null);
            onPhotosChange();
        } catch (error: any) {
            toast.error(error.response?.data?.message || "Failed to delete photo");
        }
    }

    return (
        <Card>
            <CardHeader>
                <div className="flex items-center justify-between">
                    <div>
                        <CardTitle className="flex items-center gap-2">
                            <Image className="h-5 w-5" />
                            Station Photos
                        </CardTitle>
                        <CardDescription>
                            {photos.length}/10 photos uploaded
                        </CardDescription>
                    </div>
                    <Button
                        type="button"
                        size="sm"
                        onClick={() => setShowAddDialog(true)}
                        disabled={photos.length >= 10}
                        className="cursor-pointer"
                    >
                        <Plus className="h-4 w-4 mr-1" />
                        Add Photo
                    </Button>
                </div>
            </CardHeader>
            <CardContent>
                {photos.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-12 text-muted-foreground">
                        <Image className="h-12 w-12 mb-3 opacity-40" />
                        <p className="text-sm">No photos yet</p>
                        <p className="text-xs">Upload photos to showcase your station</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                        {photos
                            .sort((a, b) => a.displayOrder - b.displayOrder)
                            .map((photo) => (
                                <div
                                    key={photo.id}
                                    className="group relative rounded-lg overflow-hidden border bg-muted aspect-square"
                                >
                                    <img
                                        src={photo.imageUrl}
                                        alt={photo.caption || "Station photo"}
                                        className="w-full h-full object-cover"
                                        onError={(e) => {
                                            (e.target as HTMLImageElement).src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200'%3E%3Crect width='200' height='200' fill='%23f3f4f6'/%3E%3Ctext x='50%25' y='50%25' text-anchor='middle' dy='.3em' fill='%239ca3af' font-size='14'%3EImage unavailable%3C/text%3E%3C/svg%3E";
                                        }}
                                    />
                                    {/* Overlay */}
                                    <div className="absolute inset-0 bg-black/0 group-hover:bg-black/40 transition-all flex items-center justify-center opacity-0 group-hover:opacity-100">
                                        <Button
                                            type="button"
                                            variant="destructive"
                                            size="icon"
                                            className="h-8 w-8 cursor-pointer"
                                            onClick={() => setDeleteTarget(photo)}
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </Button>
                                    </div>
                                    {/* Caption */}
                                    {photo.caption && (
                                        <div className="absolute bottom-0 left-0 right-0 bg-black/60 px-2 py-1">
                                            <p className="text-white text-xs truncate">{photo.caption}</p>
                                        </div>
                                    )}
                                    {/* Order badge */}
                                    <div className="absolute top-2 left-2 bg-black/60 text-white text-xs px-1.5 py-0.5 rounded">
                                        #{photo.displayOrder + 1}
                                    </div>
                                </div>
                            ))}
                    </div>
                )}
            </CardContent>

            {/* Add Photo Dialog */}
            <Dialog open={showAddDialog} onOpenChange={(open) => {
                setShowAddDialog(open);
                if (!open) resetDialog();
            }}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Upload Station Photo</DialogTitle>
                    </DialogHeader>
                    <div className="space-y-4">
                        {/* File picker */}
                        <div>
                            <label className="text-sm font-medium">Image File *</label>
                            <div
                                className="mt-1.5 border-2 border-dashed rounded-lg p-6 text-center cursor-pointer hover:border-primary/50 hover:bg-muted/50 transition-colors"
                                onClick={() => fileInputRef.current?.click()}
                            >
                                {previewUrl ? (
                                    <div className="space-y-3">
                                        <img
                                            src={previewUrl}
                                            alt="Preview"
                                            className="max-h-48 mx-auto rounded-md object-contain"
                                        />
                                        <p className="text-xs text-muted-foreground">
                                            {selectedFile?.name} ({(selectedFile?.size ?? 0 / 1024 / 1024).toFixed(1)} bytes)
                                        </p>
                                        <Button
                                            type="button"
                                            variant="outline"
                                            size="sm"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                resetDialog();
                                            }}
                                            className="cursor-pointer"
                                        >
                                            Choose Different File
                                        </Button>
                                    </div>
                                ) : (
                                    <div className="space-y-2">
                                        <Upload className="h-10 w-10 mx-auto text-muted-foreground" />
                                        <p className="text-sm font-medium">Click to select an image</p>
                                        <p className="text-xs text-muted-foreground">
                                            JPG, PNG, or WebP — max {MAX_FILE_SIZE_MB}MB
                                        </p>
                                    </div>
                                )}
                            </div>
                            <input
                                ref={fileInputRef}
                                type="file"
                                accept="image/*"
                                className="hidden"
                                onChange={handleFileChange}
                            />
                        </div>
                        {/* Caption */}
                        <div>
                            <label className="text-sm font-medium">Caption</label>
                            <Input
                                placeholder="Optional description"
                                value={caption}
                                onChange={(e) => setCaption(e.target.value)}
                            />
                        </div>
                    </div>
                    <DialogFooter>
                        <Button
                            type="button"
                            variant="outline"
                            onClick={() => {
                                setShowAddDialog(false);
                                resetDialog();
                            }}
                            className="cursor-pointer"
                        >
                            Cancel
                        </Button>
                        <Button
                            type="button"
                            onClick={handleAddPhoto}
                            disabled={submitting || !selectedFile}
                            className="cursor-pointer"
                        >
                            {submitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            {submitting ? "Uploading..." : "Upload Photo"}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Delete Confirmation */}
            <AlertDialog open={!!deleteTarget} onOpenChange={(open) => !open && setDeleteTarget(null)}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Delete Photo</AlertDialogTitle>
                        <AlertDialogDescription>
                            Are you sure you want to delete this photo? This action cannot be undone.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel className="cursor-pointer">Cancel</AlertDialogCancel>
                        <AlertDialogAction onClick={handleDeletePhoto} className="cursor-pointer">
                            Delete
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </Card>
    );
}
