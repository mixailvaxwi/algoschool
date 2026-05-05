import { create } from 'zustand';

interface LessonState {
    completedSteps: Set<number>;
    addCompletedStep: (stepId: number) => void;
    setInitialCompletedSteps: (stepIds: Set<number>) => void;
    resetLessonProgress: () => void;
}

export const useLessonStore = create<LessonState>((set) => ({
    completedSteps: new Set(),

    addCompletedStep: (stepId) => set((state) => {
        if (state.completedSteps.has(stepId)) {
            return state;
        }
        const newCompletedSteps = new Set(state.completedSteps);
        newCompletedSteps.add(stepId);
        return {
            completedSteps: newCompletedSteps,
        };
    }),

    setInitialCompletedSteps: (stepIds) => set({ completedSteps: new Set(stepIds) }),

    resetLessonProgress: () => set({ completedSteps: new Set() }),
}));