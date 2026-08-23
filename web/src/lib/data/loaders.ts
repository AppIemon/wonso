import type { AtomModel, Element, Molecule, QuizQuestion } from './types';

export type { AtomModel, Element, Molecule, QuizQuestion };

export async function loadElements(): Promise<Element[]> {
	const res = await fetch('/data/elements.json');
	return res.json();
}

export async function loadAtoms(): Promise<AtomModel[]> {
	const res = await fetch('/data/atoms.json');
	return res.json();
}

export async function loadMolecules(): Promise<Molecule[]> {
	const res = await fetch('/data/molecules.json');
	return res.json();
}

export async function loadQuiz(): Promise<QuizQuestion[]> {
	const res = await fetch('/data/quiz/all.json');
	return res.json();
}
