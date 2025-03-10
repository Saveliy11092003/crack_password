import axios from "axios";

export const crackHash = async (hash, length) => {
    const response = await axios.post("http://localhost:8080/api/hash/crack", { hash, length: parseInt(length) });
    return response.data;
};

export const getStatus = async (requestId) => {
    const response = await axios.get(`http://localhost:8080/api/hash/status?requestId=${requestId}`);
    return response.data;
};

export const getProgress = async (requestId) => {
    const response = await axios.get(`http://localhost:8080/api/hash/percent?requestId=${requestId}`);
    return response.data;
};