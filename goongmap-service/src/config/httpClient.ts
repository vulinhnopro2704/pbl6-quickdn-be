import axios from 'axios';
import { camelCaseKeys } from '../utils/camelCase';

const httpClient = axios.create({
  baseURL: process.env.GOONGMAP_API_URL || 'https://rsapi.goong.io/v2/',
  params: {
    api_key: process.env.GOONGMAP_API_KEY,
  },
  timeout: 8000,
});

httpClient.interceptors.response.use(
  (response) => {
    if (response.data && typeof response.data === 'object') {
      response.data = camelCaseKeys(response.data);
    }
    return response;
  },
  (error) => {
    return Promise.reject(error);
  },
);
export default httpClient;
