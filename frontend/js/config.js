// Use the local API for development and the HTTPS Render API for hosted pages.
const localHosts = ['localhost', '127.0.0.1', '[::1]'];
export const API_BASE_URL = localHosts.includes(window.location.hostname)
  ? 'http://localhost:8080/api'
  : 'https://fintrack-api-kaustav.onrender.com/api';
