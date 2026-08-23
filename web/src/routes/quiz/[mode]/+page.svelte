<script lang="ts">
	import { page } from '$app/state';
	import { loadQuiz, type QuizQuestion } from '$lib/data/loaders';
	import { check, pickSet } from '$lib/quiz/engine';
	import { quizCorrect } from '$lib/stores/progress';

	let questions = $state<QuizQuestion[]>([]);
	let i = $state(0);
	let answer = $state('');
	let result = $state<'idle' | 'ok' | 'bad'>('idle');

	$effect(() => {
		const mode = page.params.mode ?? 'element';
		loadQuiz().then((all) => {
			questions = pickSet(all, mode, 5);
			i = 0;
			answer = '';
			result = 'idle';
		});
	});

	let q = $derived(questions[i]);

	function submit() {
		if (!q) return;
		if (check(q, answer)) {
			result = 'ok';
			quizCorrect();
		} else result = 'bad';
	}

	function next() {
		i += 1;
		answer = '';
		result = 'idle';
	}
</script>

<div class="space-y-4 max-w-xl">
	<h1 class="text-2xl font-bold">퀴즈 · {page.params.mode}</h1>
	<div class="flex gap-2 text-sm">
		<a href="/quiz/element">원소</a>
		<a href="/quiz/atom">원자</a>
		<a href="/quiz/molecule">분자</a>
		<a href="/quiz/integrated">통합</a>
	</div>
	{#if !q}
		<p>세트가 끝났습니다.</p>
	{:else}
		<p class="text-[#adb5bd]">{i + 1} / {questions.length}</p>
		<h2 class="text-lg">{q.promptKo}</h2>
		{#if q.choices}
			<div class="grid gap-2">
				{#each q.choices as c}
					<button class="rounded border border-[#1f2a3a] bg-[#121a27] px-3 py-2 text-left" onclick={() => (answer = c)}>{c}</button>
				{/each}
			</div>
		{:else}
			<input class="w-full rounded bg-[#121a27] border border-[#1f2a3a] px-3 py-2" bind:value={answer} />
		{/if}
		<div class="flex gap-2">
			<button class="rounded bg-[#e8c86a] px-3 py-1 text-black" onclick={submit}>확인</button>
			{#if result !== 'idle'}
				<button class="rounded border border-[#1f2a3a] px-3 py-1" onclick={next}>다음</button>
			{/if}
		</div>
		{#if result === 'ok'}<p class="text-emerald-400">정답</p>{/if}
		{#if result === 'bad'}<p class="text-rose-400">오답. 정답: {q.correctAnswer}</p>{/if}
		{#if result !== 'idle'}<p class="text-sm text-[#adb5bd]">{q.explanationKo}</p>{/if}
	{/if}
</div>
