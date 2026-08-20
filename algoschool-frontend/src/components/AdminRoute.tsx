import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

export const AdminRoute = () => {
    const { isAuthenticated, user } = useAuthStore();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (user?.role !== 'ROLE_ADMIN') {
        return <Navigate to="/courses" replace />;
    }

    return <Outlet />;
};
