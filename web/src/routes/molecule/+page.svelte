<script lang="ts">
	import { page } from '$app/state';
	import FormulaViewer from '$lib/modes/molecule/FormulaViewer.svelte';
	import { loadMolecules, type Molecule } from '$lib/data/loaders';

	let molecules = $state<Molecule[]>([]);
	loadMolecules().then((m) => (molecules = m));
	let z = $derived(Number(page.url.searchParams.get('z') || 0));
	let shown = $derived(z ? molecules.filter((m) => m.relatedElements.includes(z)) : molecules);
</script>

<div class="space-y-4">
	<h1 class="text-2xl font-bold">분자 모드 {z ? `· Z=${z}` : ''}</h1>
	<p class="text-sm text-[#adb5bd]">아래첨자는 원자 개수, 앞의 숫자는 분자 계수입니다.</p>
	<div class="grid gap-3 md:grid-cols-2">
		{#each shown as m}
			<a href={`/molecule/${m.id}`}><FormulaViewer molecule={m} /></a>
		{/each}
	</div>
</div>
