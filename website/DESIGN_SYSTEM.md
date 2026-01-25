# EV-Go Website - Design System

> **Project**: EV-Go Web Application  
> **Tech Stack**: Next.js + React + Tailwind CSS v4 + shadcn/ui + TypeScript  
> **Analysis Date**: 01/25/2026

---

## 📋 Overview

This Design System was extracted from the Frontend source code of the **EV-Go Website** project - a Next.js application built with **Tailwind CSS v4** and **shadcn/ui** component library.

### Tech Stack Details

| Technology | Purpose | Version |
|-----------|----------|---------|
| **Next.js** | React framework | 15.4.7 |
| **React** | UI library | 19.1.0 |
| **Tailwind CSS** | CSS utility framework | ^4.1.11 |
| **TypeScript** | Type safety | ^5 |
| **shadcn/ui** | Component library | Latest |
| **Radix UI** | Headless UI primitives | Latest |
| **Zustand** | State management | ^5.0.7 |
| **next-themes** | Theme management | ^0.4.6 |
| **Lucide React** | Icon library | ^0.536.0 |

---

## 🎨 Design Tokens

### 1. Colors

The website uses a **CSS Custom Properties** approach with **OKLCH color space** for better color consistency and dark mode support.

#### Color System Architecture

| Color Category | Description | Usage |
|---------------|-------------|-------|
| **Semantic Colors** | Base colors defined with OKLCH | Core theme colors (background, foreground, primary, etc.) |
| **Component Colors** | Derived from semantic colors | Card, popover, modal backgrounds |
| **Chart Colors** | Data visualization palette | Recharts, analytics visualizations |
| **Sidebar Colors** | Navigation-specific colors | Left/right sidebar components |

#### Primary Colors (Light Mode)

| Token Name | OKLCH Value | Usage | CSS Variable |
|------------|-------------|-------|--------------|
| **Background** | `oklch(1 0 0)` | Main page background | `--background` |
| **Foreground** | `oklch(0.145 0 0)` | Main text color | `--foreground` |
| **Primary** | `oklch(0.205 0 0)` | Primary brand color, CTAs | `--primary` |
| **Primary Foreground** | `oklch(0.985 0 0)` | Text on primary | `--primary-foreground` |
| **Secondary** | `oklch(0.97 0 0)` | Secondary backgrounds | `--secondary` |
| **Secondary Foreground** | `oklch(0.205 0 0)` | Text on secondary | `--secondary-foreground` |
| **Accent** | `oklch(0.97 0 0)` | Accent elements | `--accent` |
| **Accent Foreground** | `oklch(0.205 0 0)` | Text on accent | `--accent-foreground` |

#### Semantic Colors

| Purpose | Light Mode | Dark Mode | CSS Variable |
|---------|------------|-----------|--------------|
| **Success** | *(Undefined - use chart colors)* | *(Undefined)* | - |
| **Destructive** | `oklch(0.577 0.245 27.325)` | `oklch(0.704 0.191 22.216)` | `--destructive` |
| **Muted** | `oklch(0.97 0 0)` | `oklch(0.269 0 0)` | `--muted` |
| **Muted Foreground** | `oklch(0.556 0 0)` | `oklch(0.708 0 0)` | `--muted-foreground` |

#### Surface Colors

| Surface Type | Light Mode | Dark Mode | Usage |
|--------------|------------|-----------|-------|
| **Card** | `oklch(1 0 0)` | `oklch(0.205 0 0)` | Card backgrounds |
| **Card Foreground** | `oklch(0.145 0 0)` | `oklch(0.985 0 0)` | Text on cards |
| **Popover** | `oklch(1 0 0)` | `oklch(0.205 0 0)` | Dropdown, tooltip backgrounds |
| **Popover Foreground** | `oklch(0.145 0 0)` | `oklch(0.985 0 0)` | Text on popovers |

#### Border & Interactive Colors

