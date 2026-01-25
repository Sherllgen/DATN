# EV-Go Mobile App - Design System

> **Project**: EV-Go Mobile Application  
> **Tech Stack**: React Native + Expo + NativeWind (Tailwind CSS) + TypeScript  
> **Analysis Date**: 01/25/2026

---

## 📋 Overview

This Design System was extracted from the Frontend source code of the **EV-Go Mobile App** project - a React Native application built with the Expo framework, using **NativeWind** (Tailwind CSS for React Native) for styling.

### Tech Stack Details

| Technology | Purpose | Version |
|-----------|----------|---------|
| **React Native** | Core framework | 0.81.5 |
| **Expo** | Development platform | ~54.0 |
| **NativeWind** | Styling framework | ^4.2.1 |
| **Tailwind CSS** | CSS utility framework | ^3.4.19 |
| **TypeScript** | Type safety | ~5.9.2 |
| **React Native Paper** | Material Design components | ^5.14.5 |
| **Zustand** | State management | ^5.0.9 |
| **Expo Router** | File-based routing | ~6.0.17 |

---

## 🎨 Design Tokens

### 1. Colors

#### Primary Colors

| Token Name | Hex Code | Usage | Tailwind Class |
|------------|----------|-------|----------------|
| **Primary** | `#33404F` | Main color, background elements, navbar | `bg-primary` |
| **Secondary** | `#00A452` | Accent color for buttons and CTAs | `bg-secondary` |
| **Navbar Color** | `#131315` | Bottom navigation bar background | `bg-navBarColor` |

#### Semantic Colors

| Purpose | Hex Code | Usage | Tailwind Classes |
|---------|----------|-------|------------------|
| **Success/Accent Green** | `#4CAF50` | Success states, primary actions, selected items | `text-[#4CAF50]`, `bg-[#4CAF50]` |
| **Error/Danger** | `#EF4444` | Delete actions, error states | `text-red-500`, `bg-red-500` |
| **Warning** | - | *(Undefined)* | - |

#### Surface Colors

| Surface Type | Light Mode | Dark Mode | Usage |
|--------------|------------|-----------|-------|
| **Background** | `#fff` | `#151718` | Main app background |
| **Surface 1** | - | `#2e2e2e` | Modal backgrounds |
| **Surface 2** | - | `#333739` | Card backgrounds, elevated surfaces |
| **Surface 3** | - | `#3a3a3a` | Interactive item backgrounds |

#### Text Colors

| Text Type | Light Mode | Dark Mode | Usage |
|-----------|------------|-----------|-------|
| **Primary Text** | `#11181C` | `#ECEDEE` | Main content text |
| **Secondary Text** | - | `#9BA1A6` | Labels, helper text, placeholders |
| **Placeholder** | - | `#999` | Input placeholders |
| **Gray Text** | - | `#ccc` | Muted text, descriptions |
| **Light Gray** | - | `#687076` | Icons, subtle text |

#### Border Colors

| Border Type | Hex Code | Usage |
|-------------|----------|-------|
| **Default Border** | `#4A5568` | Form inputs, dividers, card borders |
| **Light Border** | `rgba(255,255,255,0.3)` | Subtle dividers |

#### Tint Colors

| Mode | Hex Code | Usage |
|------|----------|-------|
| **Light Tint** | `#0a7ea4` | Tab navigation selected (light mode) |
| **Dark Tint** | `#fff` | Tab navigation selected (dark mode) |

---

### 2. Typography

#### Font Families

The project uses **system fonts** with fallbacks by platform:

| Platform | Font Stack |
|----------|------------|
| **iOS** | `system-ui` (sans), `ui-serif` (serif), `ui-rounded` (rounded), `ui-monospace` (mono) |
| **Android/Default** | `normal` (sans), `serif`, `monospace` |
| **Web** | `system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif` |

#### Font Sizes & Weights

| Text Type | Size (Tailwind) | Weight | Line Height | Usage Example |
|-----------|----------------|--------|-------------|---------------|
| **Heading XL** | `text-4xl` | `font-bold` | - | Page titles (auth screens) |
| **Heading Large** | `text-lg` | `font-semibold` | - | Modal titles, section headers |
| **Body Base** | `text-base` | `font-medium` / normal | - | Default text, button labels |
| **Body Small** | `text-sm` | `font-medium` / normal | - | Labels, helper text |
| **Caption** | `text-xs` | - | - | *(Not seen in use)* |

