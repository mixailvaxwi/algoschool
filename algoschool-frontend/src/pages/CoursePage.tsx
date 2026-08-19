import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/axios';
import { Book, PlayCircle, Lock, Info } from 'lucide-react';
import { CourseApplicationModal } from '../components/CourseApplicationModal';

interface Lesson {
    id: number;
    title: string;
}

interface Module {
    id: number;
    title: string;
    lessons: Lesson[];
}

// Новый интерфейс для данных о курсе
interface CourseInfo {
    id: number;
    title: string;
    description: string;
    accessType: 'OPEN' | 'CLOSED';
    isEnrolled: boolean;
    applicationStatus: 'NONE' | 'PENDING' | 'APPROVED' | 'REJECTED';
}

export const CoursePage = () => {
    const { courseId } = useParams<{ courseId: string }>();
    const navigate = useNavigate();

    // Состояния данных
    const [course, setCourse] = useState<CourseInfo | null>(null);
    const [modules, setModules] = useState<Module[]>([]);

    // Состояния UI
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isEnrolling, setIsEnrolling] = useState(false);

    const fetchCourseData = async () => {
        try {
            // Запрашиваем параллельно и информацию о курсе, и его структуру
            const [infoResponse, structureResponse] = await Promise.all([
                apiClient.get(`/courses/${courseId}`),
                apiClient.get(`/courses/${courseId}/structure`)
            ]);

            setCourse(infoResponse.data);
            setModules(structureResponse.data);
        } catch (err) {
            setError('Не удалось загрузить данные курса.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        if (courseId) {
            fetchCourseData().then();
        }
    }, [courseId]);

    // Функция для записи на ОТКРЫТЫЙ курс (без заявок)
    const handleEnrollOpenCourse = async () => {
        setIsEnrolling(true);
        try {
            // ИСПРАВЛЕНО: Добавлен слеш в начале пути
            await apiClient.post(`/courses/${courseId}/enroll`);
            // Если успешно - обновляем локальный стейт, чтобы открыть уроки
            setCourse(prev => prev ? { ...prev, isEnrolled: true } : null);
        } catch (error) {
            alert('Ошибка при записи на курс.');
        } finally {
            setIsEnrolling(false);
        }
    };

    if (isLoading) {
        return <div className="text-center py-20 text-slate-500">Загрузка курса...</div>;
    }

    if (error || !course) {
        return <div className="text-center py-20 text-red-500">{error || 'Курс не найден'}</div>;
    }

    const hasPendingApplication = course.applicationStatus === 'PENDING';

    return (
        <div className="max-w-4xl mx-auto p-4">

            {/* ШАПКА КУРСА */}
            <div className="mb-10">
                <h1 className="text-3xl font-bold text-slate-800 mb-4">{course.title}</h1>
                <p className="text-slate-600 text-lg mb-6">{course.description || 'Описание курса отсутствует.'}</p>

                {/* БЛОК ДОСТУПА (Показываем только если студент НЕ зачислен) */}
                {!course.isEnrolled && (
                    <div className="bg-slate-50 p-6 rounded-2xl border border-slate-200 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                        <div className="flex items-start gap-3">
                            <Info className="text-blue-500 shrink-0 mt-1" />
                            <div>
                                <h3 className="text-lg font-bold text-slate-800">
                                    {course.accessType === 'CLOSED' ? 'Курс по заявкам' : 'Открытый курс'}
                                </h3>
                                <p className="text-slate-600 text-sm mt-1">
                                    {course.accessType === 'CLOSED'
                                        ? 'Чтобы просматривать уроки, необходимо отправить мотивационное письмо преподавателю.'
                                        : 'Вы можете начать обучение прямо сейчас абсолютно бесплатно.'}
                                </p>
                            </div>
                        </div>

                        {/* КНОПКА ДЕЙСТВИЯ */}
                        {course.accessType === 'CLOSED' ? (
                            <button
                                onClick={() => setIsModalOpen(true)}
                                disabled={hasPendingApplication}
                                className="w-full sm:w-auto px-6 py-3 bg-slate-800 text-white font-medium rounded-xl disabled:bg-slate-300 disabled:text-slate-500 transition-colors whitespace-nowrap"
                            >
                                {hasPendingApplication ? 'Заявка на рассмотрении' : 'Подать заявку'}
                            </button>
                        ) : (
                            <button
                                onClick={handleEnrollOpenCourse}
                                disabled={isEnrolling}
                                className="w-full sm:w-auto px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl transition-colors whitespace-nowrap"
                            >
                                {isEnrolling ? 'Запись...' : 'Начать учиться'}
                            </button>
                        )}
                    </div>
                )}
            </div>

            {/* ПРОГРАММА КУРСА */}
            <h2 className="text-2xl font-bold text-slate-800 mb-6">Программа</h2>
            <div className="space-y-6">
                {modules.map((module) => (
                    <div key={module.id} className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
                        <h3 className="text-xl font-bold text-slate-800 mb-4 flex items-center gap-3">
                            <Book className="text-blue-500" />
                            {module.title}
                        </h3>
                        <ul className="space-y-2">
                            {module.lessons.map((lesson) => {
                                // Если курс открыт для юзера - он может кликать, иначе - нет
                                const canAccess = course.isEnrolled;

                                return (
                                    <li
                                        key={lesson.id}
                                        onClick={() => canAccess && navigate(`/courses/${courseId}/lessons/${lesson.id}`)}
                                        className={`flex items-center gap-3 p-3 rounded-lg transition-all ${
                                            canAccess
                                                ? 'hover:bg-slate-100 cursor-pointer'
                                                : 'opacity-60 cursor-not-allowed bg-slate-50'
                                        }`}
                                    >
                                        {canAccess ? (
                                            <PlayCircle className="text-emerald-500 shrink-0" />
                                        ) : (
                                            <Lock className="text-slate-400 shrink-0" size={20} />
                                        )}
                                        <span className="text-slate-700 font-medium">{lesson.title}</span>
                                    </li>
                                );
                            })}
                        </ul>
                    </div>
                ))}
            </div>

            {/* НАШЕ МОДАЛЬНОЕ ОКНО */}
            <CourseApplicationModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                courseld={Number(courseId)}
                courseTitle={course.title}
                onSuccess={() => {
                    // Обновляем статус, чтобы кнопка стала серой
                    setCourse(prev => prev ? { ...prev, applicationStatus: 'PENDING' } : null);
                }}
            />
        </div>
    );
};