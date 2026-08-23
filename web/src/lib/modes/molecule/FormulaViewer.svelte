<script lang="ts">
	import type { Molecule } from '$lib/data/loaders';

	let { molecule }: { molecule: Molecule } = $props();
</script>

<div class="rounded-xl border border-[#1f2a3a] bg-[#121a27] p-4 space-y-2">
	<h2 class="text-xl font-semibold">{molecule.formulaDisplay} <span class="text-base text-[#adb5bd]">{molecule.nameKo}</span></h2>
	<p class="text-sm text-[#e8c86a]">에너지 ⚡{molecule.energy} · {molecule.category} · 난이도 {molecule.difficulty}</p>
	<div class="flex flex-wrap gap-2">
		{#each molecule.elements as part}
			<span class="px-2 py-1 rounded bg-[#1b2636] text-sm">{part.symbol} × {part.count}</span>
		{/each}
	</div>
	<p class="text-sm leading-relaxed">{molecule.descriptionKo}</p>
</div>