**Font Weight Scale:**
- `font-bold` - Main headings
- `font-semibold` - Sub-headings, emphasized text
- `font-medium` - Labels, medium emphasis
- Default (400) - Body text

---

### 3. Spacing & Layout

#### Spacing Scale

The project uses the default Tailwind spacing scale. Commonly used values:

| Tailwind Class | Pixels | Usage |
|----------------|--------|-------|
| `p-2`, `m-2` | 8px | Compact spacing |
| `p-3`, `m-3` | 12px | Standard small spacing |
| `p-4`, `m-4` | 16px | Standard spacing |
| `p-6`, `m-6` | 24px | Large spacing |
| `py-[2px]` | 2px | Minimal vertical padding (inputs) |
| `mb-12` | 48px | Extra large bottom margin (titles) |

#### Layout Patterns

| Pattern | Classes | Usage |
|---------|---------|-------|
| **Container Padding** | `px-4`, `px-6` | Horizontal screen padding |
| **Header Height** | `h-14` | Standard header height (56px) |
| **Vertical Rhythm** | `mb-2`, `mb-3`, `mb-4`, `mb-6` | Stacking elements |
| **Form Field Spacing** | `mt-4`, `mb-6` | Between form fields |
| **Section Spacing** | `py-4`, `pb-8`, `pt-6` | Modal/card internal padding |

---

### 4. Effects

#### Border Radius

| Radius Type | Tailwind Class | Pixels | Usage |
|-------------|----------------|--------|-------|
| **Full Rounded** | `rounded-full` | 9999px | Buttons, inputs, avatar containers |
| **Extra Large** | `rounded-3xl` | 24px | Modal tops |
| **Large** | `rounded-xl` | 12px | Interactive list items |
| **Standard** | `rounded-lg` | 8px | Cards, dropdowns, buttons |

#### Shadows / Elevation

> ⚠️ **Gap**: The project has **no clearly defined shadow system**. `shadow-*` classes are not seen in use.

#### Opacity

| Opacity | Tailwind Syntax | Usage |
|---------|-----------------|-------|
| **60%** | `/60` | Overlay backgrounds (`bg-black/60`) |
| **80%** | `/80` | Modal overlays (`bg-black/80`) |
| **90%** | `/90` | Semi-transparent surfaces (`bg-primary/90`) |
| **20%** | `/20` | Subtle backgrounds (`bg-[#4CAF50]/20`) |
| **15%** | `/15` | Very subtle backgrounds (`bg-primary/15`) |
| **50%** | `/50` | Disabled states (`bg-[#4CAF50]/50`) |

#### Ripple Effects

- **Android Ripple**: `android_ripple={{ color: 'rgba(255,255,255,0.15)', borderless: true }}`
- **Active Opacity**: `activeOpacity={0.7}` or `0.8` for TouchableOpacity

---

## 🧩 Component Library

### Atoms

#### 1. Button

> ⚠️ **Gap**: There is no reusable Button component. Buttons are written inline with `TouchableOpacity`.

**Common Patterns:**

| Variant | Classes | Usage |
|---------|---------|-------|
| **Primary** | `bg-secondary py-4 rounded-full` | Main CTAs (Continue, Save) |
| **Secondary** | `bg-[#4CAF50] py-4 rounded-lg` | Alternate actions |
| **Disabled** | `bg-[#4CAF50]/50` | Disabled state |
| **Icon Button** | `rounded-full w-11 h-11` | Back button, close button |
| **Transparent** | `bg-transparent border border-[#4A5568]` | Outline style (connector tags) |

**Common Props:**
- `activeOpacity={0.7}` - Feedback on press
- `onPress` - Handler function
- `disabled` - Disable state

#### 2. TextInput

**Pattern 1: Rounded Full Input (Auth screens)**
```tsx
<View className="flex-row items-center bg-primary/90 mb-6 px-2 py-[2px] rounded-full">
  <TextInput
    className="flex-1 ml-3 text-white text-base"
    placeholder="..."
    placeholderTextColor="#999"
  />
</View>
```

