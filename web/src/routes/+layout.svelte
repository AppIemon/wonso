<script lang="ts">
	import '../app.css';
	import { page } from '$app/state';
	import { progress } from '$lib/stores/progress';

	let { children } = $props();
	const tabs = [
		{ href: '/', label: '홈' },
		{ href: '/element', label: '원소' },
		{ href: '/atom/1', label: '원자' },
		{ href: '/molecule', label: '분자' },
		{ href: '/quiz/element', label: '퀴즈' }
	];
</script>

<div class="min-h-screen">
	<header class="sticky top-0 z-10 border-b border-[#1f2a3a] bg-[#0b1320]/95 backdrop-blur">
		<div class="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
			<a href="/" class="font-bold tracking-wide text-[#e8c86a]">wonso</a>
			<nav class="flex gap-3 text-sm">
				{#each tabs as t}
					<a class={page.url.pathname.startsWith(t.href) && t.href !== '/' || page.url.pathname === t.href ? 'text-[#e8c86a]' : 'text-[#adb5bd]'} href={t.href}>{t.label}</a>
				{/each}
			</nav>
			<div class="text-xs text-[#adb5bd]">XP {$progress.xp} · 탐색 {$progress.seen.length}/118</div>
		</div>
	</header>
	<main class="mx-auto max-w-6xl px-4 py-6">
		{@render children()}
	</main>
</div>
