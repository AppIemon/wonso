<script lang="ts">
	import PeriodicTable from '$lib/modes/element/PeriodicTable.svelte';
	import { loadElements, type Element } from '$lib/data/loaders';
	import { selectedZ } from '$lib/stores/elementContext';

	let elements = $state<Element[]>([]);
	let q = $state('');
	loadElements().then((e) => (elements = e));

	let selected = $derived(elements.find((e) => e.z === $selectedZ));
	let filtered = $derived(
		q
			? elements.filter((e) =>
					[e.symbol, e.nameKo, e.nameEn, ...(e.aliasesKo ?? [])].some((s) => s.toLowerCase().includes(q.toLowerCase()))
				)
			: elements
	);
</script>

<div class="space-y-4">
	<div class="flex flex-wrap items-end justify-between gap-3">
		<h1 class="text-2xl font-bold">원소 모드</h1>
		<input class="rounded bg-[#121a27] border border-[#1f2a3a] px-3 py-1 text-sm" placeholder="한글/영문/기호 검색" bind:value={q} />
	</div>
	<PeriodicTable {elements} />
	{#if q}
		<div class="flex flex-wrap gap-2 text-sm">
			{#each filtered.slice(0, 20) as e}
				<button class="rounded bg-[#121a27] px-2 py-1" onclick={() => selectedZ.set(e.z)}>{e.symbol} {e.nameKo}</button>
			{/each}
		</div>
	{/if}
	{#if selected}
		<article class="rounded-2xl border border-[#1f2a3a] bg-[#121a27] p-5">
			<h2 class="text-xl">{selected.symbol} {selected.nameKo} <span class="text-[#adb5bd]">Z={selected.z}</span></h2>
			<p class="mt-2 text-sm">{selected.descriptionKo}</p>
			<p class="mt-2 text-xs text-[#adb5bd]">주기 {selected.period} · 족 {selected.group ?? '—'} · {selected.category} · {selected.stateAtRoomTemp}</p>
			<div class="mt-3 flex gap-3 text-sm">
				<a class="text-[#e8c86a]" href={`/atom/${selected.z}`}>원자 모드로 보기</a>
				<a class="text-[#80ffea]" href={`/molecule?z=${selected.z}`}>관련 분자 보기</a>
			</div>
		</article>
	{/if}
</div>