**Pattern 2: Underline Input (Forms)**
```tsx
<TextInput
  className="pb-3 border-[#4A5568] border-b text-[#4CAF50] text-base"
  placeholderTextColor="#9BA1A6"
/>
```

#### 3. Icon

**Library**: `@expo/vector-icons` (Ionicons, MaterialIcons, Feather, FontAwesome5)

| Icon Set | Usage |
|----------|-------|
| **Ionicons** | General UI icons (chevron, eye, camera, checkmark) |
| **MaterialIcons** | Material design icons (electric-bike, arrow-back) |
| **Feather** | Minimal icons (edit) |
| **FontAwesome5** | Social/specific icons (trash) |

**Common Sizes:** `18`, `20`, `22`, `24`, `28`

---

### Molecules

#### 1. AppHeader

**Location:** `components/ui/AppHeader.tsx`

**Props:**
```typescript
{
  title?: string;
  showBack?: boolean;
  onBack?: () => void;
  right?: React.ReactNode;
  className?: string;
  titleClassName?: string;
  bgClassName?: string;
  safeTop?: boolean;
}
```

**Features:**
- Back button (iOS style arrow)
- Centered title
- Custom right action
- Customizable background

**Layout:** `h-14 px-4 flex-row items-center`

#### 2. Dropdown

**Location:** `components/ui/Dropdown.tsx`

**Props:**
```typescript
{
  label: string;
  value?: string;
  defaultValue?: string;
  items: { label: string; value: string }[];
  onValueChange: (value: string) => void;
  placeholder?: string;
  disabled?: boolean;
}
```

**Features:**
- Controlled/uncontrolled modes
- Chevron icon animation (up/down)
- Selected item highlighting (`bg-[#4CAF50]/20`)
- Border bottom style input
- Expandable options list

**Visual Tokens:**
- Label color: `#9BA1A6`
- Selected/active color: `#4CAF50`
- Border: `#4A5568`

#### 3. ImagePickerModal

**Location:** `components/ui/ImagePickerModal.tsx`

**Props:**
```typescript
{
  visible: boolean;
  onClose: () => void;
  onTakePhoto: () => void;
  onChooseLibrary: () => void;
}
```

**Features:**
- Bottom sheet modal style (`rounded-t-3xl`)
- Two action options (Camera, Library)
- Icon + text layout
- Cancel button
- Swipe indicator (`bg-[#4A5568] rounded-full w-12 h-1`)

#### 4. VehicleCard

**Location:** `components/setting_page/VehicleCard.tsx`

**Features:**
- Icon + brand label
- Model name, connectors info
- Action buttons (Edit, Delete)
- Background: `bg-[#4A5568]/20`
- Border: `border-[#4A5568]`

**Action Button Pattern:**
- Edit: Green accent (`bg-[#4CAF50]/20`)
- Delete: Red accent (`bg-red-500/20`)

#### 5. VehicleFormModal

**Location:** `components/setting_page/VehicleFormModal.tsx`

**Features:**
- Bottom sheet modal
- Brand dropdown
- Model name input
- Multi-select connector chips
- Save button with loading state
- Error message display

**Chip Pattern (Multi-select):**
- Inactive: `bg-transparent border-[#4A5568]`
- Active: `bg-[#4CAF50] border-[#4CAF50]`

---

### Organisms (Screen Sections)

#### 1. PhoneNumberStep (Auth Form)

**Location:** `components/auth/PhoneNumberStep.tsx`

**Features:**
- Form title (`text-4xl font-bold text-center`)
- 4 text inputs (fullname, email, password, confirm)
- Password visibility toggle
- Continue button
- Divider with "or" text
- Sign in link

**Input Pattern:**
- Label above input (`mb-2 ml-4 text-sm`)
- Rounded full wrapper
- Icon on right (password eye icon)

#### 2. OTPVerificationStep

**Location:** `components/auth/OTPVerificationStep.tsx`

**Features:** *(Need to view file for detailed analysis)*

#### 3. TabHeader, BillingGroupSection, MenuItem

**Location:** `components/notifice/`, `components/setting_page/`

**Features:** Section headers, grouped lists, menu items

---

## 💻 Coding Patterns

### 1. Styling Approach

