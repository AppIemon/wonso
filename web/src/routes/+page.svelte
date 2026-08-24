<script lang="ts">
	const modes = [
		{ href: '/element', title: '원소 모드', desc: '주기율표 118칸. 위치·족·성질을 지도처럼 본다.' },
		{ href: '/atom/1', title: '원자 모드', desc: 'Bohr 쉘 모델. 양성자·중성자·전자를 분해해서 본다.' },
		{ href: '/molecule', title: '분자 모드', desc: 'Tier A 50분자. 계수와 아래첨자를 구분한다.' },
		{ href: '/quiz/integrated', title: '퀴즈', desc: '원소·원자·분자 구분 퀴즈.' }
	];
</script>

<section class="space-y-6">
	<div>
		<h1 class="text-3xl font-bold">원소 · 원자 · 분자</h1>
		<p class="mt-2 max-w-2xl text-[#adb5bd]">
			수소(H)부터 오가네손(Og)까지 118원소를 세 관점으로 학습합니다. Minecraft 26.2 Fabric 야생 모드와
			같은 <code class="text-[#e8c86a]">shared-data</code>를 씁니다.
		</p>
	</div>
	<div class="grid gap-4 md:grid-cols-2">
		{#each modes as m}
			<a href={m.href} class="rounded-2xl border border-[#1f2a3a] bg-[#121a27] p-5 hover:border-[#e8c86a]">
				<h2 class="text-xl font-semibold">{m.title}</h2>
				<p class="mt-2 text-sm text-[#adb5bd]">{m.desc}</p>
			</a>
		{/each}
	</div>
</section>
