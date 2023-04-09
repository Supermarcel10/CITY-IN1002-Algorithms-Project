package com.github.supermarcel10;

import java.util.*;
import java.util.stream.Collectors;


/*
 * 1:
 * 2:
 * 3:
 * 4:
 * 5:
 * 6:
 * 7:
 * 8:
 * 9:
 * 10:
 * 11:
 * 12:
 * 13:
 * 14:
 * 15:
 */
public class PartB {
	public static int[] checkSat(int[][] clauses) {
		return DPLL(clauses, new HashMap<>());
	}

	private static int[] DPLL(int[][] clauses, Map<Integer, Integer> symbolOccurrences) {
		List<List<Integer>> clauseList = new ArrayList<>();
		for (int[] clause : clauses) {
			List<Integer> clauseAsList = Arrays.stream(clause).boxed().collect(Collectors.toList());
			clauseList.add(clauseAsList);
		}

		Set<Integer> symbols = new HashSet<>();

		// Check if the symbolOccurrences is already cached.
		boolean useCachedSymbolOccurrences = true;
		for (List<Integer> clause : clauseList) {
			for (int literal : clause) {
				int symbol = Math.abs(literal);
				symbols.add(symbol);
				if (!symbolOccurrences.containsKey(symbol)) {
					useCachedSymbolOccurrences = false;
					break;
				}
			}
			if (!useCachedSymbolOccurrences) {
				break;
			}
		}

		// If not cached, then calculate them
		for (List<Integer> clause : clauseList) {
			for (int literal : clause) {
				int symbol = Math.abs(literal);
				symbols.add(Math.abs(literal));
				symbolOccurrences.put(symbol, symbolOccurrences.getOrDefault(symbol, 0) + 1);
			}
		}

		// Two Clause Elimination
		for (List<Integer> clause : clauseList) {
			if (clause.size() == 2) {
				int symbol1 = Math.abs(clause.get(0));
				int symbol2 = Math.abs(clause.get(1));
				int occurrences1 = symbolOccurrences.get(symbol1);
				int occurrences2 = symbolOccurrences.get(symbol2);
				if (occurrences1 < occurrences2) {
					symbolOccurrences.put(symbol1, occurrences1 + 1);
				} else {
					symbolOccurrences.put(symbol2, occurrences2 + 1);
				}
			}
		}

		for (Iterator<List<Integer>> iterator = clauseList.iterator(); iterator.hasNext(); ) {
			List<Integer> clause = iterator.next();
			if (clause.size() == 2) {
				int symbol1 = Math.abs(clause.get(0));
				int symbol2 = Math.abs(clause.get(1));
				int occurrences1 = symbolOccurrences.get(symbol1);
				int occurrences2 = symbolOccurrences.get(symbol2);
				if (occurrences1 == 1 || occurrences2 == 1) {
					iterator.remove();
				}
			}
		}

		Stack<Map<Integer, Boolean>> stack = new Stack<>();
		Map<Integer, Boolean> model = new HashMap<>();
		stack.push(model);

		int[] assignment = new int[Solver.numberOfVariables + 1];

		while (!stack.empty()) {
			model = stack.pop();

			// Unit Propagation
			Map<Integer, Boolean> unitModel = new HashMap<>(model);
			boolean unitPropagation = true;

			while (unitPropagation) {
				unitPropagation = false;
				for (List<Integer> clause : clauseList) {
					if (isClauseSatisfied(clause, unitModel)) {
						continue;
					}

					List<Integer> unassignedLiterals = clause.stream().filter(l -> !unitModel.containsKey(Math.abs(l))).toList();
					if (unassignedLiterals.size() == 1) {
						int unitLiteral = unassignedLiterals.get(0);
						unitModel.put(Math.abs(unitLiteral), unitLiteral > 0);
						unitPropagation = true;
						break;
					}
				}
			}

			model = unitModel;

			// Pure Literal Elimination
			boolean pureLiteralFound = false;
			Map<Integer, Boolean> pureModel = new HashMap<>(model);
			for (int s : symbols) {
				if (!pureModel.containsKey(s)) {
					boolean positive = false;
					boolean negative = false;
					for (List<Integer> clause : clauseList) {
						if (clause.contains(s)) positive = true;
						if (clause.contains(-s)) negative = true;
						if (positive && negative) break;
					}
					if (positive != negative) {
						pureModel.put(s, positive);
						pureLiteralFound = true;
					}
				}
			}

			if (pureLiteralFound) model = pureModel;

			// Check if all clauses are satisfied
			boolean allSatisfied = true;
			for (List<Integer> clause : clauseList) {
				if (!isClauseSatisfied(clause, model)) {
					allSatisfied = false;
					break;
				}
			}

			if (allSatisfied) {
				for (int symbol : symbols) {
					Boolean value = model.get(symbol);
					if (value != null && value) {
						assignment[symbol] = 1;
					} else {
						assignment[symbol] = -1;
					}
				}
				return assignment; // SAT
			}

			// If there is an unassigned symbol, branch on its value
			Map<Integer, Boolean> finalModel = model;
			Optional<Integer> unassignedSymbol = symbols.stream().filter(s -> !finalModel.containsKey(s)).findFirst();
			if (unassignedSymbol.isPresent()) {
				int symbol = unassignedSymbol.get();
				Map<Integer, Boolean> newModelTrue = new HashMap<>(model);
				newModelTrue.put(symbol, true);
				stack.push(newModelTrue);

				Map<Integer, Boolean> newModelFalse = new HashMap<>(model);
				newModelFalse.put(symbol, false);
				stack.push(newModelFalse);
			}
		}

		return null; // UNSAT
	}

	private static boolean isClauseSatisfied(List<Integer> clause, Map<Integer, Boolean> model) {
		for (int literal : clause) {
			if (model.getOrDefault(Math.abs(literal), null) != null && model.get(Math.abs(literal)) == (literal > 0)) {
				return true;
			}
		}
		return false;
	}
}
