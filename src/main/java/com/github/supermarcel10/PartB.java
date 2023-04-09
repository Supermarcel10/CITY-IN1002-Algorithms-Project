package com.github.supermarcel10;

import java.util.*;
import java.util.stream.Collectors;

// Part B
// I think this can solve ????
public class PartB {
	public static int[] checkSat(int[][] clauses) {
		int[] result = DPLL(clauses);
		System.out.println(Arrays.toString(result));
		return result;
	}

	private static void debug(String message) {
		System.out.println("DEBUG: " + message);
	}

	private static int[] DPLL(int[][] clauses) {
		Map<Integer, Integer> decisionLevels = new HashMap<>();
		List<Map<Integer, Boolean>> decisionModels = new ArrayList<>();
		int decisionLevel = 0;

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
		debug("New Model: " + model);
		decisionModels.add(new HashMap<>(model));

		int[] assignment = new int[symbols.size() + 1]; // initialize assignment array
		Arrays.fill(assignment, 0);

		Map<Integer, List<Integer>> antecedentClauses = new HashMap<>();

		while (!stack.empty()) {
			model = stack.pop();

			// Unit Propagation
			Map<Integer, Boolean> unitModel = new HashMap<>(model);
			boolean unitPropagation = true;
			Map<List<Integer>, Boolean> satisfiedCache = new HashMap<>();

			while (unitPropagation) {
				unitPropagation = false;
				for (List<Integer> clause : clauseList) {
					if (isClauseSatisfied(clause, unitModel, satisfiedCache)) {
						continue;
					}

					List<Integer> unassignedLiterals = clause.stream().filter(l -> !unitModel.containsKey(Math.abs(l))).toList();
					if (unassignedLiterals.size() == 1) {
						int unitLiteral = unassignedLiterals.get(0);
						unitModel.put(Math.abs(unitLiteral), unitLiteral > 0);
						antecedentClauses.put(Math.abs(unitLiteral), new ArrayList<>(clause));
						unitPropagation = true;
						break;
					}
				}
			}
			debug("After UP // Before PLE: " + unitModel);

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

			debug("After Pure Literal Elimination: " + pureModel);

			if (pureLiteralFound) {
				model = pureModel;
				debug("Pure Literal Elimination: " + model);
			}

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
				return assignment;
			}

			// Clause Learning
			int conflict = -1;
			for (List<Integer> clause : clauseList) {
				if (isClauseSatisfied(clause, model)) {
					continue;
				}

				conflict = clause.get(0);
				break;
			}

			int[] implicationGraph = createImplicationGraph(clauseList, model, decisionLevels, antecedentClauses);
			int uip = findUIP(implicationGraph, conflict);
			debug("Backtracking to decision level: " + uip);

			if (uip == -1) {
				return null; // UNSAT
			}

			while (decisionLevel > uip) {
				if (stack.empty()) {
					return null; // UNSAT
				}
				stack.pop();
				decisionLevel--;
			}
			if (!stack.empty()) {
				model = stack.peek();
			}

			if (decisionLevel + 1 < decisionModels.size()) {
				decisionModels.subList(decisionLevel + 1, decisionModels.size()).clear();
			}

			stack.push(model);
			decisionModels.add(new HashMap<>(model));
			decisionLevel++;
			debug("New Model: " + model);

			decisionLevels.put(Math.abs(conflict), decisionLevel + 1);
			model.put(Math.abs(conflict), conflict > 0);
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

	private static boolean isClauseSatisfied(List<Integer> clause, Map<Integer, Boolean> model, Map<List<Integer>, Boolean> cache) {
		if (cache.containsKey(clause)) {
			return cache.get(clause);
		}
		boolean satisfied = isClauseSatisfied(clause, model);
		cache.put(clause, satisfied);
		return satisfied;
	}

	private static int[] createImplicationGraph(List<List<Integer>> clauses, Map<Integer, Boolean> model, Map<Integer, Integer> decisionLevels, Map<Integer, List<Integer>> antecedentClauses) {
		int maxSymbol = 0;
		for (List<Integer> clause : clauses) {
			for (int literal : clause) {
				maxSymbol = Math.max(maxSymbol, Math.abs(literal));
			}
		}
		int[] implicationGraph = new int[maxSymbol + 1];
		Arrays.fill(implicationGraph, -1);

		for (int literal : model.keySet()) {
			if (decisionLevels.get(literal) == null || decisionLevels.get(literal) == 0) continue;
			debug("Literal: " + literal);

			List<Integer> antecedent = antecedentClauses.get(literal);
			int otherAssigned = -1;

			if (antecedent != null) {
				for (int otherLiteral : antecedent) {
					if (Math.abs(otherLiteral) != literal && model.get(Math.abs(otherLiteral)) == (otherLiteral > 0)) {
						otherAssigned = Math.abs(otherLiteral);
						break;
					}
				}
			}
			implicationGraph[literal] = otherAssigned;
		}
		return implicationGraph;
	}


	private static int findUIP(int[] implicationGraph, int conflict) {
		int vertex = Math.abs(conflict);
		int uip = -1;
		while (uip == -1) {
			int parent = implicationGraph[vertex];
			if (parent == -1) {
				uip = vertex;
			} else {
				vertex = parent;
			}
		}
		return uip;
	}
}
