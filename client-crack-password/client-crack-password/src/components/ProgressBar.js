export function ProgressBar({ progress }) {
    return (
        <div style={{ width: "300px", backgroundColor: "#ccc", borderRadius: "4px", marginTop: "20px", margin: "0 auto" }}>
            <div style={{ width: `${progress}%`, backgroundColor: "#4caf50", height: "20px", borderRadius: "4px", textAlign: "center", color: "white" }}>
                {progress}%
            </div>
        </div>
    );
}