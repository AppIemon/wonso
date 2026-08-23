<script lang="ts">
	import type { Element } from '$lib/data/loaders';
	import { selectedZ } from '$lib/stores/elementContext';
	import { explore } from '$lib/stores/progress';

	let { elements }: { elements: Element[] } = $props();

	const colors: Record<string, string> = {
		alkali_metal: 'bg-orange-700',
		alkaline_earth_metal: 'bg-yellow-700',
		transition_metal: 'bg-sky-800',
		post_transition_metal: 'bg-slate-600',
		metalloid: 'bg-teal-700',
		reactive_nonmetal: 'bg-emerald-700',
		halogen: 'bg-purple-700',
		noble_gas: 'bg-cyan-700',
		lanthanide: 'bg-rose-700',
		actinide: 'bg-red-900',
		unknown: 'bg-zinc-700'
	};

	function click(el: Element) {
		selectedZ.set(el.z);
		explore(el.z);
	}
</script>

<div class="overflow-auto">
	<div class="grid gap-0.5 min-w-[720px]" style="grid-template-columns: repeat(18, minmax(0,1fr));">
		{#each Array(9 * 18) as _, i}
			{@const row = Math.floor(i / 18) + 1}
			{@const col = (i % 18) + 1}
			{@const el = elements.find((e) => e.displayRow === row && e.displayColumn === col)}
			{#if el}
				<button
					class={`h-10 text-[10px] leading-tight rounded-sm ${colors[el.category] ?? 'bg-zinc-700'} hover:ring-2 ring-amber-300`}
					onclick={() => click(el)}
				>
					<div class="opacity-70">{el.z}</div>
					<div class="font-bold">{el.symbol}</div>
				</button>
			{:else}
				<div class="h-10"></div>
			{/if}
		{/each}
	</div>
</div>
