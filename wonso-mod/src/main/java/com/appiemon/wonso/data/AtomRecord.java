package com.appiemon.wonso.data;

import java.util.List;

public record AtomRecord(
		int z,
		int defaultProtons,
		int defaultNeutrons,
		int defaultElectrons,
		List<Integer> shellDistribution,
		int valenceElectrons
) {
}
