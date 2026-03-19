import { Ionicons } from "@expo/vector-icons";
import React, { useState } from "react";
import { ScrollView, Text, View } from "react-native";
import { useSafeAreaInsets, SafeAreaView } from "react-native-safe-area-context";
import Avatar from "@/components/ui/Avatar";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";
import Chip from "@/components/ui/Chip";
import Divider from "@/components/ui/Divider";
import Input from "@/components/ui/Input";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import Modal from "@/components/ui/Modal";

import GradientBackground from "@/components/ui/GradientBackground";

export default function ComponentGallery() {
    const insets = useSafeAreaInsets();

    const [modalVisible, setModalVisible] = useState(false);
    const [modalVariant, setModalVariant] = useState<
        "bottom-sheet" | "centered" | "full-screen"
    >("bottom-sheet");
    const [chip1Selected, setChip1Selected] = useState(false);
    const [chip2Selected, setChip2Selected] = useState(true);
    const [inputValue, setInputValue] = useState("");

    const Section = ({
        title,
        children,
    }: {
        title: string;
        children: React.ReactNode;
    }) => (
        <View className="mb-6">
            <Text className="mb-3 font-bold text-white text-xl">{title}</Text>
            {children}
        </View>
    );

    return (
        <GradientBackground className="flex-1">
            <SafeAreaView className="flex-1" edges={["top", "left", "right"]}>
                <ScrollView
                    className="flex-1 p-4"
                >
            {/* Header */}
            <Text className="mb-6 font-bold text-white text-3xl text-center">
                Component Gallery 🎨
            </Text>

            {/* Buttons */}
            <Section title="Buttons">
                <Card>
                    <Text className="mb-2 text-text-secondary text-sm">
                        Variants
                    </Text>
                    <Button variant="primary" className="mb-2">
                        Primary Button
                    </Button>
                    <Button variant="secondary" className="mb-2">
                        Secondary Button
                    </Button>
                    <Button variant="outline" className="mb-2">
                        Outline Button
                    </Button>
                    <Button variant="ghost" className="mb-2">
                        Ghost Button
                    </Button>
                    <Button variant="danger" className="mb-4">
                        Danger Button
                    </Button>

                    <Divider className="my-4" />

                    <Text className="mb-2 text-text-secondary text-sm">
                        Sizes
                    </Text>
                    <Button size="sm" className="mb-2">
                        Small Button
                    </Button>
                    <Button size="md" className="mb-2">
                        Medium Button
                    </Button>
                    <Button size="lg" className="mb-4">
                        Large Button
                    </Button>

                    <Divider className="my-4" />

                    <Text className="mb-2 text-text-secondary text-sm">
                        States
                    </Text>
                    <Button loading className="mb-2">
                        Loading...
                    </Button>
                    <Button disabled className="mb-2">
                        Disabled Button
                    </Button>
                    <Button fullWidth>Full Width Button</Button>
                </Card>
            </Section>

            {/* Inputs */}
            <Section title="Inputs">
                <Card>
                    <Input
                        label="Rounded Variant"
                        placeholder="Enter your name..."
                        variant="rounded"
                        wrapperClassName="mb-4"
                        value={inputValue}
                        onChangeText={setInputValue}
                    />

                    <Input
                        label="Underline Variant"
                        placeholder="Enter your email..."
                        variant="underline"
                        wrapperClassName="mb-4"
                    />

                    <Input
                        label="Outline Variant"
                        placeholder="Enter text..."
                        variant="outline"
                        wrapperClassName="mb-4"
                    />

                    <Input
                        label="Password Input"
                        placeholder="Enter password..."
                        secureTextEntry
                        variant="rounded"
                        wrapperClassName="mb-4"
                    />

                    <Input
                        label="With Left Icon"
                        placeholder="Search..."
                        leftIcon="search"
                        variant="outline"
                        wrapperClassName="mb-4"
                    />

                    <Input
                        label="With Error"
                        placeholder="Enter text..."
                        error="This field is required"
                        variant="rounded"
                        wrapperClassName="mb-4"
                    />

                    <Input
                        label="With Success"
                        placeholder="Enter text..."
                        successMessage="Looks good!"
                        variant="underline"
                    />
                </Card>
            </Section>

            {/* Cards */}
            <Section title="Cards">
                <Card variant="default" className="mb-3">
                    <Text className="text-white">Default Card</Text>
                    <Text className="text-text-secondary text-sm">
                        With border and light background
                    </Text>
                </Card>

                <Card variant="elevated" className="mb-3">
                    <Text className="text-white">Elevated Card</Text>
                    <Text className="text-text-secondary text-sm">
                        Standard surface background
                    </Text>
                </Card>

                <Card variant="outlined">
                    <Text className="text-white">Outlined Card</Text>
                    <Text className="text-text-secondary text-sm">
                        Transparent with border
                    </Text>
                </Card>
            </Section>

            {/* Modals */}
            <Section title="Modals">
                <Card>
                    <Button
                        variant="outline"
                        className="mb-2"
                        onPress={() => {
                            setModalVariant("bottom-sheet");
                            setModalVisible(true);
                        }}
                    >
                        Open Bottom Sheet
                    </Button>
                    <Button
                        variant="outline"
                        className="mb-2"
                        onPress={() => {
                            setModalVariant("centered");
                            setModalVisible(true);
                        }}
                    >
                        Open Centered Modal
                    </Button>
                    <Button
                        variant="outline"
                        onPress={() => {
                            setModalVariant("full-screen");
                            setModalVisible(true);
                        }}
                    >
                        Open Full Screen
                    </Button>
                </Card>
            </Section>

            {/* Avatars */}
            <Section title="Avatars">
                <Card>
                    <Text className="mb-3 text-text-secondary text-sm">
                        Sizes with Initials
                    </Text>
                    <View className="flex-row items-center gap-3 mb-4">
                        <Avatar size="sm" initials="SM" />
                        <Avatar size="md" initials="MD" />
                        <Avatar size="lg" initials="LG" />
                        <Avatar size="xl" initials="XL" />
                    </View>

                    <Text className="mb-3 text-text-secondary text-sm">
                        With Images (example)
                    </Text>
                    <View className="flex-row gap-3">
                        <Avatar
                            size="md"
                            source="https://i.pravatar.cc/150?img=1"
                        />
                        <Avatar
                            size="md"
                            source="https://i.pravatar.cc/150?img=2"
                        />
                        <Avatar
                            size="md"
                            source="https://i.pravatar.cc/150?img=3"
                        />
                    </View>
                </Card>
            </Section>

            {/* Badges */}
            <Section title="Badges">
                <Card>
                    <Text className="mb-3 text-text-secondary text-sm">
                        Variants
                    </Text>
                    <View className="flex-row flex-wrap gap-2 mb-4">
                        <Badge variant="primary">5</Badge>
                        <Badge variant="success">✓</Badge>
                        <Badge variant="error">!</Badge>
                        <Badge variant="warning">2</Badge>
                        <Badge variant="info">i</Badge>
                    </View>

                    <Text className="mb-3 text-text-secondary text-sm">
                        Sizes
                    </Text>
                    <View className="flex-row items-center gap-3 mb-4">
                        <Badge size="sm" variant="success">
                            SM
                        </Badge>
                        <Badge size="md" variant="success">
                            MD
                        </Badge>
                        <Badge size="lg" variant="success">
                            LG
                        </Badge>
                    </View>

                    <Text className="mb-3 text-text-secondary text-sm">
                        Dot Mode
                    </Text>
                    <View className="flex-row gap-3">
                        <View className="relative">
                            <Ionicons
                                name="notifications"
                                size={28}
                                color="#fff"
                            />
                            <View className="absolute top-0 right-0">
                                <Badge
                                    dot
                                    variant="error"
                                    className="border-2 border-surface-dark"
                                />
                            </View>
                        </View>
                    </View>
                </Card>
            </Section>

            {/* Chips */}
            <Section title="Chips">
                <Card>
                    <Text className="mb-3 text-text-secondary text-sm">
                        Selectable
                    </Text>
                    <View className="flex-row flex-wrap gap-2 mb-4">
                        <Chip
                            label="React Native"
                            selected={chip1Selected}
                            onPress={() => setChip1Selected(!chip1Selected)}
                        />
                        <Chip
                            label="TypeScript"
                            selected={chip2Selected}
                            onPress={() => setChip2Selected(!chip2Selected)}
                        />
                        <Chip label="Expo" />
                    </View>

                    <Text className="mb-3 text-text-secondary text-sm">
                        With Icons
                    </Text>
                    <View className="flex-row flex-wrap gap-2 mb-4">
                        <Chip label="Star" icon="star" selected />
                        <Chip label="Heart" icon="heart" selected />
                        <Chip label="Location" icon="location" />
                    </View>

                    <Text className="mb-3 text-text-secondary text-sm">
                        Removable
                    </Text>
                    <View className="flex-row flex-wrap gap-2">
                        <Chip
                            label="Tag 1"
                            selected
                            onRemove={() =>
                                console.log("Remove tag 1")
                            }
                        />
                        <Chip
                            label="Tag 2"
                            selected
                            onRemove={() =>
                                console.log("Remove tag 2")
                            }
                        />
                    </View>
                </Card>
            </Section>

            {/* Dividers */}
            <Section title="Dividers">
                <Card>
                    <Text className="text-white mb-2">Content Above</Text>
                    <Divider />
                    <Text className="text-white mt-2 mb-2">
                        Content Between
                    </Text>
                    <Divider />
                    <Text className="text-white mt-2">Content Below</Text>
                </Card>
            </Section>

            {/* Loading Spinner */}
            <Section title="Loading Spinner">
                <Card className="h-32">
                    <LoadingSpinner />
                </Card>
            </Section>

            {/* Bottom Padding */}
            <View className="h-8" />

            {/* Modal Component */}
            <Modal
                visible={modalVisible}
                onClose={() => setModalVisible(false)}
                variant={modalVariant}
                title={
                    modalVariant === "bottom-sheet"
                        ? "Bottom Sheet Modal"
                        : modalVariant === "centered"
                            ? "Centered Modal"
                            : "Full Screen Modal"
                }
            >
                <Text className="mb-4 text-white">
                    This is a {modalVariant} modal example.
                </Text>
                <Text className="mb-6 text-text-secondary">
                    You can put any content here. The modal will handle the
                    backdrop, animations, and close button automatically.
                </Text>

                <Button
                    variant="primary"
                    onPress={() => setModalVisible(false)}
                >
                    Close Modal
                </Button>
            </Modal>
                </ScrollView>
            </SafeAreaView>
        </GradientBackground>
    );
}
