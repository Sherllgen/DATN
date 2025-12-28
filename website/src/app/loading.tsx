export default function Loading() {
    return (
        <div className="flex justify-center items-center min-h-screen">
            <div className="text-center">
                <div className="mx-auto border-primary border-b-2 rounded-full w-8 h-8 animate-spin"></div>
                <p className="mt-2 text-muted-foreground">Loading...</p>
            </div>
        </div>
    );
}
