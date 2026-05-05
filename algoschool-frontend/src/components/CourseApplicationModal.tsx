import { useState } from 'react';
import { X, Send, AlertCircle } from 'lucide-react';
import { apiClient } from '../api/axios';

interface CourseApplicationModalProps {
    isOpen: boolean;
    onClose: () => void;
    courseId: number;
    courseTitle: string;
    // Функция, которая вызовется при успешной отправке (чтобы обновить кнопку на "Заявка отправлена")
    onSuccess: () => void;
}

export const CourseApplicationModal = ({
                                           isOpen,
                                           onClose,
                                           courseId,
                                           courseTitle,
                                           onSuccess
                                       }: CourseApplicationModalProps) => {

    const [message, setMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    // Если окно закрыто, вообще ничего не рендерим
    if (!isOpen) return null;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setError('');

        try {
            // Отправляем запрос на наш новый эндпоинт
            await apiClient.post(`/applications/courses/${courseId}/apply`, {
                message: message.trim()
            });

            // Если всё прошло супер:
            setMessage(''); // Очищаем форму
            onSuccess();    // Сообщаем родителю, что всё ок
            onClose();      // Закрываем модалку

        } catch (err: any) {
            // Ловим ошибки (например, если заявка уже была подана)
            setError(err.response?.data?.message || 'Не удалось отправить заявку. Попробуйте позже.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        // Backdrop (Затемненный фон)
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm transition-opacity">

            // Само модальное окно
            <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden animate-in fade-in zoom-in-95 duration-200">

                {/* Шапка модалки */}
                <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100">
                    <h2 className="text-xl font-bold text-slate-800">Заявка на курс</h2>
                    <button
                        onClick={onClose}
                        className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-colors"
                    >
                        <X size={20} />
                    </button>
                </div>

                {/* Тело модалки */}
                <div className="p-6">
                    <div className="mb-4">
                        <p className="text-sm text-slate-500 mb-1">Вы подаете заявку на закрытый курс:</p>
                        <p className="font-semibold text-slate-800">{courseTitle}</p>
                    </div>

                    {error && (
                        <div className="flex items-start gap-2 mb-4 p-3 bg-red-50 text-red-600 rounded-xl text-sm border border-red-100">
                            <AlertCircle size={18} className="shrink-0 mt-0.5" />
                            <span>{error}</span>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} id="application-form">
                        <label className="block text-sm font-medium text-slate-700 mb-2">
                            Сопроводительное письмо (почему вы хотите на этот курс?)
                        </label>
                        <textarea
                            required
                            rows={4}
                            value={message}
                            onChange={(e) => setMessage(e.target.value)}
                            placeholder="Здравствуйте! Я очень хочу изучить эту тему, потому что..."
                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all resize-none"
                        />
                    </form>
                </div>

                {/* Подвал с кнопками */}
                <div className="flex items-center justify-end gap-3 px-6 py-4 bg-slate-50 border-t border-slate-100">
                    <button
                        type="button"
                        onClick={onClose}
                        disabled={isLoading}
                        className="px-4 py-2 font-medium text-slate-600 hover:text-slate-800 hover:bg-slate-200 rounded-xl transition-colors"
                    >
                        Отмена
                    </button>
                    <button
                        type="submit"
                        form="application-form"
                        disabled={isLoading || !message.trim()}
                        className="flex items-center gap-2 px-5 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-medium rounded-xl transition-colors"
                    >
                        {isLoading ? 'Отправка...' : (
                            <>
                                <span>Отправить заявку</span>
                                <Send size={16} />
                            </>
                        )}
                    </button>
                </div>

            </div>
        </div>
    );
};