/** Zod schemas from ARCHITECTURE_PLAN §6 */

export type ElementCategory =
  | "alkali_metal"
  | "alkaline_earth_metal"
  | "transition_metal"
  | "post_transition_metal"
  | "metalloid"
  | "reactive_nonmetal"
  | "halogen"
  | "noble_gas"
  | "lanthanide"
  | "actinide"
  | "unknown";

export interface Element {
  z: number;
  symbol: string;
  nameKo: string;
  nameEn: string;
  atomicMass: number | null;
  massUncertainty?: string | null;
  period: number;
  group: number | null;
  category: ElementCategory;
  stateAtRoomTemp: "solid" | "liquid" | "gas" | "unknown";
  electronConfiguration: string | null;
  electronegativity: number | null;
  density: number | null;
  yearDiscovered: string | null;
  displayRow: number;
  displayColumn: number;
  aliasesKo?: string[];
  hasKnownMolecules: boolean;
  descriptionKo?: string;
  relatedMoleculeIds?: string[];
  card: { attack: number; health: number; rarity: string; cost: number; category: string };
  battle: { hp: number; attack: number; defense: number; speed: number; rarity: string };
}

export interface AtomModel {
  z: number;
  defaultProtons: number;
  defaultNeutrons: number;
  defaultElectrons: number;
  shellDistribution: number[];
  valenceElectrons: number;
  isotopes: { massNumber: number; neutrons: number; abundance: number | null; name?: string }[];
}

export interface Molecule {
  id: string;
  formula: string;
  formulaDisplay: string;
  nameKo: string;
  nameEn: string;
  elements: { symbol: string; count: number }[];
  relatedElements: number[];
  category: string;
  structureType?: string;
  descriptionKo: string;
  tier: "A" | "B" | "C";
  difficulty: 1 | 2 | 3;
  energy: number;
}

export interface QuizQuestion {
  id: string;
  mode: "element" | "atom" | "molecule" | "integrated";
  type: string;
  difficulty: 1 | 2 | 3;
  promptKo: string;
  choices?: string[] | null;
  correctAnswer: string | number;
  explanationKo: string;
  relatedZ?: number;
  relatedMoleculeId?: string;
}
