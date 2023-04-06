package com.github.supermarcel10;

import java.util.*;


public class DPLL {
	public static Map<Integer, Boolean> DPLL(List<List<Integer>> clauses, Set<Integer> symbols) {
		Stack<Map<Integer, Boolean>> stack = new Stack<>();
		Map<Integer, Boolean> model = new HashMap<>();
		stack.push(model);

		while (!stack.empty()) {
			model = stack.pop();
			if (model.size() == symbols.size()) {
				Map<Integer, Boolean> finalModel = model;
				if (clauses.stream().allMatch(c -> c.stream().anyMatch(l -> finalModel.containsKey(Math.abs(l)) && ((l > 0) == finalModel.get(Math.abs(l)))))) {
					return model;
				}
			} else {
				int symbol = 0;
				for (int s : symbols) {
					if (!model.containsKey(s)) {
						symbol = s;
						break;
					}
				}

				Map<Integer, Boolean> trueModel = new HashMap<>(model);
				trueModel.put(symbol, true);
				if (clauses.stream().noneMatch(c -> c.stream().allMatch(l -> trueModel.containsKey(Math.abs(l)) && ((l > 0) != trueModel.get(Math.abs(l)))))) {
					stack.push(trueModel);
				}

				Map<Integer, Boolean> falseModel = new HashMap<>(model);
				falseModel.put(symbol, false);
				if (clauses.stream().noneMatch(c -> c.stream().allMatch(l -> falseModel.containsKey(Math.abs(l)) && ((l > 0) != falseModel.get(Math.abs(l)))))) {
					stack.push(falseModel);
				}
			}
		}
		return null;
	}

	public static void main(String[] args) {
		List<List<Integer>> clauses = new ArrayList<>();
		// 1
//		clauses.add(Arrays.asList(-1, 1));
//		clauses.add(Arrays.asList(3, 1));
//		clauses.add(Arrays.asList(-2));

		// 2
//		clauses.add(Arrays.asList(-1, 1));
//		clauses.add(Arrays.asList(3, 1));
//		clauses.add(Arrays.asList(-2));
//		clauses.add(Arrays.asList(3, 2));
//		clauses.add(Arrays.asList(2, 3));
//		clauses.add(Arrays.asList(-3, -1));
//		clauses.add(Arrays.asList(2, -3));
//		clauses.add(Arrays.asList(-3, 3));

		// 3
		clauses.add(Arrays.asList(-1, 2, -4));
		clauses.add(Arrays.asList(-4, -3, 4));
		clauses.add(Arrays.asList(-3, -1, 2));
		clauses.add(Arrays.asList(3, 4, -1));
		clauses.add(Arrays.asList(1, -4, -2));
		clauses.add(Arrays.asList(4, -1, -4));


		Set<Integer> symbols = new HashSet<>(Arrays.asList(1, 2, 3));
		Map<Integer, Boolean> result = DPLL(clauses, symbols);
		System.out.println(result);
	}
}
