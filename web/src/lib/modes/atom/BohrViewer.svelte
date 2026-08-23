<script lang="ts">
	import type { AtomModel, Element } from '$lib/data/loaders';

	let { atom, element }: { atom: AtomModel; element: Element } = $props();
	const size = 320;
	const cx = size / 2;
	const cy = size / 2;
</script>

<div class="flex flex-col items-center gap-3">
	<svg width={size} height={size} class="rounded-xl bg-[#101820]">
		<circle cx={cx} cy={cy} r="14" fill="#ff6b6b" />
		<text x={cx} y={cy + 4} text-anchor="middle" font-size="9" fill="#fff">핵</text>
		{#each atom.shellDistribution as count, i}
			{@const r = 36 + i * 28}
			<circle cx={cx} cy={cy} {r} fill="none" stroke="#4cc9f0" stroke-width="1" opacity="0.7" />
			{#each Array(count) as _, e}
				{@const ang = (Math.PI * 2 * e) / count}
				<circle cx={cx + Math.cos(ang) * r} cy={cy + Math.sin(ang) * r} r="4" fill="#80ffea" />
			{/each}
		{/each}
	</svg>
	<p class="text-sm text-[#adb5bd]">
		p⁺ {atom.defaultProtons} · n⁰ {atom.defaultNeutrons} · e⁻ {atom.defaultElectrons} · 원자가 {atom.valenceElectrons}
		· 배치 {atom.shellDistribution.join('-')}
	</p>
	<p class="text-xs text-[#868e96]">{element.electronConfiguration}</p>
</div>
