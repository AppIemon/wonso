<script lang="ts">
	import { page } from '$app/state';
	import { goto } from '$app/navigation';
	import { selectedZ } from '$lib/stores/elementContext';
	import { loadElements, type Element } from '$lib/data/loaders';
	import { explore } from '$lib/stores/progress';

	let el = $state<Element | null>(null);
	$effect(() => {
		const z = Number(page.params.z);
		selectedZ.set(z);
		loadElements().then((all) => {
			el = all.find((e) => e.z === z) ?? null;
			if (el) explore(el.z);
		});
	});
</script>

{#if el}
	<article class="space-y-3">
		<button class="text-sm text-[#adb5bd]" onclick={() => goto('/element')}>← 주기율표</button>
		<h1 class="text-3xl font-bold">{el.symbol} {el.nameKo}</h1>
		<p>{el.descriptionKo}</p>
		<ul class="text-sm text-[#adb5bd] space-y-1">
			<li>영문명 {el.nameEn}</li>
			<li>원자량 {el.atomicMass ?? '합성/불확정'}</li>
			<li>전자 배치 {el.electronConfiguration}</li>
			<li>전기음성도 {el.electronegativity ?? '—'}</li>
		</ul>
		<div class="flex gap-3">
			<a class="text-[#e8c86a]" href={`/atom/${el.z}`}>원자로 보기</a>
			<a class="text-[#80ffea]" href={`/molecule?z=${el.z}`}>분자로 보기</a>
		</div>
	</article>
{/if}
