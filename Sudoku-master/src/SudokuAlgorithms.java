import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.io.*;
import java.util.*;
import static java.lang.Math.exp;
import static java.lang.Math.random;

public class SudokuAlgorithms {

	// Testing webhook
	/* Implement the backtracking algorithm */
	public boolean backtracking(Sudoku s) {

		if (s.complete()) {
			return true;
		}

		// pick next empty spot
		Cell cell = s.popFromUnassigned();
		int row = cell.position.row;
		int col = cell.position.col;

		// Duplicating it, to avoid modification horrors
		Set<Integer> domainList = new HashSet<>();

		domainList.addAll(s.board[row][col].domain);

		for (Integer val : domainList) {
			s.board[row][col].val = val;
			if (s.constraintsPresent(row, col)) { // Cut DOWN option, which violates constraints
				continue;
			}

			if (backtracking(s)) {
				return true;
			} else {
				continue;
			}
		}
		s.board[row][col].val = 0;
		s.pushBackToUnassigned(cell);
		return false;
	}

	private boolean eliminateDomainValuesFromConstraints(Cell cell) {

		for (Cell constraintCell : cell.constraints) {
			if (constraintCell.val != 0) // We should only check variables that are not assigned values and are
											// neighbors
				continue;

			boolean removed = constraintCell.removeFromDomain(cell.val);

			if (removed == true)
				cell.removedFromDomainSet.add(constraintCell);

			if (constraintCell.domain.size() == 0) {
				// System.out.println("WIPEOUT: Removing value"+cell.val+" cell- "+cell+" -
				// contrainst"+constraintCell);
				return false;
			}
		}

		return true;
	}

	private void addDomainValuesToConstraint(Cell cell) {

		for (Cell constraintCell : cell.constraints) {

			if (cell.removedFromDomainSet.remove(constraintCell) == false)
				continue;

			if (constraintCell.val != 0)
				continue;
			constraintCell.domain.add(cell.val);
		}
	}

	/* Implement the backtracking algorithm with forward checking */

	public boolean forwardChecking(Sudoku s) {

		if (s.complete()) {
			return true;
		}

		// pick next empty spot
		Cell cell = s.popFromUnassigned();

		int row = cell.position.row;
		int col = cell.position.col;

		Set<Integer> domainList = new HashSet<>();

		domainList.addAll(s.board[row][col].domain);

		for (Integer val : domainList) {
			s.board[row][col].val = val;

			// We don't have to check constraintsPresent for forwardChecking, because, we
			// make sure in previous step that the neighbor constraint domain is not empty
			// if (s.constraintsPresent(row, col)) { // Cut DOWN option, which violates
			// constraints
			// System.out.println("Hello");
			// continue;
			// }

			if (!eliminateDomainValuesFromConstraints(s.board[row][col])) {
				// oopsie, we got domain-wipeout
				// rollback
				// System.out.println(s);
				addDomainValuesToConstraint(s.board[row][col]);
				s.board[row][col].val = 0;
			} else {

				if (forwardChecking(s)) {
					return true;
				} else {
					addDomainValuesToConstraint(s.board[row][col]);
					s.board[row][col].val = 0;
					continue;
				}
			}

		}
		s.board[row][col].val = 0;
		s.pushBackToUnassigned(cell);
		return false;

	}

	/*
	 * AC3 Algorithm - remove arc inconsistency
	 */
	private Sudoku ac3(Sudoku s, Stack<Cell> stack) {

		while (!stack.isEmpty()) {

			Cell cell = stack.pop();

			int cellRow = cell.position.row;
			int cellCol = cell.position.col;

			// Only single value is left
			if (s.board[cellRow][cellCol].domain.size() == 1)
				continue;

			Set<Integer> valuesToRemoveFromDomain = new HashSet<>();

			for (Cell constraintCell : cell.constraints) {
				if (constraintCell.val == 0)
					continue;
				if (!cell.domain.contains((Integer) constraintCell.val))
					continue;

				valuesToRemoveFromDomain.add(constraintCell.val);

			}

			// No change in Domain, don't continue any further, pop new node
			if (valuesToRemoveFromDomain.isEmpty())
				continue;

			// remove the value from domain + add it's neighbors in stack

			for (int val : valuesToRemoveFromDomain) {
				s.board[cellRow][cellCol].removeFromDomain(val);
			}

			// Only one value if left, so that will only be the solution
			if (s.board[cellRow][cellCol].domain.size() == 1) {

				Iterator<Integer> itr = s.board[cellRow][cellCol].domain.iterator();

				while (itr.hasNext()) {
					s.board[cellRow][cellCol].val = (int) itr.next();
				}
			}

			// Now, it's time to add it's neighbors to stack

			for (Cell constraintCell : cell.constraints) {
				if (constraintCell.val != 0)
					continue; // No need to add them, as their domain would not change
				if (!stack.contains(constraintCell))
					stack.add(constraintCell);
			}
		}
		return s;
	}

