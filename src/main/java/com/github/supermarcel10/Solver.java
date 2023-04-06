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

import static com.github.supermarcel10.PartA.*;
import static com.github.supermarcel10.PartB.*;

public class Solver {
	private int[][] clauseDatabase;
	private int numberOfVariables = 0;
	static long startTime, endTime;

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