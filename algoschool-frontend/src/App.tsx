import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/authStore';
import { Navbar } from './components/Navbar';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { CatalogPage } from './pages/CatalogPage';
import { CoursePage } from './pages/CoursePage';
import { TeacherRoute } from './components/TeacherRoute';
import { AdminRoute } from './components/AdminRoute';
import { TeacherLayout } from './layouts/TeacherLayout';
import { AdminLayout } from './layouts/AdminLayout';
import { AdminUsersPage } from './pages/admin/AdminUsersPage';
import { TeacherCoursesPage } from './pages/teacher/TeacherCoursesPage';
import { TeacherCourseEditorPage } from './pages/teacher/TeacherCourseEditorPage';
import { LessonEditorPage } from './pages/teacher/LessonEditorPage';
import { LessonPlayerPage } from './pages/student/LessonPlayerPage';
import { TeacherApplicationsPage } from './pages/teacher/TeacherApplicationsPage';
import { TeacherSubmissionsPage } from './pages/teacher/TeacherSubmissionsPage';
import { ProfilePage } from './pages/ProfilePage';
import { PublicProfilePage } from './pages/PublicProfilePage';
import { MyApplicationsPage } from './pages/MyApplicationsPage';
import { MyCoursesPage } from './pages/MyCoursesPage';
import type {JSX} from "react";

const ProtectedRoute = ({ children }: { children: JSX.Element }) => {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
    return isAuthenticated ? children : <Navigate to="/login" replace />;
};

function App() {
    return (
        <BrowserRouter>
            <Navbar />
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />

                <Route path="/teacher" element={<TeacherRoute />}>
                    <Route element={<TeacherLayout />}>
                        <Route index element={<Navigate to="courses" replace />} />
                        <Route path="courses" element={<TeacherCoursesPage />} />
                        <Route path="courses/:courseId" element={<TeacherCourseEditorPage />} />
                        <Route path="courses/:courseId/lessons/:lessonId/edit" element={<LessonEditorPage />} />
                        <Route path="courses/:courseId/applications" element={<TeacherApplicationsPage />} />
                        <Route path="submissions" element={<TeacherSubmissionsPage />} />
                        <Route path="users" element={<div>Страница пользователей (в разработке)</div>} />
                    </Route>
                </Route>

                <Route path="/admin" element={<AdminRoute />}>
                    <Route element={<AdminLayout />}>
                        <Route index element={<Navigate to="users" replace />} />
                        <Route path="users" element={<AdminUsersPage />} />
                    </Route>
                </Route>

                <Route
                    path="/*"
                    element={
                        <div className="min-h-screen bg-slate-50">
                            <main className="container mx-auto px-4 py-8">
                                <Routes>
                                    <Route path="/" element={<div className="text-2xl font-bold">Главная страница</div>} />
                                    <Route path="/courses" element={<CatalogPage />} />
                                    <Route path="/courses/enrolled" element={<ProtectedRoute><MyCoursesPage /></ProtectedRoute>} />
                                    <Route path="/courses/:courseId" element={<CoursePage />} />
                                    <Route path="/courses/:courseId/lessons/:lessonId" element={<LessonPlayerPage />} />
                                    <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
                                    <Route path="/users/:username" element={<ProtectedRoute><PublicProfilePage /></ProtectedRoute>} />
                                    <Route path="/my-applications" element={<ProtectedRoute><MyApplicationsPage /></ProtectedRoute>} />
                                </Routes>
                            </main>
                        </div>
                    }
                />
            </Routes>
        </BrowserRouter>
    );
}

export default App;