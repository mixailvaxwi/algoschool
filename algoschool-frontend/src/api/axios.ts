import axios from 'axios';

export const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json'
    }
});

// Интерцептор для добавления JWT токена к каждому запросу
apiClient.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

// Интерцептор ответов.
//
// Разлогиниваем ТОЛЬКО на 401 (нет токена / токен протух). 403 означает
// «вошёл, но прав на это действие нет» — раньше он тоже сбрасывал сессию,
// и любая попытка открыть чужой курс выкидывала пользователя из аккаунта.
// Теперь 403 просто отдаётся вызывающему коду, чтобы страница показала ошибку.
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user_profile');

            // Не зацикливаем редирект, если мы уже на странице входа.
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }

        return Promise.reject(error);
    }
);
