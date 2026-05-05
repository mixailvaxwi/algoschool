import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/authStore';
import { Navbar } from './components/Navbar';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { CatalogPage } from './pages/CatalogPage';
import { CoursePage } from './pages/CoursePage';
import { LessonPage } from './pages/LessonPage';
import { TeacherRoute } from './components/TeacherRoute';
import { TeacherLayout } from './layouts/TeacherLayout';
import { TeacherCoursesPage } from './pages/teacher/TeacherCoursesPage';
import { TeacherCourseEditorPage } from './pages/teacher/TeacherCourseEditorPage';
import { TeacherLessonEditorPage } from './pages/teacher/TeacherLessonEditorPage';
import { LessonPlayerPage } from './pages/student/LessonPlayerPage';
import { CourseApplicationsPage } from './pages/teacher/CourseApplicationsPage';
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
                        <Route path="courses/:courseId/applications" element={<CourseApplicationsPage />} />
                        <Route path="users" element={<div>Страница пользователей (в разработке)</div>} />
                        <Route path="lessons/:lessonId" element={<TeacherLessonEditorPage />} />
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
                                    <Route path="/courses/:courseId" element={<CoursePage />} />
                                    <Route path="/courses/:courseId/lessons/:lessonId" element={<LessonPlayerPage />} />
                                    <Route path="/lessons/:lessonId" element={<ProtectedRoute><LessonPage /></ProtectedRoute>} />
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