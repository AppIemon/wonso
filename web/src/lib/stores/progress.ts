import { writable } from 'svelte/store';

const KEY = 'wonso-progress';

function load() {
	if (typeof localStorage === 'undefined') return { xp: 0, seen: [] as number[], quiz: 0 };
	try {
		return JSON.parse(localStorage.getItem(KEY) || '{"xp":0,"seen":[],"quiz":0}');
	} catch {
		return { xp: 0, seen: [] as number[], quiz: 0 };
	}
}

export const progress = writable(load());

progress.subscribe((v) => {
	if (typeof localStorage !== 'undefined') localStorage.setItem(KEY, JSON.stringify(v));
});

export function explore(z: number) {
	progress.update((p) => {
		if (!p.seen.includes(z)) {
			p.seen = [...p.seen, z];
			p.xp += 5;
		}
		return { ...p };
	});
}

export function quizCorrect() {
	progress.update((p) => ({ ...p, xp: p.xp + 10, quiz: p.quiz + 1 }));
}