| Type | Light Mode | Dark Mode | Usage |
|------|------------|-----------|-------|
| **Border** | `oklch(0.922 0 0)` | `oklch(1 0 0 / 10%)` | Default borders, dividers |
| **Input** | `oklch(0.922 0 0)` | `oklch(1 0 0 / 15%)` | Input borders |
| **Ring** | `oklch(0.708 0 0)` | `oklch(0.556 0 0)` | Focus rings |

#### Chart Colors (Data Visualization)

| Chart Token | Light Mode | Dark Mode | Usage |
|-------------|------------|-----------|-------|
| **Chart 1** | `oklch(0.646 0.222 41.116)` | `oklch(0.488 0.243 264.376)` | Primary data series |
| **Chart 2** | `oklch(0.6 0.118 184.704)` | `oklch(0.696 0.17 162.48)` | Secondary data series |
| **Chart 3** | `oklch(0.398 0.07 227.392)` | `oklch(0.769 0.188 70.08)` | Tertiary data series |
| **Chart 4** | `oklch(0.828 0.189 84.429)` | `oklch(0.627 0.265 303.9)` | Quaternary data series |
| **Chart 5** | `oklch(0.769 0.188 70.08)` | `oklch(0.645 0.246 16.439)` | Quinary data series |

#### Sidebar Colors

| Sidebar Token | Light Mode | Dark Mode | Usage |
|---------------|------------|-----------|-------|
| **Sidebar** | `oklch(0.985 0 0)` | `oklch(0.205 0 0)` | Sidebar background |
| **Sidebar Foreground** | `oklch(0.145 0 0)` | `oklch(0.985 0 0)` | Sidebar text |
| **Sidebar Primary** | `oklch(0.205 0 0)` | `oklch(0.488 0.243 264.376)` | Active navigation items |
| **Sidebar Accent** | `oklch(94.912% 0.00011 271.152)` | `oklch(0.269 0 0)` | Hover states |
| **Sidebar Border** | `oklch(0.922 0 0)` | `oklch(1 0 0 / 10%)` | Sidebar borders |

#### Using Colors in Code

**Tailwind Classes:**
```tsx
// Use semantic color tokens
<div className="bg-background text-foreground">
<Button className="bg-primary text-primary-foreground">
<Card className="border-border bg-card text-card-foreground">
```

**Custom Properties:**
```css
/* Access raw CSS variables */
background-color: var(--primary);
color: var(--foreground);
border-color: var(--border);
```

---

### 2. Typography

#### Font Families

The project uses **Inter** as the primary font family with system font fallbacks:

| Purpose | Font Stack | CSS Variable |
|---------|------------|--------------|
| **Sans Serif** | `var(--font-inter)` | `--font-sans` |
| **Default** | System fonts fallback | - |

**Font Setup:**
```tsx
// In app/layout.tsx
import { Inter } from "next/font/google";

const inter = Inter({ subsets: ["latin"], variable: "--font-inter" });
```

#### Font Sizes & Weights

The website uses Tailwind's default type scale with custom tokens. Common usage patterns:

| Text Type | Tailwind Class | Weight | Usage Example |
|-----------|----------------|--------|---------------|
| **Heading XL** | `text-4xl`, `text-5xl`, `text-6xl` | `font-bold` | Page titles, hero sections |
| **Heading Large** | `text-2xl`, `text-3xl` | `font-semibold`, `font-bold` | Section headers |
| **Heading Medium** | `text-xl` | `font-semibold` | Card titles, modal headers |
| **Body Large** | `text-lg` | `font-medium` | Emphasized body text |
| **Body Base** | `text-base`, `text-sm` | `font-normal`, `font-medium` | Default text, labels |
| **Body Small** | `text-sm` | `font-normal` | Helper text, captions |
| **Caption** | `text-xs` | `font-normal` | Metadata, timestamps |

**Font Weight Scale:**
- `font-bold` (700) - Main headings
- `font-semibold` (600) - Sub-headings, emphasized text
- `font-medium` (500) - Labels, buttons
- `font-normal` (400) - Body text

