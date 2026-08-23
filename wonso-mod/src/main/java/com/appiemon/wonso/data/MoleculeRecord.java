package com.appiemon.wonso.data;

import java.util.List;

public record MoleculeRecord(
		String id,
		String formula,
		String formulaDisplay,
		String nameKo,
		String nameEn,
		String category,
		String descriptionKo,
		String tier,
		int difficulty,
		int energy,
		List<Part> elements,
		List<Integer> relatedElements
) {
	public record Part(String symbol, int count) {
	}

	public String cardId() {
		return "molecule_card_" + id.replace('-', '_');
	}
}
