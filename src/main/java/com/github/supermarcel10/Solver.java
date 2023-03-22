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


public class Solver {
	private int [][] clauseDatabase = null;
	private int numberOfVariables = 0;
	private double[] activity;

	static long startTime, endTime;

	/* You answers go below here */

	// Part A.1
	// Worst case complexity : O(v)
	// Best case complexity : O(1)
	public boolean checkClause(int[] assignment, int[] clause) {
		for (int literal : clause) {
			if (assignment[Math.abs(literal)] * literal > 0) {
				return true;
			}
		}

		return false;
	}

	// Part A.2
	// Worst case complexity : O(l * c)
	// Best case complexity : O(l)
	public boolean checkClauseDatabase(int[] assignment, int[][] clauseDatabase) {
		for (int[] clause : clauseDatabase) {
			if (!checkClause(assignment, clause)) {
				return false;
			}
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
		activity = new double[numberOfVariables + 1];
		int[] partialAssignment = new int[numberOfVariables + 1];

		unitPropagation(clauseDatabase, partialAssignment);

		return DPLL(clauseDatabase, partialAssignment);
	}


	private int[] DPLL(int[][] clauseDatabase, int[] partialAssignment) {
		boolean isUnknown = false;

		for (int[] clause : clauseDatabase) {
			int clauseCheck = checkClausePartial(partialAssignment, clause);
			if (clauseCheck == -1) {
				return null;
			} else if (clauseCheck == 0) {
				isUnknown = true;
			}
		}

		// Pure Literal Elimination
		Map<Integer, Integer> pureLiterals = getPureLiterals(clauseDatabase, partialAssignment);
		if (!pureLiterals.isEmpty()) {
			int[] newAssignment = Arrays.copyOf(partialAssignment, partialAssignment.length);

			for (int literal : pureLiterals.keySet()) {
				newAssignment[Math.abs(literal)] = literal > 0 ? 1 : -1;
			}

			return DPLL(clauseDatabase, newAssignment);
		}

		if (!isUnknown) {
			for (int i = 1; i < partialAssignment.length; i++) {
				if (partialAssignment[i] == 0) {
					partialAssignment[i] = 1;
				}
			}
			return partialAssignment;
		}

		int unassignedVar = selectVariable(partialAssignment);

		if (unassignedVar == 0) {
			return null;
		}

		int[] newAssignment = Arrays.copyOf(partialAssignment, partialAssignment.length);

		newAssignment[unassignedVar] = 1;
		int[] result = DPLL(clauseDatabase, newAssignment);
		if (result != null) {
			return result;
		} else {
			int[] conflictClause = null;
			for (int[] clause : clauseDatabase) {
				if (checkClausePartial(newAssignment, clause) == -1) {
					conflictClause = clause;
					break;
				}
			}
			if (conflictClause != null) {
				updateActivity(conflictClause);
			}
		}

		newAssignment[unassignedVar] = -1;
		return DPLL(clauseDatabase, newAssignment);
	}


	// Returns a Map of pure literals and their counts
	public Map<Integer, Integer> getPureLiterals(int[][] clauseDatabase, int[] partialAssignment) {
		Map<Integer, Integer> literalCount = new HashMap<>();

		for (int[] clause : clauseDatabase) {
			for (int literal : clause) {
				int var = Math.abs(literal);
				if (partialAssignment[var] == 0) {
					literalCount.put(literal, literalCount.getOrDefault(literal, 0) + 1);
				}
			}
		}

		Map<Integer, Integer> pureLiterals = new HashMap<>();

		for (Map.Entry<Integer, Integer> entry : literalCount.entrySet()) {
			int literal = entry.getKey();
			if (!literalCount.containsKey(-literal)) {
				pureLiterals.put(literal, 1);
			}
		}

		return pureLiterals;
	}



	private void unitPropagation(int[][] clauseDatabase, int[] partialAssignment) {
		boolean foundUnitClause;
		do {
			foundUnitClause = false;
			outer: for (int[] clause : clauseDatabase) {
				int unassignedLiteral = 0;
				boolean clauseSatisfied = false;

				for (int literal : clause) {
					int var = Math.abs(literal);
					if (partialAssignment[var] == 0) {
						if (unassignedLiteral == 0) {
							unassignedLiteral = literal;
						} else {
							continue outer;
						}
					} else if (partialAssignment[var] == (literal > 0 ? 1 : -1)) {
						clauseSatisfied = true;
						break;
					}
				}

				if (!clauseSatisfied && unassignedLiteral != 0) {
					int var = Math.abs(unassignedLiteral);
					partialAssignment[var] = unassignedLiteral > 0 ? 1 : -1;
					foundUnitClause = true;
				}
			}
		} while (foundUnitClause);

	}


	private void updateActivity(int[] conflictClause) {
		double maxActivity = 0;
		for (int literal : conflictClause) {
			int var = Math.abs(literal);
			activity[var] += 1;
			maxActivity = Math.max(maxActivity, activity[var]);
		}

		// Decay the activity scores periodically
		if (maxActivity > 1e100) {
			for (int i = 1; i < activity.length; i++) {
				activity[i] *= 1e-100;
			}
		}
	}


	private int selectVariable(int[] partialAssignment) {
		int selectedVar = 0;
		double maxActivity = -1;
		for (int i = 1; i < partialAssignment.length; i++) {
			if (partialAssignment[i] == 0 && activity[i] > maxActivity) {
				selectedVar = i;
				maxActivity = activity[i];
			}
		}
		return selectedVar;
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