---

### 3. Spacing & Layout

#### Spacing Scale

The project uses the default Tailwind spacing scale (4px base unit):

| Tailwind Class | Pixels | Usage |
|----------------|--------|-------|
| `p-1`, `m-1` | 4px | Minimal spacing |
| `p-2`, `m-2` | 8px | Compact spacing |
| `p-3`, `m-3` | 12px | Small spacing |
| `p-4`, `m-4` | 16px | Standard spacing |
| `p-6`, `m-6` | 24px | Large spacing |
| `p-8`, `m-8` | 32px | Extra large spacing |
| `p-12`, `m-12` | 48px | Section spacing |

#### Layout Patterns

| Pattern | Classes | Usage |
|---------|---------|-------|
| **Container** | `container mx-auto px-4 md:px-6 lg:px-8` | Page container with responsive padding |
| **Section Spacing** | `py-12 md:py-16 lg:py-24` | Vertical section spacing |
| **Card Padding** | `p-4 md:p-6` | Internal card spacing |
| **Form Field Spacing** | `space-y-4` | Stack form fields |
| **Grid Gap** | `gap-4 md:gap-6` | Grid/flex gap |

#### Responsive Breakpoints

| Breakpoint | Min Width | Tailwind Prefix | Usage |
|------------|-----------|-----------------|-------|
| **sm** | 640px | `sm:` | Mobile landscape |
| **md** | 768px | `md:` | Tablet |
| **lg** | 1024px | `lg:` | Desktop |
| **xl** | 1280px | `xl:` | Large desktop |
| **2xl** | 1536px | `2xl:` | Extra large desktop |

---

### 4. Effects

#### Border Radius

Custom radius system defined in `globals.css`:

| Radius Type | CSS Variable | Value | Tailwind Class | Usage |
|-------------|--------------|-------|----------------|-------|
| **Small** | `--radius-sm` | `calc(var(--radius) - 4px)` | - | Nested elements |
| **Medium** | `--radius-md` | `calc(var(--radius) - 2px)` | - | Cards, inputs |
| **Large** | `--radius-lg` | `var(--radius)` | `rounded-lg` | Default (10px) |
| **Extra Large** | `--radius-xl` | `calc(var(--radius) + 4px)` | - | Modals, large containers |
| **Full** | - | `9999px` | `rounded-full` | Buttons, avatars, badges |

**Default Radius:** `0.625rem` (10px)

#### Shadows

The website uses subtle shadows for depth. Common patterns from shadcn/ui:

| Shadow Level | Usage | Example Component |
|--------------|-------|-------------------|
| **None** | Flat surfaces | Cards on colored backgrounds |
| **Small** | `shadow-sm` | Buttons, input fields |
| **Medium** | `shadow-md` | Cards, dropdowns |
| **Large** | `shadow-lg` | Modals, popovers |
| **Extra Large** | `shadow-xl` | Overlays |

#### Focus States

- **Default Focus Ring**: `outline-ring/50` (applied globally via `@layer base`)
- **Custom Focus**: `focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2`

#### Hover States

```tsx
// Button hover
"hover:bg-primary/90"

// Link hover
"hover:underline"

// Card hover
"hover:shadow-lg transition-shadow"
```

#### Transitions

```tsx
// Default transition
"transition-colors duration-200"

// Smooth transition
"transition-all duration-300"

// Transform transition
"transition-transform hover:scale-105"
```

---

## 🧩 Component Library

### Component Architecture

The website uses **shadcn/ui** - a collection of reusable components built with Radix UI primitives and styled with Tailwind CSS.

**Component Location:** `src/components/ui/`

### Atoms

#### 1. Button

**Location:** `components/ui/button.tsx` ✅

