<script lang="ts">
	import { page } from '$app/state';
	import BohrViewer from '$lib/modes/atom/BohrViewer.svelte';
	import { loadAtoms, loadElements, type AtomModel, type Element } from '$lib/data/loaders';
	import { selectedZ } from '$lib/stores/elementContext';
	import { explore } from '$lib/stores/progress';

	let element = $state<Element | null>(null);
	let atom = $state<AtomModel | null>(null);

	$effect(() => {
		const z = Number(page.params.z);
		selectedZ.set(z);
		Promise.all([loadElements(), loadAtoms()]).then(([els, ats]) => {
			element = els.find((e) => e.z === z) ?? null;
			atom = ats.find((a) => a.z === z) ?? null;
			if (element) explore(element.z);
		});
	});
</script>

{#if element && atom}
	<div class="space-y-4">
		<div class="flex justify-between">
			<h1 class="text-2xl font-bold">원자 모드 · {element.symbol}</h1>
			<div class="flex gap-2 text-sm">
				<a href={`/atom/${Math.max(1, element.z - 1)}`}>이전</a>
				<a href={`/atom/${Math.min(118, element.z + 1)}`}>다음</a>
			</div>
		</div>
		<BohrViewer {atom} {element} />
		<p class="text-sm text-[#adb5bd]">{element.descriptionKo}</p>
		<div class="text-sm flex gap-3">
			<a class="text-[#e8c86a]" href="/element">원소 표</a>
			<a class="text-[#80ffea]" href={`/molecule?z=${element.z}`}>관련 분자</a>
		</div>
	</div>
{/if}
