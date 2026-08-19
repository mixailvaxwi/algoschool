import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

export const TeacherRoute = () => {
    // Достаем статус авторизации и объект пользователя напрямую из Zustand
    const { isAuthenticated, user } = useAuthStore();

    // 1. Если пользователь вообще не вошел в систему -> отправляем на логин
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    // 2. Проверяем наличие нужной роли в объекте пользователя
    const isTeacher = user?.role === 'ROLE_TEACHER';

    // 3. Если он авторизован, но не учитель -> мягко выкидываем в обычный каталог курсов
    if (!isTeacher) {
        return <Navigate to="/courses" replace />;
    }

    // 4. Если всё отлично, разрешаем отрисовку дочерних компонентов (страниц учителя)
    return <Outlet />;
};