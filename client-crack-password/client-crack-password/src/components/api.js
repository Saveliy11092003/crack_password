import axios from "axios";
import API_BASE_URL from "./configs";

export const crackHash = async (hash, length) => {
    const response = await axios.post(`${API_BASE_URL}/crack`, { hash, length: parseInt(length) });
    return response.data;
};

export const getStatus = async (requestId) => {
    const response = await axios.get(`${API_BASE_URL}/status?requestId=${requestId}`);
    return response.data;
};

export const getProgress = async (requestId) => {
    const response = await axios.get(`${API_BASE_URL}/percent?requestId=${requestId}`);
    return response.data;
};