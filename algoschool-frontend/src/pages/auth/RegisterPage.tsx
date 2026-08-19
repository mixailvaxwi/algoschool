import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { apiClient } from '../../api/axios';
import { UserPlus, GraduationCap } from 'lucide-react';

export const RegisterPage = () => {
    const navigate = useNavigate();

    // Добавили username в данные для отправки
    const [formData, setFormData] = useState({
        name: '',
        username: '',
        email: '',
        password: '',
    });

    // Отдельное состояние для повторного ввода пароля (не отправляется на сервер)
    const [confirmPassword, setConfirmPassword] = useState('');

    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        // Валидация совпадения паролей до отправки запроса
        if (formData.password !== confirmPassword) {
            setError('Пароли не совпадают. Пожалуйста, проверьте правильность ввода.');
            return;
        }

        setIsLoading(true);
        setError('');

        try {
            await apiClient.post('/auth/register', formData);
            alert('Регистрация прошла успешно! Теперь вы можете войти.');
            navigate('/login');
        } catch (err: any) {
            setError(err.response?.data?.message || 'Ошибка регистрации. Возможно, email или логин уже заняты.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-slate-50 p-4 py-12">
            <div className="max-w-md w-full bg-white rounded-2xl shadow-sm border border-slate-100 p-8">
                <div className="flex flex-col items-center mb-8">
                    <div className="bg-blue-100 p-3 rounded-full mb-4 text-blue-600">
                        <UserPlus size={32} />
                    </div>
                    <h1 className="text-2xl font-bold text-slate-800">Создать аккаунт</h1>
                    <p className="text-slate-500 mt-2">Присоединяйтесь к AlgoSchool</p>
                </div>

                {error && <div className="mb-6 p-4 bg-red-50 text-red-600 rounded-xl text-sm border border-red-100">{error}</div>}

                <form onSubmit={handleSubmit} className="space-y-5">

                    {/*
                      Выбор роли убран: сервер больше не принимает role от клиента,
                      иначе любой желающий регистрировался преподавателем или админом.
                      Все новые аккаунты создаются студенческими; права преподавателя
                      выдаёт администратор.
                    */}
                    <div className="flex items-start gap-3 mb-6 p-4 rounded-xl bg-slate-50 border border-slate-200 text-slate-600">
                        <GraduationCap size={20} className="shrink-0 mt-0.5 text-blue-600" />
                        <p className="text-sm m-0">
                            Аккаунт создаётся со статусом студента. Права преподавателя выдаёт администратор.
                        </p>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Ваше имя</label>
                        <input
                            type="text"
                            required
                            value={formData.name}
                            onChange={(e) => setFormData({...formData, name: e.target.value})}
                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all"
                            placeholder="Иван Иванов"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Логин (Уникальный никнейм)</label>
                        <input
                            type="text"
                            required
                            value={formData.username}
                            onChange={(e) => setFormData({...formData, username: e.target.value})}
                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all"
                            placeholder="ivan_dev"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Email</label>
                        <input
                            type="email"
                            required
                            value={formData.email}
                            onChange={(e) => setFormData({...formData, email: e.target.value})}
                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all"
                            placeholder="student@example.com"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Пароль</label>
                        <input
                            type="password"
                            required
                            value={formData.password}
                            onChange={(e) => setFormData({...formData, password: e.target.value})}
                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all"
                            placeholder="••••••••"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Повторите пароль</label>
                        <input
                            type="password"
                            required
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all"
                            placeholder="••••••••"
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={isLoading}
                        className="w-full py-3 px-4 bg-slate-800 hover:bg-slate-900 text-white font-medium rounded-xl transition-colors disabled:opacity-70 mt-6"
                    >
                        {isLoading ? 'Регистрация...' : 'Зарегистрироваться'}
                    </button>
                </form>

                <div className="mt-8 text-center text-sm text-slate-600">
                    Уже есть аккаунт? <Link to="/login" className="text-blue-600 hover:underline font-medium">Войти</Link>
                </div>
            </div>
        </div>
    );
};