**Variants:**
```tsx
import { Button } from "@/components/ui/button";

// Default
<Button>Click me</Button>

// Variants
<Button variant="default">Default</Button>
<Button variant="destructive">Delete</Button>
<Button variant="outline">Outline</Button>
<Button variant="secondary">Secondary</Button>
<Button variant="ghost">Ghost</Button>
<Button variant="link">Link</Button>

// Sizes
<Button size="default">Default</Button>
<Button size="sm">Small</Button>
<Button size="lg">Large</Button>
<Button size="icon">Icon</Button>
```

**Props Pattern:**
- Uses `class-variance-authority` for variant management
- Extends `React.ButtonHTMLAttributes`
- Supports `asChild` prop for composition (via Radix Slot)

#### 2. Input

**Location:** `components/ui/input.tsx` ✅

**Usage:**
```tsx
import { Input } from "@/components/ui/input";

<Input type="text" placeholder="Enter text..." />
<Input type="email" placeholder="Email" />
<Input type="password" placeholder="Password" />
<Input disabled placeholder="Disabled" />
```

**Features:**
- Styled with `border-input`, `bg-background`
- Focus ring integrated
- Disabled state styling

#### 3. Label

**Location:** `components/ui/label.tsx` ✅

```tsx
import { Label } from "@/components/ui/label";

<Label htmlFor="email">Email</Label>
<Input id="email" />
```

#### 4. Badge

**Location:** `components/ui/badge.tsx` ✅

**Variants:**
```tsx
import { Badge } from "@/components/ui/badge";

<Badge>Default</Badge>
<Badge variant="secondary">Secondary</Badge>
<Badge variant="destructive">Error</Badge>
<Badge variant="outline">Outline</Badge>
```

#### 5. Avatar

**Location:** `components/ui/avatar.tsx` ✅

```tsx
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";

<Avatar>
  <AvatarImage src="/avatar.jpg" alt="User" />
  <AvatarFallback>UN</AvatarFallback>
</Avatar>
```

#### 6. Separator

**Location:** `components/ui/separator.tsx` ✅

```tsx
import { Separator } from "@/components/ui/separator";

<Separator orientation="horizontal" />
<Separator orientation="vertical" />
```

#### 7. Skeleton

**Location:** `components/ui/skeleton.tsx` ✅

```tsx
import { Skeleton } from "@/components/ui/skeleton";

<Skeleton className="w-full h-12" />
```

#### 8. Loading Spinner

**Location:** `components/ui/loading-spinner.tsx` ✅

```tsx
import { LoadingSpinner } from "@/components/ui/loading-spinner";

<LoadingSpinner />
```

---

### Molecules

#### 1. Card

**Location:** `components/ui/card.tsx` ✅

```tsx
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from "@/components/ui/card";

<Card>
  <CardHeader>
    <CardTitle>Card Title</CardTitle>
    <CardDescription>Card description</CardDescription>
  </CardHeader>
  <CardContent>
    Main content
  </CardContent>
  <CardFooter>
    Footer actions
  </CardFooter>
</Card>
```

#### 2. Dialog (Modal)

**Location:** `components/ui/dialog.tsx` ✅

```tsx
import { Dialog, DialogTrigger, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "@/components/ui/dialog";

<Dialog>
  <DialogTrigger asChild>
    <Button>Open Dialog</Button>
  </DialogTrigger>
  <DialogContent>
    <DialogHeader>
      <DialogTitle>Title</DialogTitle>
      <DialogDescription>Description</DialogDescription>
    </DialogHeader>
    <DialogFooter>
      <Button>Close</Button>
    </DialogFooter>
  </DialogContent>
</Dialog>
```

#### 3. Sheet (Slide-out Panel)

**Location:** `components/ui/sheet.tsx` ✅

```tsx
import { Sheet, SheetTrigger, SheetContent, SheetHeader, SheetTitle } from "@/components/ui/sheet";

<Sheet>
  <SheetTrigger asChild>
    <Button>Open Sheet</Button>
  </SheetTrigger>
  <SheetContent side="right">
    <SheetHeader>
      <SheetTitle>Title</SheetTitle>
    </SheetHeader>
    Content
  </SheetContent>
</Sheet>
```

