import React, { useState, useRef } from 'react';
import type { CodeProblemStep } from '../playerTypes';
import { Code, Zap, Database, Upload, FileCode, Send, X, Terminal, CheckCircle, XCircle } from 'lucide-react';
import Editor from '@monaco-editor/react';
import type { AssessmentResult } from '../StepRenderer';

interface Props {
    step: CodeProblemStep;
    onSubmit: (payload: string) => Promise<AssessmentResult | null>;
    isLoading: boolean;
}

export const CodeStepDisplay: React.FC<Props> = ({ step, onSubmit, isLoading }) => {
    const [submitMethod, setSubmitMethod] = useState<'editor' | 'file'>('editor');
    // Исправили опечатку в стартовом коде (String[] args)
    const [code, setCode] = useState('public class Main {\n  public static void main(String[] args) {\n    // Ваш код здесь\n  }\n}');
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [result, setResult] = useState<AssessmentResult | null>(null);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            setSelectedFile(e.target.files[0]);
        }
    };

    const clearFile = () => {
        setSelectedFile(null);
        if (fileInputRef.current) fileInputRef.current.value = '';
    };

    const handleSubmit = async () => {
        let finalCodeToSubmit = '';
        if (submitMethod === 'editor') {
            finalCodeToSubmit = code;
        } else if (submitMethod === 'file' && selectedFile) {
            finalCodeToSubmit = await selectedFile.text();
        } else {
            return;
        }

        setResult(null);
        const res = await onSubmit(finalCodeToSubmit);
        if (res) setResult(res);
    };

    const isAccepted = result?.status === 'CORRECT';

    return (
        <div className="space-y-6 flex flex-col h-full">
            <div className="flex items-center gap-3 text-amber-600 mb-2 pb-4 border-b border-slate-100 shrink-0">
                <Code size={28} />
                <h1 className="m-0 text-3xl font-bold text-slate-900">Задача на программирование</h1>
            </div>

            <div className="shrink-0 space-y-4">
                <div className="text-slate-700 whitespace-pre-wrap leading-relaxed text-lg">
                    {step.description}
                </div>
                <div className="flex flex-wrap gap-4 bg-slate-50 p-4 rounded-xl border border-slate-200 text-sm">
                    <div className="flex items-center gap-2 text-slate-700 font-medium bg-white px-3 py-1.5 rounded-lg border border-slate-200 shadow-sm">
                        <Zap size={16} className="text-amber-500" /> Время: {step.timeLimitSec} сек
                    </div>
                    <div className="flex items-center gap-2 text-slate-700 font-medium bg-white px-3 py-1.5 rounded-lg border border-slate-200 shadow-sm">
                        <Database size={16} className="text-blue-500" /> Память: {step.memoryLimitMb} МБ
                    </div>
                    <div className="flex items-center gap-2 text-slate-700 font-medium bg-white px-3 py-1.5 rounded-lg border border-slate-200 shadow-sm">
                        <Terminal size={16} className="text-emerald-500" /> Языки: {step.allowedLanguages}
                    </div>
                </div>
            </div>

            {result && (
                <div className={`mt-2 p-4 rounded-xl flex items-start gap-3 border ${
                    isAccepted ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-red-50 border-red-200 text-red-800'
                }`}>
                    {isAccepted ? <CheckCircle className="shrink-0 mt-0.5" /> : <XCircle className="shrink-0 mt-0.5" />}
                    <div className="w-full">
                        <p className="font-bold text-lg">{isAccepted ? 'Решение принято!' : 'Ошибка выполнения'}</p>
                        <pre className="mt-2 p-3 bg-white/60 rounded border border-black/10 text-sm whitespace-pre-wrap font-mono">
                            {result.message}
                        </pre>
                    </div>
                </div>
            )}

            <div className="flex-1 flex flex-col border border-slate-200 rounded-xl overflow-hidden bg-white shadow-sm mt-2 min-h-[400px]">
                <div className="flex bg-slate-100 border-b border-slate-200 p-1 shrink-0">
                    <button
                        onClick={() => setSubmitMethod('editor')}
                        className={`flex-1 flex items-center justify-center gap-2 py-2.5 text-sm font-medium rounded-lg transition-colors ${
                            submitMethod === 'editor' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'
                        }`}
                    >
                        <Code size={18} /> Писать код
                    </button>
                    <button
                        onClick={() => setSubmitMethod('file')}
                        className={`flex-1 flex items-center justify-center gap-2 py-2.5 text-sm font-medium rounded-lg transition-colors ${
                            submitMethod === 'file' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'
                        }`}
                    >
                        <Upload size={18} /> Загрузить файл
                    </button>
                </div>

                <div className="flex-1 bg-slate-900 relative">
                    {submitMethod === 'editor' ? (
                        <div className="absolute inset-0">
                            <Editor
                                height="100%"
                                defaultLanguage="java"
                                theme="vs-dark"
                                value={code}
                                onChange={(value) => !isAccepted && setCode(value || '')}
                                options={{
                                    readOnly: isAccepted,
                                    minimap: { enabled: false },
                                    fontSize: 15,
                                    padding: { top: 16 }
                                }}
                            />
                        </div>
                    ) : (
                        <div className="absolute inset-0 flex items-center justify-center bg-white p-8">
                            <div className="w-full max-w-md">
                                {!selectedFile ? (
                                    <label className={`flex flex-col items-center justify-center w-full h-64 border-2 border-slate-300 border-dashed rounded-xl transition-all ${
                                        isAccepted ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer bg-slate-50 hover:bg-slate-100 hover:border-blue-400'
                                    }`}>
                                        <div className="flex flex-col items-center justify-center pt-5 pb-6">
                                            <Upload className="w-12 h-12 text-slate-400 mb-4" />
                                            <p className="mb-2 text-sm text-slate-700 font-medium">Нажмите для загрузки или перетащите файл</p>
                                        </div>
                                        <input
                                            ref={fileInputRef}
                                            type="file"
                                            className="hidden"
                                            accept=".java,.py,.cpp,.txt,.c,.cs"
                                            onChange={handleFileChange}
                                            disabled={isAccepted}
                                        />
                                    </label>
                                ) : (
                                    <div className="bg-blue-50 border border-blue-200 rounded-xl p-6 flex flex-col items-center text-center">
                                        <FileCode className="w-16 h-16 text-blue-500 mb-4" />
                                        <h3 className="text-lg font-bold text-slate-800 break-all">{selectedFile.name}</h3>
                                        {!isAccepted && (
                                            <button onClick={clearFile} className="mt-4 text-red-500 hover:text-red-700 text-sm font-medium flex items-center gap-1">
                                                <X size={16} /> Выбрать другой файл
                                            </button>
                                        )}
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                </div>
            </div>

            <div className="pt-4 shrink-0 flex justify-end">
                <button
                    onClick={handleSubmit}
                    disabled={isLoading || isAccepted || (submitMethod === 'file' && !selectedFile)}
                    className="px-8 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl transition-colors disabled:opacity-50 flex items-center gap-2"
                >
                    {isLoading ? 'Проверяем код...' : <><Send size={20} /> Отправить на проверку</>}
                </button>
            </div>
        </div>
    );
};