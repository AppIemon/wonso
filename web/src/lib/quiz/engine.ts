import type { QuizQuestion } from '$lib/data/loaders';

export function pickSet(all: QuizQuestion[], mode: string, n = 5): QuizQuestion[] {
	const pool = all.filter((q) => mode === 'integrated' || q.mode === mode);
	return [...pool].sort(() => Math.random() - 0.5).slice(0, n);
}

export function check(q: QuizQuestion, answer: string | number): boolean {
	return String(q.correctAnswer) === String(answer);
}