**Sides:** `"left"`, `"right"`, `"top"`, `"bottom"`

#### 4. Dropdown Menu

**Location:** `components/ui/dropdown-menu.tsx` ✅

```tsx
import { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator } from "@/components/ui/dropdown-menu";

<DropdownMenu>
  <DropdownMenuTrigger asChild>
    <Button variant="outline">Menu</Button>
  </DropdownMenuTrigger>
  <DropdownMenuContent>
    <DropdownMenuItem>Item 1</DropdownMenuItem>
    <DropdownMenuSeparator />
    <DropdownMenuItem>Item 2</DropdownMenuItem>
  </DropdownMenuContent>
</DropdownMenu>
```

#### 5. Select

**Location:** `components/ui/select.tsx` ✅

```tsx
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from "@/components/ui/select";

<Select>
  <SelectTrigger>
    <SelectValue placeholder="Select option" />
  </SelectTrigger>
  <SelectContent>
    <SelectItem value="1">Option 1</SelectItem>
    <SelectItem value="2">Option 2</SelectItem>
  </SelectContent>
</Select>
```

#### 6. Popover

**Location:** `components/ui/popover.tsx` ✅

```tsx
import { Popover, PopoverTrigger, PopoverContent } from "@/components/ui/popover";

<Popover>
  <PopoverTrigger asChild>
    <Button>Open</Button>
  </PopoverTrigger>
  <PopoverContent>
    Popover content
  </PopoverContent>
</Popover>
```

#### 7. Tooltip

**Location:** `components/ui/tooltip.tsx` ✅

```tsx
import { Tooltip, TooltipTrigger, TooltipContent, TooltipProvider } from "@/components/ui/tooltip";

<TooltipProvider>
  <Tooltip>
    <TooltipTrigger>Hover me</TooltipTrigger>
    <TooltipContent>
      Tooltip text
    </TooltipContent>
  </Tooltip>
</TooltipProvider>
```

#### 8. Toast

**Location:** `components/ui/sonner.tsx` ✅

```tsx
import { toast } from "sonner";

// Usage
toast.success("Success message");
toast.error("Error message");
toast.info("Info message");
toast.warning("Warning message");
```

#### 9. Form

**Location:** `components/ui/form.tsx` ✅

```tsx
import { Form, FormField, FormItem, FormLabel, FormControl, FormMessage } from "@/components/ui/form";
import { useForm } from "react-hook-form";

const form = useForm();

<Form {...form}>
  <form onSubmit={form.handleSubmit(onSubmit)}>
    <FormField
      control={form.control}
      name="email"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Email</FormLabel>
          <FormControl>
            <Input {...field} />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  </form>
</Form>
```

#### 10. Checkbox

**Location:** `components/ui/checkbox.tsx` ✅

```tsx
import { Checkbox } from "@/components/ui/checkbox";

<Checkbox checked={checked} onCheckedChange={setChecked} />
```

#### 11. Switch

**Location:** `components/ui/switch.tsx` ✅

```tsx
import { Switch } from "@/components/ui/switch";

<Switch checked={enabled} onCheckedChange={setEnabled} />
```

#### 12. Radio Group

**Location:** `components/ui/radio-group.tsx` ✅

```tsx
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";

<RadioGroup value={value} onValueChange={setValue}>
  <RadioGroupItem value="1" id="1" />
  <Label htmlFor="1">Option 1</Label>
  <RadioGroupItem value="2" id="2" />
  <Label htmlFor="2">Option 2</Label>
</RadioGroup>
```

#### 13. Progress

**Location:** `components/ui/progress.tsx` ✅

```tsx
import { Progress } from "@/components/ui/progress";

<Progress value={60} />
```

#### 14. Table

**Location:** `components/ui/table.tsx` ✅

