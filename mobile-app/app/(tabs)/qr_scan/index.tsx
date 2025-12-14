import { Feather } from "@expo/vector-icons";
import Ionicons from "@expo/vector-icons/Ionicons";
import { CameraType, CameraView, useCameraPermissions } from "expo-camera";
import { router } from "expo-router";
import { useState } from "react";
import { Button, StyleSheet, Text, TouchableOpacity, View } from "react-native";

export default function QrScanPage() {
    const [facing, setFacing] = useState<CameraType>("back");
    const [permission, requestPermission] = useCameraPermissions();
    const [flashMode, setFlashMode] = useState<"off" | "on">("off");

    if (!permission) {
        return <View />;
    }

    if (!permission.granted) {
        return (
            <View style={{ flex: 1, justifyContent: "center", padding: 20 }}>
                <Text style={styles.message}>
                    Cần quyền truy cập camera để quét mã QR
                </Text>
                <Button onPress={requestPermission} title="Cấp quyền" />
            </View>
        );
    }

    function handleBarCodeScanned({
        type,
        data,
    }: {
        type: string;
        data: string;
    }) {
        alert(`Đã quét được mã QR!\nLoại: ${type}\nDữ liệu: ${data}`);
    }

    function toggleFlash() {
        setFlashMode((current) => (current === "off" ? "on" : "off"));
    }

    return (
        <CameraView
            style={styles.camera}
            facing={facing}
            enableTorch={flashMode === "on"}
            barcodeScannerSettings={{
                barcodeTypes: ["qr"],
            }}
            onBarcodeScanned={handleBarCodeScanned}
        >
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity
                    style={styles.headerButton}
                    onPress={() => router.back()}
                >
                    <Ionicons name="close" size={22} color="#333" />
                </TouchableOpacity>
                <View style={styles.headerRight}>
                    <TouchableOpacity onPress={toggleFlash}>
                        <Ionicons
                            name={
                                flashMode === "on" ? "flash" : "flash-outline"
                            }
                            size={22}
                            color="white"
                        />
                    </TouchableOpacity>
                    <TouchableOpacity>
                        <Feather
                            name="more-horizontal"
                            size={24}
                            color="white"
                        />
                    </TouchableOpacity>
                </View>
            </View>

            {/* Scan Area */}
            <View style={styles.overlay}>
                <View style={styles.scanFrame}>
                    <View style={[styles.corner, styles.cornerTopLeft]} />
                    <View style={[styles.corner, styles.cornerTopRight]} />
                    <View style={[styles.corner, styles.cornerBottomLeft]} />
                    <View style={[styles.corner, styles.cornerBottomRight]} />
                </View>
            </View>

            {/* Bottom Section */}
            <View style={styles.bottomSection}>
                <Text style={styles.scanText}>Recognize QR code</Text>
                <View style={styles.bottomButtons}>
                    <TouchableOpacity style={styles.bottomButton}>
                        <View style={styles.iconCircle}>
                            <Ionicons
                                name="image-outline"
                                size={32}
                                color="white"
                            />
                        </View>
                    </TouchableOpacity>
                </View>
            </View>
        </CameraView>
    );
}

const styles = StyleSheet.create({
    message: {
        textAlign: "center",
        paddingBottom: 30,
        fontSize: 16,
        color: "white",
    },
    camera: {
        flex: 1,
        paddingTop: 30,
        paddingBottom: 40,
    },
    header: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        paddingHorizontal: 20,
        paddingTop: 20,
        paddingBottom: 20,
    },
    headerButton: {
        width: 30,
        height: 30,
        justifyContent: "center",
        alignItems: "center",
        backgroundColor: "#fff",
        borderRadius: 22,
    },
    headerRight: {
        flexDirection: "row",
        gap: 16,
        marginTop: 6,
        paddingRight: 4,
    },
    overlay: {
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
    },
    scanFrame: {
        width: 230,
        height: 230,
        position: "relative",
    },
    corner: {
        position: "absolute",
        width: 40,
        height: 40,
        borderColor: "#4ADE80",
        borderWidth: 4,
    },
    cornerTopLeft: {
        top: 0,
        left: 0,
        borderRightWidth: 0,
        borderBottomWidth: 0,
        borderTopLeftRadius: 8,
    },
    cornerTopRight: {
        top: 0,
        right: 0,
        borderLeftWidth: 0,
        borderBottomWidth: 0,
        borderTopRightRadius: 8,
    },
    cornerBottomLeft: {
        bottom: 0,
        left: 0,
        borderRightWidth: 0,
        borderTopWidth: 0,
        borderBottomLeftRadius: 8,
    },
    cornerBottomRight: {
        bottom: 0,
        right: 0,
        borderLeftWidth: 0,
        borderTopWidth: 0,
        borderBottomRightRadius: 8,
    },
    bottomSection: {
        paddingBottom: 40,
        paddingHorizontal: 20,
    },
    scanText: {
        color: "white",
        fontSize: 14,
        textAlign: "center",
        marginBottom: 30,
        opacity: 0.8,
    },
    bottomButtons: {
        flexDirection: "row",
        justifyContent: "flex-end",
        marginBottom: 20,
    },
    bottomButton: {
        alignItems: "center",
        gap: 8,
    },
    iconCircle: {
        width: 60,
        height: 60,
        borderRadius: 30,
        backgroundColor: "rgba(255, 255, 255, 0.2)",
        justifyContent: "center",
        alignItems: "center",
    },
    bottomButtonText: {
        color: "white",
        fontSize: 14,
    },
});
