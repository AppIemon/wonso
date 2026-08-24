package com.appiemon.wonso.data;

import java.util.List;
import java.util.Map;

public record ElementRecord(
		int z,
		String symbol,
		String nameKo,
		String nameEn,
		Double atomicMass,
		Integer period,
		Integer group,
		String category,
		String stateAtRoomTemp,
		String electronConfiguration,
		Double electronegativity,
		Integer displayRow,
		Integer displayColumn,
		boolean hasKnownMolecules,
		String descriptionKo,
		List<String> relatedMoleculeIds,
		Map<String, Object> card,
		Map<String, Object> battle
) {
	public String cardId() {
		return "element_card_" + symbol.toLowerCase();
	}
}