| Aspect | Details |
|--------|---------|
| **Framework** | **NativeWind** (Tailwind CSS for React Native) |
| **Class Syntax** | `className="..."` with Tailwind utility classes |
| **Custom Colors** | Hardcoded hex values in className: `text-[#4CAF50]` |
| **Conditional Classes** | Template literals with ternary: `` `base ${condition ? 'class1' : 'class2'}` `` |
| **Dynamic Classes** | `.join(" ")` pattern for array of classes |
| **Global Styles** | `global.css` with `@tailwind` directives |

**Example:**
```tsx
className={[
  "h-14 px-4 mb-2 flex-row items-center",
  bgClassName,
  className,
].join(" ")}
```

### 2. Naming Conventions

#### Component Files
- **PascalCase**: `AppHeader.tsx`, `VehicleFormModal.tsx`
- **Kebab-case for routes**: `(tabs)/setting/myVehicle.tsx`

#### Component Props
- **camelCase**: `onValueChange`, `showBack`, `titleClassName`
- **Boolean props**: Prefix `is`, `show`, `has` (`isSaving`, `showBack`)
- **Callbacks**: Prefix `on` (`onClose`, `onSave`, `onEdit`)

#### Style Props Convention
- `className` - Main wrapper styles
- `<element>ClassName` - Specific element styles (e.g., `titleClassName`, `bgClassName`)

#### Variables
- **camelCase**: `selectedBrand`, `modelName`, `currentValue`
- **SCREAMING_SNAKE_CASE** for constants/enums: `SOCKET_220V`, `VINFAST_STD`

### 3. State Management

#### Local State (useState)
```tsx
const [showPassword, setShowPassword] = useState(false);
const [isOpen, setIsOpen] = useState(false);
```

#### Controlled Components Pattern
```tsx
// Either controlled or internal state
const currentValue = value !== undefined ? value : internalValue;
```

#### State Lifting
- Parent components manage form data
- Child components receive via props + callbacks
- Example: `PhoneNumberStep` lifts state to parent auth screen

#### Global State (Zustand)
- **Package**: `zustand` (^5.0.9)
- *(Need to view store files for detailed analysis)*

### 4. Component Structure Pattern

**Typical component structure:**

```tsx
// 1. Imports
import { View, Text, TouchableOpacity } from 'react-native';

// 2. Types/Interfaces
interface ComponentProps {
  // ...
}

// 3. Component definition
export default function ComponentName({ props }: ComponentProps) {
  // 4. Hooks (useState, useRef, etc.)
  const [state, setState] = useState();

  // 5. Handlers
  const handleAction = () => { /* ... */ };

  // 6. JSX Return
  return (
    <View className="...">
      {/* Component markup */}
    </View>
  );
}
```

### 5. TypeScript Patterns

#### Union Types for Enums
```tsx
type VehicleBrand = "VINFAST" | "YADEA" | "PEGA" | "ANBICO" | "DAT_BIKE" | "OTHER";
```

#### Interface for Props
```tsx
interface DropdownProps {
  label: string;
  value?: string; // Optional with ?
  items: DropdownItem[];
  onValueChange: (value: string) => void;
}
```

#### Generic Types
```tsx
const [internalValue, setInternalValue] = useState<string>("");
const emailInputRef = useRef<TextInput>(null);
```

### 6. Navigation

- **Router**: Expo Router (file-based routing)
- **Navigation**: `router.back()`, `<Link href="/auth/login">`

---

## 📊 Evaluation & Gaps

### ⚠️ Inconsistencies

| Issue | Evidence | Recommendation |
|-------|----------|----------------|
| **Hard-coded colors** | `text-[#4CAF50]`, `bg-[#333739]` found everywhere | Define in `tailwind.config.js` theme.extend.colors |
| **No Button component** | Every button is written inline with `TouchableOpacity` | Create `Button.tsx` atom with variants (primary, secondary, outline, ghost) |
| **No shadow system** | No elevation/shadow guidelines | Add shadow tokens for depth hierarchy |
| **Inconsistent spacing** | Mix of `py-4`, `py-3`, `py-[2px]` | Standardize spacing scale for each use case |
| **Color naming unclear** | `#4CAF50` used as "green" but no token name | Name it: `accent`, `success`, `primary-green` |
| **Typography scale incomplete** | Missing heading sizes (h1-h6), paragraph sizes | Define full type scale in theme |
| **No warning color** | Only success (green) and error (red) | Add warning (yellow/orange) semantic color |

