import { useState, useEffect } from "react";
import { crackHash, getStatus, getProgress } from "./api";
import { ProgressBar } from "./ProgressBar";

export default function HashCracker() {
    const [hash, setHash] = useState("");
    const [length, setLength] = useState("");
    const [requestId, setRequestId] = useState("");
    const [status, setStatus] = useState("");
    const [data, setData] = useState([]);
    const [progress, setProgress] = useState(0);

    const handleCrack = async () => {
        try {
            const id = await crackHash(hash, length);
            setRequestId(id);
            setProgress(0);
        } catch (error) {
            console.error("Ошибка при отправке запроса:", error);
        }
    };

    const checkStatus = async () => {
        if (!requestId) return;
        try {
            const response = await getStatus(requestId);
            setStatus(response.status);
            setData(response.data || []);
        } catch (error) {
            console.error("Ошибка при проверке статуса:", error);
        }
    };

    useEffect(() => {
        if (requestId) {
            const interval = setInterval(async () => {
                try {
                    const progress = await getProgress(requestId);
                    setProgress(progress);
                } catch (error) {
                    console.error("Ошибка при получении прогресса:", error);
                }
            }, 1000);
            return () => clearInterval(interval);
        }
    }, [requestId]);

    return (
        <div>
            <div>
                <label>Hash:</label>
                <input type="text" value={hash} onChange={(e) => setHash(e.target.value)} />
                <label>Length:</label>
                <input type="number" value={length} onChange={(e) => setLength(e.target.value)} />
                <button onClick={handleCrack}>Crack Hash</button>
            </div>

            <br />
            <div>
                <label>Request ID:</label>
                <input type="text" value={requestId} onChange={(e) => setRequestId(e.target.value)} />
            </div>

            <div>
                <button onClick={checkStatus}>Check Status</button>
            </div>

            {status && (
                <div>
                    <p>Status:</p>
                    <p>{status}</p>
                    {status === "ERROR" && <p>Произошла ошибка в сервисе, результат не полный.</p>}
                </div>
            )}

            <ProgressBar progress={progress} />

            {data.length > 0 && (
                <div>
                    <p>Possible Passwords:</p>
                    <div>
                        {data.map((item, index) => (
                            <div key={index}>{item}</div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}
