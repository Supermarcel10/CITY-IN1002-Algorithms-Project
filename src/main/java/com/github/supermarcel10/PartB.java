package com.github.supermarcel10;

import java.util.*;
import java.util.stream.Collectors;

// Part B
// I think this can solve ????
import java.util.*;
import java.util.stream.Collectors;

public class PartB {
	public static int[] checkSat(int[][] clauses) {
		return DPLL(clauses);
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
				boolean pureLiteralFound = false;

				// Pure Literal Elimination
				Map<Integer, Boolean> finalModel1 = model;
				Map<Integer, Long> literalCounts = clauseList.stream()
						.filter(c -> !c.stream().anyMatch(l -> finalModel1.containsKey(Math.abs(l)) && ((l > 0) != finalModel1.get(Math.abs(l)))))
						.flatMap(List::stream)
						.collect(Collectors.groupingBy(l -> l, Collectors.counting()));

				for (int s : symbols) {
					if (!model.containsKey(s)) {
						Long countPositive = literalCounts.getOrDefault(s, 0L);
						Long countNegative = literalCounts.getOrDefault(-s, 0L);

						if (countPositive > 0 && countNegative == 0) {
							symbol = s;
							pureLiteralFound = true;
							break;
						} else if (countPositive == 0 && countNegative > 0) {
							symbol = -s;
							pureLiteralFound = true;
							break;
						}
					}
				}

				if (pureLiteralFound) {
					Map<Integer, Boolean> pureModel = new HashMap<>(model);
					pureModel.put(Math.abs(symbol), symbol > 0);
					stack.push(pureModel);
					assignment[Math.abs(symbol)] = symbol > 0 ? 1 : -1;
				} else {
					for (int s : symbols) {
						if (!model.containsKey(s)) {
							symbol = s;
							break;
						}
					}

					if (symbol != 0) { // if symbol is found
						Map<Integer, Boolean> trueModel = new HashMap<>(model);
						trueModel.put(symbol, true);
						if (clauseList.stream().noneMatch(c -> c.stream().allMatch(l -> trueModel.containsKey(Math.abs(l)) && ((l > 0) != trueModel.get(Math.abs(l)))))) {
							stack.push(trueModel);
							assignment[symbol] = 1; // update assignment array for true value
						}

						Map<Integer, Boolean> falseModel = new HashMap<>(model);
						falseModel.put(symbol, false);
						if (clauseList.stream().noneMatch(c -> c.stream().allMatch(l -> falseModel.containsKey(Math.abs(l)) && ((l > 0) != falseModel.get(Math.abs(l)))))) {
							stack.push(falseModel);
							assignment[symbol] = -1; // update assignment array for false value
						}
					} else { // if symbol is not found, set to 0
						assignment[symbols.size()] = 0;
						return assignment;
					}
				}
			}
		}
		return null;
	}
}
