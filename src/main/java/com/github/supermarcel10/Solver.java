/**
 * @author Supermarcel10
 */

package com.github.supermarcel10;

// IN1002 Introduction to Algorithms
// Coursework 2022/2023
//
// Submission by
// Marcel Barlik
// marcel.barlik@city.ac.uk

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Solver {
	private int[][] clauseDatabase;
	private int[][] originalDatabase;
	private int[] assignment = null;

	private boolean assignmentChanged = true;
	private int numberOfVariables = 0;
	static long startTime, endTime;

	/* You answers go below here */

	// Part A.1
	// Worst case complexity : O(v)
	// Best case complexity : O(1)
	public boolean checkClause(int[] assignment, int[] clause) {
		try {
			for (int literal : clause) {
				if (assignment[Math.abs(literal)] * literal > 0) {
					return true;
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Assignment is not long enough!");
			return false;
		}
		return false;
	}

	// Part A.2
	// Worst case complexity : O(l * c)
	// Best case complexity : O(l)
	public boolean checkClauseDatabase(int[] assignment, int[][] clauseDatabase) {
		try {
			for (int[] clause : clauseDatabase) {
				if (!checkClause(assignment, clause)) {
					return false;
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Assignment is not long enough!");
			return false;
		}
		return true;
	}

	// Part A.3
	// Worst case complexity : O(v)
	// Best case complexity : O(1)
	public int checkClausePartial(int[] partialAssignment, int[] clause) {
		boolean isUnknown = false;

		for (int literal : clause) {
			int absolute = Math.abs(literal);
			if (partialAssignment[absolute] * literal > 0) return 1;
			if (partialAssignment[absolute] == 0) isUnknown = true;
		}

		return isUnknown ? 0 : -1;
	}

	// Part A.4
	// Worst case complexity : O(v)
	// Best case complexity : O(1)
	public int findUnit(int[] partialAssignment, int[] clause) {
		int valLiteral = 0;

		for (int literal : clause) {
			if (partialAssignment[Math.abs(literal)] == 0) {
				if (valLiteral == 0) valLiteral = literal;
				else return 0;
			} else if (partialAssignment[Math.abs(literal)] == -literal) {
				return 0;
			}
		}

		return valLiteral;
	}





















	// Part B
	// I think this can solve ????
	public int[] checkSat(int[][] clauseDatabase) {
		assignment = new int[numberOfVariables + 1];
		this.clauseDatabase = clauseDatabase;
		this.originalDatabase = clauseDatabase;

		return startSat();
	}

	public int[] startSat() {
		// TODO: Possibly remove last run since it's the same.
		while (assignmentChanged) {
			System.out.println(Arrays.toString(assignment));
			System.out.println(Arrays.deepToString(clauseDatabase));

			optimiseClauses();

			System.out.println(Arrays.toString(assignment));
			System.out.println(Arrays.deepToString(clauseDatabase));

			removeKnownClauses();

			System.out.println(Arrays.toString(assignment));
			System.out.println(Arrays.deepToString(clauseDatabase));
		}

		System.out.println(Arrays.toString(assignment));
		System.out.println(Arrays.deepToString(clauseDatabase));

		// TODO: DPLL or CDCL
//		decisionLevel = new int[assignment.length];
//		CDCL();
//		if (clauseDatabase.length == 1) {
//			assignment[Math.abs(clauseDatabase[0][0])] = clauseDatabase[0][0] / Math.abs(clauseDatabase[0][0]);
//		}

		if (checkUnsat()) return null;

		// Default all unassigned variables to 1.
//		IntStream.range(1, assignment.length)
//				.forEach(i -> {
//					if (assignment[i] == 0) {
//						assignment[i] = 1;
//					}
//				});

		System.out.println(Arrays.toString(assignment));
		System.out.println(Arrays.deepToString(clauseDatabase));

		clauseDatabase = originalDatabase; originalDatabase = null;

		return assignment;
	}

	/*
	1. Remove duplicates within each clause
	2. Sort literals in each clause
	3. Remove duplicate clauses
	4. Remove clauses containing both a literal and its negation
	5. Collect unit clauses and assign values
	6. Remove clauses with assigned literals
	 */
	public void optimiseClauses() {
		// Step 1, 2, 3, and 4: Remove duplicates within each clause, sort literals, remove duplicate clauses, and remove literals and their negations within each clause
		Set<List<Integer>> distinctSortedClauses = Arrays.stream(clauseDatabase)
				.map(clause -> Arrays.stream(clause)
						.distinct()
						.boxed()
						.collect(Collectors.groupingBy(Math::abs, Collectors.summingInt(Integer::intValue))))
				.map(literalSumMap -> literalSumMap.values().stream()
						.filter(literalSum -> literalSum != 0)
						.collect(Collectors.toList()))
				.filter(clause -> !clause.isEmpty())
				.collect(Collectors.toSet());

		// TODO: CHEK IF THE INVERSE EXISTS AND RETURN UNSAT!
		// Step 5: Collect unit clauses and assign values
		List<Integer> unitClauses = distinctSortedClauses.stream()
				.filter(clause -> clause.size() == 1)
				.map(clause -> clause.get(0))
				.toList();

		assignmentChanged = false;
		for (int unitClause : unitClauses) {
			assignmentChanged = true;
			assignment[Math.abs(unitClause)] = unitClause / Math.abs(unitClause);
		}

		// Step 6: Remove clauses with assigned literals
		Set<Integer> assignedLiterals = new HashSet<>(unitClauses);
		distinctSortedClauses.removeIf(clause -> clause.size() == 1 && assignedLiterals.contains(clause.get(0)));

		// Convert the set of distinctSortedClauses back to the int[][] format for clauseDatabase
		clauseDatabase = distinctSortedClauses.stream()
				.map(clause -> clause.stream().mapToInt(Integer::intValue).toArray())
				.toArray(int[][]::new);
	}

	public void removeKnownClauses() {
		List<Integer> nonZeroAssignments = IntStream.range(0, assignment.length)
				.filter(i -> assignment[i] != 0)
				.map(i -> i * assignment[i])
				.boxed()
				.toList();

		Set<Integer> inverseLiterals = nonZeroAssignments.stream()
				.map(literal -> -literal)
				.collect(Collectors.toSet());

		// Filter out clauses containing any literals specified in nonZeroAssignments
		clauseDatabase = Arrays.stream(clauseDatabase)
				.filter(clause -> Arrays.stream(clause).noneMatch(nonZeroAssignments::contains))
				.toArray(int[][]::new);

		// Remove inverse literals from the remaining clauses
		for (int i = 0; i < clauseDatabase.length; i++) {
			clauseDatabase[i] = Arrays.stream(clauseDatabase[i])
					.filter(literal -> !inverseLiterals.contains(literal))
					.toArray();
		}
	}

	/*
	1. Filter unit clauses and collect their literals in a set
	 */
	public boolean checkUnsat() {
		Set<Integer> unitLiterals = Arrays.stream(clauseDatabase)
				.filter(clause -> clause.length == 1)
				.map(clause -> clause[0])
				.collect(Collectors.toSet());

		// Check if there are any conflicting unit clauses
		return unitLiterals.stream().anyMatch(literal -> unitLiterals.contains(-literal));
	}

	private List<int[]> reasons;
	private List<Integer> decisionLevels;
	private int[] decisionLevel;
	private Stack<Integer> assignmentStack;
	private boolean conflict;

	public void CDCL() {
		// Initialize necessary data structures
		assignmentStack = new Stack<>();
		decisionLevels = new ArrayList<>(Collections.nCopies(assignment.length, 0));
		decisionLevel = new int[assignment.length];
		reasons = new ArrayList<>(Collections.nCopies(assignment.length, null));
		conflict = false;

		// Apply the forced literals from the partial assignment
		for (int i = 1; i < assignment.length; i++) {
			if (assignment[i] != 0) {
				int literal = (assignment[i] > 0) ? i : -i;
				propagate(literal);
			}
		}

		while (true) {
			if (conflict) {
				if (decisionLevels.get(Math.abs(assignmentStack.peek())) == 0) {
					assignment = null;
					return;
				}

				int[] learnedClause = analyzeConflict();
				int backtrackLevel = computeBacktrackLevel(learnedClause);

				backtrack(backtrackLevel);

				clauseDatabase = Arrays.copyOf(clauseDatabase, clauseDatabase.length + 1);
				clauseDatabase[clauseDatabase.length - 1] = learnedClause;

				conflict = false;
			} else if (assignmentStack.size() == assignment.length) {
				return;
			} else {
				int literal = chooseLiteral();
				if (literal == 0) return;
				int currentDecisionLevel = decisionLevels.get(Math.abs(literal));
				decisionLevels.set(Math.abs(literal), currentDecisionLevel + 1);
				propagate(literal);
			}
		}
	}

//	private void propagate(int literal) {
//		assignmentStack.push(literal);
//		for (int[] clause : clauseDatabase) {
//			int unassignedCount = 0;
//			int unassignedLiteral = 0;
//			boolean satisfied = false;
//			for (int lit : clause) {
//				int value = assignment[Math.abs(lit)];
//				if (value == 0) {
//					unassignedCount++;
//					unassignedLiteral = lit;
//				} else if ((value > 0) == (lit > 0)) {
//					satisfied = true;
//					break;
//				}
//			}
//
//			if (!satisfied && unassignedCount == 1) {
//				int varIndex = Math.abs(unassignedLiteral);
//				assignment[Math.abs(unassignedLiteral)] = Integer.signum(unassignedLiteral);
//				decisionLevel[varIndex] = decisionLevels.get(varIndex) + 1;
//				assignmentStack.push(unassignedLiteral);
//				reasons.set(varIndex, clause);
//			} else if (!satisfied && unassignedCount == 0) {
//				conflict = true;
//			}
//		}
//
//		System.out.println("assignment after propagate: " + Arrays.toString(assignment));
//	}

	private void propagate(int literal) {
		assignmentStack.push(literal);
		for (int[] clause : clauseDatabase) {
			int unassignedCount = 0;
			int unassignedLiteral = 0;
			boolean satisfied = false;
			for (int lit : clause) {
				int value = assignment[Math.abs(lit)];
				if (value == 0) {
					unassignedCount++;
					unassignedLiteral = lit;
				} else if ((value > 0) == (lit > 0)) {
					satisfied = true;
					break;
				}
			}

//			System.out.println("clause: " + Arrays.toString(clause));
//			System.out.println("unassignedCount: " + unassignedCount);
//			System.out.println("unassignedLiteral: " + unassignedLiteral);
//			System.out.println("satisfied: " + satisfied);

			if (!satisfied && unassignedCount >= 1) {
				int varIndex = Math.abs(unassignedLiteral);
				System.out.println("propagating " + ((unassignedLiteral > 0) ? "" : "-") + varIndex + " to " + ((literal > 0) ? "true" : "false"));
				assignment[varIndex] = Integer.signum(literal);
				decisionLevel[varIndex] = decisionLevels.get(varIndex) + 1;
				assignmentStack.push(unassignedLiteral);
				reasons.set(varIndex, clause);
			} else if (!satisfied && unassignedCount == 0) {
				conflict = true;
			}
		}
	}

	private int computeBacktrackLevel(int[] learnedClause) {
		int maxDecisionLevel = -1;
		int secondMaxDecisionLevel = -1;

		for (int literal : learnedClause) {
			int decisionLevel = decisionLevels.get(Math.abs(literal));
			if (decisionLevel > maxDecisionLevel) {
				secondMaxDecisionLevel = maxDecisionLevel;
				maxDecisionLevel = decisionLevel;
			} else if (decisionLevel > secondMaxDecisionLevel && decisionLevel < maxDecisionLevel) {
				secondMaxDecisionLevel = decisionLevel;
			}
		}

		return secondMaxDecisionLevel;
	}

	private int[] analyzeConflict() {
		Map<Integer, Integer> level = new HashMap<>();
		Deque<Integer> trail = new ArrayDeque<>();
		int maxLevel = 0;

		// Construct implication graph and find the maximum decision level
		for (int i = 0; i < assignment.length; i++) {
			if (assignment[i] != 0) {
				int literal = (assignment[i] > 0) ? i + 1 : -(i + 1);
				trail.addLast(literal);
				level.put(literal, decisionLevel[i]);
				maxLevel = Math.max(maxLevel, decisionLevel[i]);
			}
		}

		// Initialize variables for the conflict analysis
		List<Integer> learnedClause = new ArrayList<>();
		int conflictLiteral = 0;
		int numLiteralsFromMaxLevel = 0;
		int lastUIP = 0;

		// Traverse the implication graph in reverse order
		while (!trail.isEmpty()) {
			int literal = trail.removeLast();
			if (level.get(literal) == maxLevel) {
				conflictLiteral = literal;
				numLiteralsFromMaxLevel++;
			}

			int[] reasonClause = reasons.get(Math.abs(literal));
			if (reasonClause != null) {
				for (int reasonLiteral : reasonClause) {
					if (reasonLiteral != -conflictLiteral) {
						if (!level.containsKey(reasonLiteral)) {
							level.put(reasonLiteral, decisionLevel[Math.abs(reasonLiteral) - 1]);
							trail.addLast(reasonLiteral);
						}
					}
				}
			} else {
				if (numLiteralsFromMaxLevel == 1) {
					lastUIP = literal;
					break;
				} else {
					learnedClause.add(-literal);
					numLiteralsFromMaxLevel--;
				}
			}
		}

		// Add the last UIP to the learned clause and return it
		learnedClause.add(-lastUIP);
		return learnedClause.stream().mapToInt(Integer::intValue).toArray();
	}

	private void backtrack(int level) {
		while (!assignmentStack.isEmpty() && decisionLevels.get(Math.abs(assignmentStack.peek())) > level) {
			int literal = assignmentStack.pop();
			assignment[Math.abs(literal)] = 0;
			decisionLevels.set(Math.abs(literal), level);
			reasons.set(Math.abs(literal), null);
		}
	}

	// DLIS Heuristic
	private int chooseLiteral() {
		int maxFrequency = -1;
		int chosenLiteral = 0;

		// Count frequencies of literals in unsatisfied clauses
		Map<Integer, Integer> literalFrequencies = new HashMap<>();
		for (int[] clause : clauseDatabase) {
			boolean unsatisfied = true;
			for (int literal : clause) {
				int variable = Math.abs(literal);
				if (assignment[variable - 1] == Integer.signum(literal)) {
					unsatisfied = false;
					break;
				}
			}
			if (unsatisfied) {
				for (int literal : clause) {
					int variable = Math.abs(literal);
					if (assignment[variable - 1] == 0) {
						literalFrequencies.put(literal, literalFrequencies.getOrDefault(literal, 0) + 1);
						if (literalFrequencies.get(literal) > maxFrequency) {
							maxFrequency = literalFrequencies.get(literal);
							chosenLiteral = literal;
						}
					}
				}
			}
		}
		System.out.println("chosen literal: " + chosenLiteral);
		return chosenLiteral;
	}



























	/*****************************************************************\
	 *** DO NOT CHANGE! DO NOT CHANGE! DO NOT CHANGE! DO NOT CHANGE! ***
	 *******************************************************************
	 *********** Do not change anything below this comment! ************
	 \*****************************************************************/

	public static void main(String[] args) {
		try {
			Solver mySolver = new Solver();

			System.out.println("Enter the file to check");

			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isr);
			String fileName = br.readLine();

			int returnValue = 0;

			Path file = Paths.get(fileName);
			BufferedReader reader = Files.newBufferedReader(file);

			startTime = System.currentTimeMillis();

			returnValue = mySolver.runSatSolver(reader);

			endTime = System.currentTimeMillis();
			System.out.println("Time taken: " + (endTime - startTime) + "ms");
			return;

		} catch (Exception e) {
			System.err.println("Solver failed :-(");
			e.printStackTrace(System.err);
			return;

		}
	}

	public int runSatSolver(BufferedReader reader) throws Exception, IOException {

		// First load the problem in, this will initialise the clause
		// database and the number of variables.
		loadDimacs(reader);

		// Then we run the part B algorithm
		int [] assignment = checkSat(clauseDatabase);

		// Depending on the output do different checks
		if (assignment == null) {
			// No assignment to check, will have to trust the result
			// is correct...
			System.out.println("s UNSATISFIABLE");
			return 20;

		} else {
			// Cross check using the part A algorithm
			boolean checkResult = checkClauseDatabase(assignment, clauseDatabase);

			if (checkResult == false) {
				throw new Exception("The assignment returned by checkSat is not satisfiable according to checkClauseDatabase?");
			}

			System.out.println("s SATISFIABLE");

			// Check that it is a well structured assignment
			if (assignment.length != numberOfVariables + 1) {
				throw new Exception("Assignment should have one element per variable.");
			}
			if (assignment[0] != 0) {
				throw new Exception("The first element of an assignment must be zero.");
			}
			for (int i = 1; i <= numberOfVariables; ++i) {
				if (assignment[i] == 1 || assignment[i] == -1) {
					System.out.println("v " + (i * assignment[i]));
				} else {
					throw new Exception("assignment[" + i + "] should be 1 or -1, is " + assignment[i]);
				}
			}

			return 10;
		}
	}

	// This is a simple parser for DIMACS file format
	void loadDimacs(BufferedReader reader) throws Exception, IOException {
		int numberOfClauses = 0;

		// Find the header line
		do {
			String line = reader.readLine();

			if (line == null) {
				throw new Exception("Found end of file before a header?");
			} else if (line.startsWith("c")) {
				// Comment line, ignore
				continue;
			} else if (line.startsWith("p cnf ")) {
				// Found the header
				String counters = line.substring(6);
				int split = counters.indexOf(" ");
				numberOfVariables = Integer.parseInt(counters.substring(0,split));
				numberOfClauses = Integer.parseInt(counters.substring(split + 1));

				if (numberOfVariables <= 0) {
					throw new Exception("Variables should be positive?");
				}
				if (numberOfClauses < 0) {
					throw new Exception("A negative number of clauses?");
				}
				break;
			} else {
				throw new Exception("Unexpected line?");
			}
		} while (true);

		// Set up the clauseDatabase
		clauseDatabase = new int[numberOfClauses][];

		// Parse the clauses
		for (int i = 0; i < numberOfClauses; ++i) {
			String line = reader.readLine();

			if (line == null) {
				throw new Exception("Unexpected end of file before clauses have been parsed");
			} else if (line.startsWith("c")) {
				// Comment; skip
				--i;
				continue;
			} else {
				// Try to parse as a clause
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				String working = line;

				do {
					int split = working.indexOf(" ");

					if (split == -1) {
						// No space found so working should just be
						// the final "0"
						if (!working.equals("0")) {
							throw new Exception("Unexpected end of clause string : \"" + working + "\"");
						} else {
							// Clause is correct and complete
							break;
						}
					} else {
						int var = Integer.parseInt(working.substring(0,split));

						if (var == 0) {
							throw new Exception("Unexpected 0 in the middle of a clause");
						} else {
							tmp.add(var);
						}

						working = working.substring(split + 1);
					}
				} while (true);

				// Add to the clause database
				clauseDatabase[i] = new int[tmp.size()];
				for (int j = 0; j < tmp.size(); ++j) {
					clauseDatabase[i][j] = tmp.get(j);
				}
			}
		}

		// All clauses loaded successfully!
		return;
	}

}