	/*
	 * Pre-process the constraint graph with ac3: adds all nodes which are
	 * unassigned initially
	 */
	private Sudoku preeProcessAC3(Sudoku s) {
		Stack<Cell> stack = new Stack<>();
		// Initially, add all cells in Queue
		for (Cell cell : s.unassigned) {
			stack.push(cell);
		}

		return ac3(s, stack);
	}

	/*
	 * Preprocess the constraint graph with ac3 and then run backtracking algorithm
	 */
	public boolean runAC3backtracking(Sudoku s) {
		return backtracking(preeProcessAC3(s));
		// return forwardChecking(preeProcessAC3(s));
	}

	/* Implement the heuristics on vertex order and value/vertex order */
	public boolean heuristicFC(Sudoku s) {
		if (s.complete()) {
			return true;
		}

		// pick next empty spot
		Cell cell = s.findMRVCell();
		int row = cell.position.row;
		int col = cell.position.col;

		int domainList[] = s.findLCValues(cell);

		for (int i = 0; i < domainList.length; i++) {
			int val = domainList[i];
			s.board[row][col].val = val;

			if (s.constraintsPresent(row, col)) { // Cut DOWN option, which violates constraints
				continue;
			}

			if (heuristic(s)) {
				return true;
			} else {
				continue;
			}
		}
		s.board[row][col].val = 0;
		s.pushBackToUnassigned(cell);
		return false;
	}
	
	public boolean heuristic (Sudoku s) {
		if (s.complete()) {
			return true;
		}

		// pick next empty spot
		Cell cell = s.findMRVCell();
		int row = cell.position.row;
		int col = cell.position.col;

		int domainList[] = s.findLCValues(cell);

		for (int i = 0; i < domainList.length; i++) {
			int val = domainList[i];
			s.board[row][col].val = val;

			if (s.constraintsPresent(row, col)) { // Cut DOWN option, which violates constraints
				continue;
			}

			if (heuristic(s)) {
				return true;
			} else {
				continue;
			}
		}
		s.board[row][col].val = 0;
		s.pushBackToUnassigned(cell);
		return false;
	}

	private void randomizeSubSquares(Sudoku s) {
		Set<Integer> set;
		int colCount = 0;
		while (colCount < 3) { // Iterate through 3 cols at a time
			int rowCount = 0;
			while (rowCount < 3) { // For those 3 cols, iterate 3 rows
				//find which numbers are already present and insert into HashSet
				set = new HashSet<>();
				set.add(1);
				set.add(2);
				set.add(3);
				set.add(4);
				set.add(5);
				set.add(6);
				set.add(7);
				set.add(8);
				set.add(9);
				for (int row = rowCount * 3; row < rowCount * 3 + 3; row++) {
					for (int col = colCount * 3; col < colCount * 3 + 3; col++) {
						// check if number was already present?
						int val = s.board[row][col].val;
						if (val == 0) {
							continue;
						}
						set.remove(val);
					}
				}
				for (int row = rowCount * 3; row < rowCount * 3 + 3; row++) {
					for (int col = colCount * 3; col < colCount * 3 + 3; col++) {
						// check if number was already present?
						int val = s.board[row][col].val;
						if (val == 0) {
							Iterator <Integer> iterSet = set.iterator();
							s.board[row][col].val = iterSet.next();
							iterSet.remove();
						}
					}
				}
				rowCount++;
			}
			colCount++;
		}
		return;
	}

	private int simAnnScoringHeuristic(Sudoku s) {
		int score = 0;
		Set<Integer> set;

		//iterate through rows
		for(int row = 0; row < 9; row++) {
			set = new HashSet<>();
			for(int col = 0; col < 9; col++) {
				int value = s.board[row][col].val;
				if(value != 0) {
					set.add(value);
				}
			}
			score = score + (set.size()*(-1));
		}

		for(int col = 0; col < 9; col++) {
			set = new HashSet<>();
			for(int row = 0; row < 9; row++) {
				int value = s.board[row][col].val;
				if(value != 0) {
					set.add(value);
				}
			}
			score = score + (set.size()*(-1));
		}
		return score;
	}

