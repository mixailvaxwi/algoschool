import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/axios';
import { Book, PlayCircle } from 'lucide-react';

interface Lesson {
    id: number;
    title: string;
}

interface Module {
    id: number;
    title: string;
    lessons: Lesson[];
}

export const CoursePage = () => {
    const { courseId } = useParams<{ courseId: string }>();
    const navigate = useNavigate();
    const [modules, setModules] = useState<Module[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchCourseStructure = async () => {
            try {
                const response = await apiClient.get(`/courses/${courseId}/structure`);
                setModules(response.data);
            } catch (err) {
                setError('Не удалось загрузить структуру курса.');
            } finally {
                setIsLoading(false);
            }
        };

        fetchCourseStructure();
    }, [courseId]);

    if (isLoading) {
        return <div className="text-center py-20 text-slate-500">Загрузка структуры курса...</div>;
    }

    if (error) {
        return <div className="text-center py-20 text-red-500">{error}</div>;
    }

    return (
        <div className="max-w-4xl mx-auto">
            <h1 className="text-3xl font-bold text-slate-800 mb-8">Содержание курса</h1>
            <div className="space-y-6">
                {modules.map((module) => (
                    <div key={module.id} className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
                        <h2 className="text-xl font-bold text-slate-800 mb-4 flex items-center gap-3">
                            <Book className="text-blue-500" />
                            {module.title}
                        </h2>
                        <ul className="space-y-2">
                            {module.lessons.map((lesson) => (
                                <li
                                    key={lesson.id}
                                    onClick={() => navigate(`/courses/${courseId}/lessons/${lesson.id}`)}
                                    className="flex items-center gap-3 p-3 rounded-lg hover:bg-slate-100 cursor-pointer transition-colors"
                                >
                                    <PlayCircle className="text-emerald-500" />
                                    <span className="text-slate-700">{lesson.title}</span>
                                </li>
                            ))}
                        </ul>
                    </div>
                ))}
            </div>
        </div>
    );
};