```tsx
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from "@/components/ui/table";

<Table>
  <TableHeader>
    <TableRow>
      <TableHead>Header</TableHead>
    </TableRow>
  </TableHeader>
  <TableBody>
    <TableRow>
      <TableCell>Cell</TableCell>
    </TableRow>
  </TableBody>
</Table>
```

#### 15. Tabs

**Location:** `components/ui/tabs.tsx` ✅

```tsx
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";

<Tabs defaultValue="tab1">
  <TabsList>
    <TabsTrigger value="tab1">Tab 1</TabsTrigger>
    <TabsTrigger value="tab2">Tab 2</TabsTrigger>
  </TabsList>
  <TabsContent value="tab1">Content 1</TabsContent>
  <TabsContent value="tab2">Content 2</TabsContent>
</Tabs>
```

---

### Organisms (Complex Components)

#### 1. Sidebar

**Location:** `components/ui/sidebar.tsx` ✅

**Features:**
- Collapsible sidebar
- Support for left/right placement
- Variants: `sidebar`, `floating`, `inset`
- Mobile responsive

```tsx
import { Sidebar, SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";

<SidebarProvider>
  <Sidebar>
    {/* Sidebar content */}
  </Sidebar>
  <main>
    <SidebarTrigger />
    {/* Page content */}
  </main>
</SidebarProvider>
```

#### 2. App Sidebar

**Location:** `components/app-sidebar.tsx` ✅

Custom sidebar implementation with navigation, user menu, and notifications.

#### 3. Site Header

**Location:** `components/site-header.tsx` ✅

```tsx
import SiteHeader from "@/components/site-header";

<SiteHeader />
```

#### 4. Site Footer

**Location:** `components/site-footer.tsx` ✅

```tsx
import SiteFooter from "@/components/site-footer";

<SiteFooter />
```

#### 5. Navigation Menu

**Location:** `components/ui/navigation-menu.tsx` ✅

```tsx
import { NavigationMenu, NavigationMenuList, NavigationMenuItem, NavigationMenuTrigger, NavigationMenuContent } from "@/components/ui/navigation-menu";

<NavigationMenu>
  <NavigationMenuList>
    <NavigationMenuItem>
      <NavigationMenuTrigger>Menu</NavigationMenuTrigger>
      <NavigationMenuContent>
        Content
      </NavigationMenuContent>
    </NavigationMenuItem>
  </NavigationMenuList>
</NavigationMenu>
```

#### 6. Command (Command Palette)

**Location:** `components/ui/command.tsx` ✅

```tsx
import { Command, CommandInput, CommandList, CommandItem, CommandGroup } from "@/components/ui/command";

<Command>
  <CommandInput placeholder="Search..." />
  <CommandList>
    <CommandGroup heading="Suggestions">
      <CommandItem>Item 1</CommandItem>
      <CommandItem>Item 2</CommandItem>
    </CommandGroup>
  </CommandList>
</Command>
```

#### 7. Accordion

**Location:** `components/ui/accordion.tsx` ✅

```tsx
import { Accordion, AccordionItem, AccordionTrigger, AccordionContent } from "@/components/ui/accordion";

<Accordion type="single" collapsible>
  <AccordionItem value="item-1">
    <AccordionTrigger>Question?</AccordionTrigger>
    <AccordionContent>Answer</AccordionContent>
  </AccordionItem>
</Accordion>
```

#### 8. Calendar

**Location:** `components/ui/calendar.tsx` ✅

```tsx
import { Calendar } from "@/components/ui/calendar";

<Calendar
  mode="single"
  selected={date}
  onSelect={setDate}
/>
```

#### 9. Chart

**Location:** `components/ui/chart.tsx` ✅

Wrapper around Recharts library with consistent theming.

---

## 💻 Coding Patterns

### 1. Styling Approach

