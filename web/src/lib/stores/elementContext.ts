import { writable } from 'svelte/store';

export const selectedZ = writable(1);
export const currentMode = writable<'element' | 'atom' | 'molecule' | 'quiz'>('element');
