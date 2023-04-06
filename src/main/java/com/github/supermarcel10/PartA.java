package com.github.supermarcel10;


public class PartA {
	// Part A.1
	// Worst case complexity : O(v)
	// Best case complexity : O(1)
	public static boolean checkClause(int[] assignment, int[] clause) {
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
	public static boolean checkClauseDatabase(int[] assignment, int[][] clauseDatabase) {
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
	public static int checkClausePartial(int[] partialAssignment, int[] clause) {
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
	public static int findUnit(int[] partialAssignment, int[] clause) {
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
}