| Aspect | Details |
|--------|---------|
| **Framework** | **Tailwind CSS v4** (PostCSS plugin) |
| **Class Syntax** | `className="..."` with Tailwind utility classes |
| **Custom Properties** | OKLCH color space with CSS variables |
| **Conditional Classes** | `cn()` utility from `lib/utils.ts` |
| **Component Variants** | `class-variance-authority` (cva) |
| **Theme** | Inline `@theme` in `globals.css` |

**Example:**
```tsx
import { cn } from "@/lib/utils";

<div className={cn(
  "flex items-center justify-between",
  isActive && "bg-primary text-primary-foreground",
  className
)} />
```

### 2. Naming Conventions

#### File & Component Names
- **PascalCase**: `Button.tsx`, `AppSidebar.tsx`
- **kebab-case for routes**: `(auth)/login/page.tsx`
- **camelCase for utilities**: `utils.ts`, `cn.ts`

#### Component Props
- **camelCase**: `onClick`, `isOpen`, `className`
- **Boolean props**: Prefix `is`, `show`, `has`, `can` (`isOpen`, `showModal`, `hasError`)
- **Callbacks**: Prefix `on` (`onClick`, `onClose`, `onSubmit`)

#### Style Props Convention
- `className` - Main wrapper styles
- `asChild` - Radix Slot composition (render as child element)

#### Variables
- **camelCase**: `userData`, `isLoading`, `formData`
- **SCREAMING_SNAKE_CASE** for constants: `API_BASE_URL`, `MAX_FILE_SIZE`

### 3. State Management

#### Local State (useState)
```tsx
const [isOpen, setIsOpen] = useState(false);
const [formData, setFormData] = useState({ name: "", email: "" });
```

#### Form State (react-hook-form)
```tsx
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

const form = useForm({
  resolver: zodResolver(schema),
  defaultValues: { email: "" },
});
```

#### Global State (Zustand)
```tsx
import { create } from "zustand";

interface Store {
  user: User | null;
  setUser: (user: User) => void;
}

export const useStore = create<Store>((set) => ({
  user: null,
  setUser: (user) => set({ user }),
}));
```

#### Theme State (next-themes)
```tsx
import { useTheme } from "next-themes";

const { theme, setTheme } = useTheme();
```

### 4. Component Structure Pattern

**Typical component structure:**

```tsx
// 1. Imports - External libraries
import * as React from "react";
import { cn } from "@/lib/utils";

// 2. Imports - Internal components
import { Button } from "@/components/ui/button";

// 3. Types/Interfaces
interface ComponentProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: "default" | "outline";
  isOpen?: boolean;
  onClose?: () => void;
}

// 4. Component definition
export default function Component({ 
  variant = "default",
  isOpen,
  onClose,
  className,
  ...props 
}: ComponentProps) {
  // 5. Hooks
  const [state, setState] = React.useState();
  
  // 6. Effects
  React.useEffect(() => {
    // Effect logic
  }, []);
  
  // 7. Handlers
  const handleClick = () => {
    // Handler logic
  };
  
  // 8. JSX Return
  return (
    <div className={cn("base-classes", className)} {...props}>
      {/* Component markup */}
    </div>
  );
}
```

### 5. TypeScript Patterns

#### Interface Extending HTML Attributes
```tsx
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "default" | "outline";
  size?: "sm" | "md" | "lg";
}
```

#### Union Types for Variants
```tsx
type Variant = "default" | "destructive" | "outline" | "secondary" | "ghost" | "link";
```

#### Generic Types
```tsx
interface ApiResponse<T> {
  data: T;
  error?: string;
}

const [data, setData] = useState<User | null>(null);
```

#### Zod Schema Validation
```tsx
import { z } from "zod";

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8),
});

type LoginFormData = z.infer<typeof loginSchema>;
```

### 6. Navigation

- **Router**: Next.js App Router (file-based routing)
- **Navigation**: `useRouter()`, `<Link href="/path" />`
- **Route Groups**: `(auth)`, `(dashboard)` for layout organization

```tsx
import Link from "next/link";
import { useRouter } from "next/navigation";

// Link component
<Link href="/dashboard" className="...">
  Dashboard
</Link>

// Programmatic navigation
const router = useRouter();
router.push("/login");
router.back();
```