### 📦 Components "To Be Created"

Based on screens analysis, these components should be created:

| Component | Type | Priority | Description |
|-----------|------|----------|-------------|
| **Button** | Atom | 🔴 High | Reusable button with variants (primary, secondary, outline, text, icon) and sizes (sm, md, lg) |
| **Input** | Atom | 🔴 High | Reusable input with variants (rounded-full, underline, outline) and states (default, focused, error, disabled) |
| **Avatar** | Atom | 🟡 Medium | User avatar component with sizes and fallback |
| **Badge** | Atom | 🟡 Medium | Notification badges, status indicators |
| **Chip/Tag** | Atom | 🟡 Medium | Standardize multi-select chips pattern (currently used in VehicleFormModal) |
| **Divider** | Atom | 🟢 Low | Horizontal/vertical dividers (currently hardcoded `<View className="h-px bg-gray-100/30" />`) |
| **Card** | Molecule | 🔴 High | Container component with consistent padding, border, background |
| **Modal** | Molecule | 🔴 High | Unified modal base (bottom sheet, centered, full-screen) |
| **EmptyState** | Molecule | 🟡 Medium | Styled empty state component (currently have EmptyState.tsx and EmptyVehicleState.tsx - should merge) |
| **LoadingSpinner** | Atom | 🟡 Medium | Loading indicator |
| **Toast/Snackbar** | Molecule | 🟡 Medium | Notification feedback (note: `toastify-react-native` package exists) |
| **TabBar** | Organism | 🟢 Low | Custom tab bar (if needed to override default) |

### 🎯 Recommendations

#### 1. Design Tokens Improvements

**File:** `tailwind.config.js`

```js
module.exports = {
  theme: {
    extend: {
      colors: {
        // Brand
        primary: '#33404F',
        secondary: '#00A452',
        accent: '#4CAF50',
        
        // Semantic
        success: '#4CAF50',
        error: '#EF4444',
        warning: '#F59E0B', // NEW
        info: '#3B82F6',    // NEW
        
        // Surfaces
        surface: {
          DEFAULT: '#2e2e2e',
          light: '#3a3a3a',
          dark: '#131315',
        },
        
        // Text
        text: {
          primary: '#ECEDEE',
          secondary: '#9BA1A6',
          placeholder: '#999',
          muted: '#ccc',
        },
        
        // Borders
        border: {
          DEFAULT: '#4A5568',
          light: 'rgba(255,255,255,0.3)',
        }
      },
      
      // Typography
      fontSize: {
        'heading-xl': ['36px', { lineHeight: '40px', fontWeight: '700' }],
        'heading-lg': ['24px', { lineHeight: '32px', fontWeight: '600' }],
        'heading-md': ['20px', { lineHeight: '28px', fontWeight: '600' }],
        'body-lg': ['18px', { lineHeight: '28px' }],
        'body-md': ['16px', { lineHeight: '24px' }],
        'body-sm': ['14px', { lineHeight: '20px' }],
        'caption': ['12px', { lineHeight: '16px' }],
      },
      
      // Shadows (NEW)
      boxShadow: {
        'sm': '0 1px 2px rgba(0, 0, 0, 0.2)',
        'md': '0 4px 6px rgba(0, 0, 0, 0.3)',
        'lg': '0 10px 15px rgba(0, 0, 0, 0.4)',
      }
    },
  },
};
```

#### 2. Create Component Library

**Priority order:**
1. ✅ `Button.tsx` - Most critical, used everywhere
2. ✅ `Input.tsx` - Second most critical
3. ✅ `Card.tsx` - Container consistency
4. ✅ `Modal.tsx` - Unify modal patterns
5. Remaining atoms and molecules

#### 3. Documentation

- Create **Storybook** or **Component Gallery** screen to showcase components
- Document props with JSDoc comments
- Provide usage examples

#### 4. Accessibility

- Add accessibility labels (`accessibilityLabel`, `accessibilityHint`)
- Ensure touch targets ≥ 44x44 pts (iOS) / 48x48 dp (Android)
- Test with screen readers

---

## 📁 Component Directory Map

