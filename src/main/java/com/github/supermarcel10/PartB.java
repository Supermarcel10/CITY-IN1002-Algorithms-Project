package com.github.supermarcel10;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 1: 13ms
 * 2: 8ms
 * 3: 9ms
 * 4: 17ms
 * 5: 21ms
 * 6: 51ms
 * 7: 77ms
 * 8: 53ms
 * 9: 744ms
 * 10: 1473ms
 * 11: 11646ms
 * 12: 4232ms
 * 13: NDF
 * 14: NDF
 * 15: NDF
 */

// TODO: 13, 14, 15
public class PartB {
	public static int[] checkSat(int[][] clauses) {
		int[] result = DPLL(clauses);
		System.out.println(Arrays.toString(result));
		return result;
	}

	private static int[] DPLL(int[][] clauses) {
		List<List<Integer>> clauseList = new ArrayList<>();
		for (int[] clause : clauses) {
			List<Integer> clauseAsList = Arrays.stream(clause).boxed().collect(Collectors.toList());
			clauseList.add(clauseAsList);
		}

		Set<Integer> symbols = new HashSet<>();
		for (List<Integer> clause : clauseList) {
			for (int literal : clause) {
				symbols.add(Math.abs(literal));
			}
		}

		Stack<Map<Integer, Boolean>> stack = new Stack<>();
		Map<Integer, Boolean> model = new HashMap<>();
		stack.push(model);

		int[] assignment = new int[symbols.size() + 1]; // initialize assignment array
		Arrays.fill(assignment, 0);

		while (!stack.empty()) {
			model = stack.pop();

			// Unit Propagation
			Map<Integer, Boolean> unitModel = new HashMap<>(model);
			boolean unitPropagation = true;
			while (unitPropagation) {
				unitPropagation = false;
				for (List<Integer> clause : clauseList) {
					if (clause.stream().anyMatch(l -> unitModel.containsKey(Math.abs(l)) && ((l > 0) == unitModel.get(Math.abs(l))))) {
						continue;
					}
					List<Integer> unassignedLiterals = clause.stream().filter(l -> !unitModel.containsKey(Math.abs(l))).collect(Collectors.toList());
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

			if (pureLiteralFound) {
				stack.push(pureModel);
			} else {
				if (model.size() == symbols.size()) {
					Map<Integer, Boolean> finalModel = model;
					if (clauseList.stream().allMatch(c -> c.stream().anyMatch(l -> finalModel.containsKey(Math.abs(l)) && ((l > 0) == finalModel.get(Math.abs(l)))))) {
						// update assignment array for true values in final model
						finalModel.forEach((k, v) -> {
							int index = (k > 0) ? k : -k;
							assignment[index] = v ? 1 : -1;
						});
						return assignment;
					}
				} else {
					int symbol = 0;
					for (int s : symbols) {
						if (!model.containsKey(s) && clauseList.stream().anyMatch(c -> c.contains(s) || c.contains(-s))) {
							symbol = s;
							break;
						}
					}

					if (symbol != 0) {
						Map<Integer, Boolean> trueModel = new HashMap<>(model);
						trueModel.put(symbol, true);
						stack.push(trueModel);

						Map<Integer,Boolean> falseModel = new HashMap<>(model);
						falseModel.put(symbol, false);
						stack.push(falseModel);
					} else {
						assignment[symbols.size()] = 0;
						return assignment;
					}
				}
			}
		}
		return null;
	}
}