---

## 📊 Evaluation & Recommendations

### ✅ Strengths

| Strength | Evidence |
|----------|----------|
| **Comprehensive component library** | 39 shadcn/ui components in `components/ui/` |
| **Consistent theming** | CSS custom properties with OKLCH color space |
| **Type safety** | Full TypeScript coverage |
| **Dark mode support** | Built-in with `next-themes` |
| **Accessibility** | Radix UI primitives provide ARIA support |
| **Modern CSS** | Tailwind CSS v4 with PostCSS plugin |

### 🎯 Recommendations

#### 1. Design Token Documentation

**Action:** Document custom OKLCH color values in this design system.
- Map OKLCH values to hex equivalents for designer handoff
- Create color palette visualization page

#### 2. Component Usage Examples

**Action:** Create a component showcase/storybook page
- Route: `/components` or `/design-system`
- Display all variants and states
- Include code snippets

#### 3. Accessibility Testing

**Action:** Implement accessibility testing
- Add keyboard navigation testing
- Test with screen readers
- Ensure WCAG 2.1 AA compliance

#### 4. Responsive Design Patterns

**Action:** Document responsive patterns
- Mobile-first approach
- Sidebar behavior on mobile
- Form layout patterns

#### 5. Animation & Transitions

**Action:** Define animation design tokens
- Transition durations
- Easing functions
- Micro-interactions

---

## 🔗 Quick Reference

### Color Usage Patterns

```tsx
// Backgrounds
className="bg-background text-foreground"
className="bg-card text-card-foreground"
className="bg-muted text-muted-foreground"

// Borders
className="border border-border"
className="border-input"

// Interactive States
className="hover:bg-accent hover:text-accent-foreground"
className="focus-visible:ring-2 focus-visible:ring-ring"

// Semantic Colors
className="bg-primary text-primary-foreground"
className="bg-secondary text-secondary-foreground"
className="bg-destructive text-destructive-foreground"
```

### Layout Patterns

```tsx
// Centered Container
className="container mx-auto px-4"

// Flex Row
className="flex items-center justify-between gap-4"

// Grid
className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"

// Stack
className="flex flex-col gap-4"

// Full Height
className="min-h-screen flex flex-col"
```

### Component Composition

```tsx
// Button in a Card
<Card>
  <CardHeader>
    <CardTitle>Title</CardTitle>
  </CardHeader>
  <CardContent>
    Content
  </CardContent>
  <CardFooter>
    <Button className="w-full">Action</Button>
  </CardFooter>
</Card>

// Form with Dialog
<Dialog>
  <DialogTrigger asChild>
    <Button>Open Form</Button>
  </DialogTrigger>
  <DialogContent>
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)}>
        {/* Form fields */}
      </form>
    </Form>
  </DialogContent>
</Dialog>
```

---

## 📝 Conclusion

The **EV-Go Website** Design System provides a robust foundation with:
- ✅ Complete shadcn/ui component library
- ✅ Modern theming with OKLCH color space
- ✅ Full TypeScript support
- ✅ Dark mode out of the box
- ✅ Accessibility-first with Radix UI
- ✅ Tailwind CSS v4 for styling

**Best Practices:**
- Use semantic color tokens (`bg-primary`, not hex codes)
- Compose with Radix Slot pattern (`asChild` prop)
- Leverage `cn()` utility for conditional classes
- Follow shadcn/ui component patterns
- Maintain type safety with TypeScript

**Next Steps:**
1. Create component showcase page
2. Document custom components in `components/` directory
3. Add animation design tokens
4. Build design handoff documentation
5. Create accessibility testing workflow

---

**📄 This document can be used as a reference for:**
- New developers joining the team
- Design handoff from designers
- Component usage guidelines
- Code review standards
- Consistency enforcement

*Analysis complete - Design System extracted successfully! 🎉*