	private void swapWithinRandomSubsquare(Sudoku s) {
		//Need to make sure to preserve "hardwired" default given value.  Can't switch values that were part of the original puzzle data
		int max = 2;
		int min  = 0;

		Random r = new Random();
		int randSubSquareRow = r.nextInt((max - min) + 1) + min;
		int randSubSquareCol = r.nextInt((max - min) + 1) + min;

		int colSubSquare = randSubSquareCol*3;
		int rowSubSquare = randSubSquareRow*3;
		boolean swapped = false;
		while(swapped == false) {
			int randElement1Row = r.nextInt((max - min) + 1) + min;
			int randElement1Col = r.nextInt((max - min) + 1) + min;
			int randElement2Row = r.nextInt((max - min) + 1) + min;
			int randElement2Col = r.nextInt((max - min) + 1) + min;
			Cell elem1 = s.board[rowSubSquare + randElement1Row][colSubSquare + randElement1Col];
			Cell elem2 = s.board[rowSubSquare + randElement2Row][colSubSquare + randElement2Col];
			if(elem1.hardwired == false && elem2.hardwired == false) {
				int temp = s.board[rowSubSquare + randElement1Row][colSubSquare + randElement1Col].val;
				s.board[rowSubSquare + randElement1Row][colSubSquare + randElement1Col].val = s.board[rowSubSquare + randElement2Row][colSubSquare + randElement2Col].val;
				s.board[rowSubSquare + randElement2Row][colSubSquare + randElement2Col].val = temp;
				swapped = true;
			}
		}
	}

	/* Implement simulated annealing instead of the backtracking algorithm */
	//https://en.wikipedia.org/wiki/Simulated_annealing
	//https://arxiv.org/pdf/1203.2295.pdf
	//http://www.site.uottawa.ca/~lucia/courses/5165-11/a3.pdf
	public boolean simulatedAnnealing(Sudoku s) {
		//1. Populate remaining zero-value elements within puzzle with random values while maintaining subblock constraint integrity
		Sudoku current = s;
		randomizeSubSquares(current);
		int currentScore = simAnnScoringHeuristic(current);
		double t = 0.5;
		int counter = 0;

		while (counter < 600000) {
			//Generate neighbor by swapping the elements of random two randomly-generated cells
			Sudoku neighbor = new Sudoku(current);
			swapWithinRandomSubsquare(neighbor);
			int neighborScore = simAnnScoringHeuristic(neighbor);
			Random random = new Random();
			if (exp((currentScore - neighborScore) / t) >= random.nextFloat()) {  //EXP AND RANDOM ARE CORRECT???
				current = neighbor;
				currentScore = neighborScore;
			}
			if (neighborScore == -162) {
				current = neighbor;
				currentScore = neighborScore;
				break;
			}
			t = 0.99999 * t;
			counter++;
			if(counter == 599999) {
				counter = 0;
				t = 0.5;
			}
		}

		if(currentScore == -162) {
			//System.out.println(current);
			return true;
		}
		return false;
	}

	enum levels {
		easy, medium, hard
	};

	public static void seeStats() {

		SudokuAlgorithms algo = new SudokuAlgorithms();

		for (levels level : levels.values()) {

			for (int number = 1; number <= 5; number++) {

				System.out.print(level.toString() + "" + number + "    ");

				for (int j = 1; j <= 5; j++) {
					Sudoku s = new Sudoku(level.toString(), number);
					// System.out.println(s);
					long startTime, stopTime, elapsedTime;
					startTime = System.currentTimeMillis();

					switch (j) {
					case 1:
						System.out.print(algo.backtracking(s) + " --> ");
						break;
					case 2:
						System.out.print(algo.forwardChecking(s) + " --> ");
						break;
					case 3:
						System.out.print(algo.runAC3backtracking(s) + " --> ");
						break;
					case 4:
						System.out.print(algo.heuristic(s) + " --> ");
						break;
					case 5:
						System.out.print(algo.simulatedAnnealing(s) + " --> ");
						break;
					}
					stopTime = System.currentTimeMillis();
					elapsedTime = stopTime - startTime;
					String filename="./Results/"+level.toString()+""+number+".txt";
					try{
					BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
					writer.write(s.printBoard());
							     
				    writer.close();}
					catch(Exception e){System.out.println(e);}
							
					System.out.print(elapsedTime + "   ");
				}
				System.out.println();
			}
			System.out.println();

		}

	}

	public static void main(String ar[]) {

		seeStats();

		// Sudoku s = new Sudoku("easy", 2);
		// System.out.println(s);

	}

}
