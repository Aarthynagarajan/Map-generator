import apiClient from './apiClient';

export const authService = {
  login: async (credentials: any) => {
    const response = await apiClient.post('/auth/login', credentials);
    return response.data.data;
  },
  register: async (details: any) => {
    const response = await apiClient.post('/auth/register', details);
    return response.data.data;
  },
  getProfile: async () => {
    const response = await apiClient.get('/api/v1/user/profile');
    return response.data.data;
  },
};
