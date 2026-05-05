import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

export const TeacherRoute = () => {
    const { isAuthenticated, user } = useAuthStore();

    // ВЫВОДИМ В КОНСОЛЬ ТО, ЧТО ВИДИТ ФРОНТЕНД
    console.log("Профиль пользователя:", user);

    // Проверяем, авторизован ли пользователь и имеет ли он роль ADMIN
    // (Убедитесь, что ваш Spring Boot возвращает правильное название роли, например "ADMIN" или "ROLE_ADMIN")
    const isAdmin = isAuthenticated && user && (user.role === 'TEACHER' || user.role === 'ROLE_TEACHER');

    if (!isAdmin) {
        // Если это не админ, отправляем его в общий каталог
        return <Navigate to="/courses" replace />;
    }

    // Outlet — это специальный компонент React Router, куда будут рендериться вложенные страницы (например, список курсов)
    return <Outlet />;
};