<script lang="ts">
	import { page } from '$app/state';
	import FormulaViewer from '$lib/modes/molecule/FormulaViewer.svelte';
	import { loadMolecules, type Molecule } from '$lib/data/loaders';

	let mol = $state<Molecule | null>(null);
	$effect(() => {
		loadMolecules().then((all) => (mol = all.find((m) => m.id === page.params.id) ?? null));
	});
</script>

{#if mol}
	<div class="space-y-4">
		<a class="text-sm text-[#adb5bd]" href="/molecule">← 라이브러리</a>
		<FormulaViewer molecule={mol} />
		<div class="flex flex-wrap gap-2 text-sm">
			{#each mol.relatedElements as z}
				<a class="rounded bg-[#121a27] px-2 py-1" href={`/element/${z}`}>Z={z}</a>
				<a class="rounded bg-[#121a27] px-2 py-1" href={`/atom/${z}`}>원자 {z}</a>
			{/each}
		</div>
	</div>
{/if}