```
components/
├── ui/                           # Reusable UI components
│   ├── AppHeader.tsx            ✅ Header with back button
│   ├── Dropdown.tsx             ✅ Dropdown selector
│   ├── ImagePickerModal.tsx     ✅ Image picker bottom sheet
│   ├── collapsible.tsx          ℹ️ Collapsible component
│   ├── icon-symbol.tsx          ℹ️ Icon wrapper
│   ├── [MISSING] Button.tsx     ❌ To be created
│   ├── [MISSING] Input.tsx      ❌ To be created
│   ├── [MISSING] Card.tsx       ❌ To be created
│   └── [MISSING] Modal.tsx      ❌ To be created
│
├── auth/                         # Authentication components
│   ├── PhoneNumberStep.tsx      ✅ Registration form
│   └── OTPVerificationStep.tsx  ✅ OTP input screen
│
├── setting_page/                 # Settings screen components
│   ├── VehicleCard.tsx          ✅ Vehicle display card
│   ├── VehicleFormModal.tsx     ✅ Add/Edit vehicle modal
│   ├── DeleteConfirmModal.tsx   ✅ Confirmation dialog
│   ├── EmptyVehicleState.tsx    ✅ Empty state
│   └── MenuItem.tsx             ✅ Settings menu item
│
├── notifice/                     # Notification components
│   ├── BillingGroupSection.tsx  ✅ Billing section
│   ├── TabHeader.tsx            ✅ Tab header
│   └── EmptyState.tsx           ✅ Empty state (duplicate?)
│
├── themed-text.tsx              ℹ️ Themed text component
├── themed-view.tsx              ℹ️ Themed view wrapper
├── parallax-scroll-view.tsx     ℹ️ Parallax scroll
├── haptic-tab.tsx               ℹ️ Haptic feedback tab
└── hello-wave.tsx               ℹ️ Animated wave component
```

**Legend:**
- ✅ = Exists and in use
- ❌ = To be created (gap)
- ℹ️ = Exists but usage pattern unclear

---

## 🔗 Quick Reference

### Most Used Color Values

```tsx
// Accents & CTAs
bg-[#4CAF50]      // Primary green accent
text-[#4CAF50]    // Green text

// Backgrounds
bg-[#2e2e2e]      // Modal background
bg-[#333739]      // Card background
bg-[#3a3a3a]      // Item background
bg-primary/90     // Semi-transparent primary

// Borders
border-[#4A5568]  // Standard border

// Text
text-[#9BA1A6]    // Secondary text (labels)
text-[#ccc]       // Muted text
placeholderTextColor="#999"  // Placeholder text

// Opacity overlays
bg-black/60       // Modal overlay
bg-red-500/20     // Subtle error background
bg-[#4CAF50]/20   // Subtle success background
```

### Most Used Layout Patterns

```tsx
// Header
className="h-14 px-4 flex-row items-center"

// Full-width button
className="py-4 rounded-full w-full"

// Card
className="bg-[#4A5568]/20 p-4 border border-[#4A5568] rounded-lg"

// Bottom modal
className="bg-[#333739] pb-8 rounded-t-3xl"

// Input wrapper
className="flex-row items-center bg-primary/90 mb-6 px-2 py-[2px] rounded-full"

// Flex centering
className="flex-1 justify-center items-center"
```

---

## 📝 Conclusion

The **EV-Go Mobile App** Design System has a good foundation with:
- ✅ consistent usage of NativeWind/Tailwind CSS
- ✅ Clear component structure
- ✅ Complete TypeScript types
- ✅ Responsive layout patterns

**But needs improvement:**
- ❌ Create design tokens system in tailwind config
- ❌ Build reusable component library (Button, Input, Card, Modal)
- ❌ Standardize naming and color usage
- ❌ Add shadow/elevation system
- ❌ Document components and usage guidelines

**Next Steps:**
1. Refactor `tailwind.config.js` with extended color tokens
2. Create atomic components (Button, Input) in `components/ui/`
3. Migrate hardcoded colors to semantic tokens
4. Write documentation/Storybook for components

---

**📄 This document can be used as a reference for:**
- New developers joining the team
- Design handoff from designers
- Component refactoring roadmap
- Style guide enforcement

*Analysis complete - Design System extracted successfully! 🎉*
