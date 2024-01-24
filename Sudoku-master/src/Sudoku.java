import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/*
 * Represents SUDOKU board
 */

class Cell {
	int val;
	Set<Integer> domain;
	Cell constraints[];
	final boolean hardwired;
	final CellPosition position;
	Set<Cell> removedFromDomainSet; // keeps track of all constraint cells, from which domain value was removed

	public Cell(Cell copy) {
		this.val = copy.val;
		this.domain = new HashSet<Integer>();
		this.domain.addAll(copy.domain);
		this.constraints = copy.constraints;  //Durr
		this.hardwired = copy.hardwired;
		this.position = new CellPosition(copy.position.row, copy.position.col);
		this.removedFromDomainSet = new HashSet<>();
		//this.removedFromDomainSet.addAll(copy.removedFromDomainSet);
	}


	public Cell(int val, int row, int col) {
		this.val = val;
		domain = new HashSet<Integer>(); // true - value is present in domain, else removed
		position = new CellPosition(row, col);

		if (val == 0) {
			this.hardwired = false;
			for (int i = 1; i <= 9; i++)
				domain.add(i);
			constraints = new Cell[20];
			removedFromDomainSet = new HashSet<>();
		} else {
			this.hardwired = true;
			domain.add(val);
		}
	}

	@Override
	public boolean equals(Object obj) {
		Cell c = (Cell) obj;
		return this.position.row == c.position.row && this.position.col == c.position.col;
	}

	public boolean removeFromDomain(int val) {
		return domain.remove((Integer) val);
	}

	@Override
	public String toString() {
		String str = this.position.toString() + " = "+this.val+" {"+ this.domain.toString()+"}";
		return str;
	}
}

class CellPosition {
	int row;
	int col;

	public CellPosition(int row, int col) {
		this.row = row;
		this.col = col;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.row*10+this.col;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "["+this.row+"]["+this.col+"]";
	}

	@Override
	public boolean equals(Object obj) {
		CellPosition c = (CellPosition) obj;
		return this.row == c.row && this.col == c.col;
	}
}

public class Sudoku {

	Cell board[][];
	ArrayList<Cell> unassigned;

	public Sudoku(Sudoku s) {
		this.board = new Cell[9][9];

		for(int x = 0; x < 9; x++) {
			for(int y = 0; y < 9; y++) {
				this.board[x][y] = new Cell(s.board[x][y]);
			}
		}
		this.unassigned = s.unassigned;
	}

	/* initialize sudoku Board */
	public Sudoku(String level, int number) {
		// Populate the board
		// Read from the file
		this.board = new Cell[9][9];
		unassigned = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader("boardfiles/" + level + number))) {
			// Populate the board
			for (int i = 0; i < 9; i++) {
				String currLine = br.readLine();
				for (int j = 0; j < 9; j++) {
					int val = Integer.parseInt(currLine.charAt(j) + "");
					board[i][j] = new Cell(val, i, j);
					if (val == 0) {
						unassigned.add(board[i][j]);
					}
				}
			}

			// Now add the constraints for blank values

			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					if(this.board[i][j].hardwired) continue;
					addConstraints(this.board[i][j]);
				}
			}

			//Remove the hardwired domains values in cells which are blank
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					if(this.board[i][j].hardwired) continue;
					removeDomains(this.board[i][j]);
				}
			}


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addConstraints(Cell cell) {
		int startRow = cell.position.row / 3;
		int startCol = cell.position.col / 3;

		Set<CellPosition> pos = new HashSet<CellPosition>();
		// SQUARE
		for (int r = startRow * 3; r < startRow * 3 + 3; r++) {

			for (int c = startCol * 3; c < startCol * 3 + 3; c++) {
				if (r == cell.position.row && c == cell.position.col)
					continue;
				pos.add(new CellPosition(r, c));
//				cell.constraints[index++] = this.board[r][c];

			}
		}

		// ROW
		for (int i = 0; i < 9; i++) {
			if (i == cell.position.col)
				continue;
			pos.add(new CellPosition(cell.position.row, i));
//			cell.constraints[index++] = this.board[cell.position.row][i];
		}

		// Col
		for (int i = 0; i < 9; i++) {
			if (i == cell.position.row)
				continue;
			pos.add(new CellPosition(i, cell.position.col));
//			cell.constraints[index++] = this.board[i][cell.position.col];
		}
		int index = 0;
		for(CellPosition position: pos) {
			cell.constraints[index++] = this.board[position.row][position.col];
		}
	}

	private void removeDomains(Cell cell) {

		for(Cell constraintCell: cell.constraints) {

			if (!constraintCell.hardwired) continue;

			cell.domain.remove((Integer) constraintCell.val);
		}
	}

	/*
	 * returns true if Horizontal Constraints are present returns false otherwise
	 */
	public boolean allRowConstraintsViolated() {

		for (int row = 0; row < 9; row++) {

			// Using arr of 10 size to avoid unwanted complexity
			// With this we can directly map if some value is presentAlready or not
			boolean presentAlready[] = new boolean[10];

			for (int col = 0; col < 9; col++) {
				int val = board[row][col].val;
				if (val == 0) {
					continue;
				}
				if (presentAlready[val]) {
					return true;
				}

				presentAlready[val] = true;
			}
		}
		return false;
	}

	/*
	 * returns true if Vertical Constraints are present returns false otherwise
	 */
	public boolean allColsConstraintsViolated() {

		for (int col = 0; col < 9; col++) {
			// Using arr of 10 size to avoid unwanted complexity
			// With this we can directly map if some value is presentAlready or not
			boolean presentAlready[] = new boolean[10];

			for (int row = 0; row < 9; row++) {
				int val = board[row][col].val;
				if (val == 0) {
					continue;
				}
				if (presentAlready[val]) {
					return true;
				}

				presentAlready[val] = true;
			}
		}
		return false;
	}

	/*
	 * returns true if Square Constraints are present returns false otherwise
	 */

	public boolean allSquareConstraintsViolated() {

		int colCount = 0;
		while (colCount < 3) { // Iterate through 3 cols at a time
			int rowCount = 0;
			while (rowCount < 3) { // For those 3 cols, iterate 3 rows

				boolean presentAlready[] = new boolean[10];

				for (int row = rowCount * 3; row < rowCount * 3 + 3; row++) {

					for (int col = colCount * 3; col < colCount * 3 + 3; col++) {
						// check if number was already present?
						int val = board[row][col].val;
						if (val == 0) {
							continue;
						}
						if (presentAlready[val]) {
							return true;
						}
						presentAlready[val] = true;
					}
				}
				rowCount++;
			}
			colCount++;
		}
		return false;
	}

	/*
	 * Give it position where you have inserted value in the cell And checks if the
	 * update have violated any constraints Returns true - contraints violated false
	 * otherwise
	 */
	public boolean constraintsPresent(int row, int col) {

		// SquareConstraint
		// Figure out which square needs to be looked for this constraint
		Cell cell = this.board[row][col];

		for (Cell constraintCell: cell.constraints) {
			if (constraintCell.val == 0) //We should only check variables that are assigned values and are neighbors
				continue;
			if (cell.val == constraintCell.val)
				return true;
		}

		return false;
	}

	/*
	 * Checks if all Values are filled in Sudoku Does not Check if all Constraints
	 * are satisfied or not returns true -> if all values are filled false otherwise
	 */

	public boolean complete() {
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				if (board[row][col].val == 0)
					return false;
			}
		}
		return true;
	}

	/**/
	public int[] findEmptyCell() {

		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				if (board[row][col].val == 0) {
					int cell[] = { row, col };
					return cell;
				}
			}
		}
		return null;
	}

	/*
	 * Chooses variable with least constraining values, rules out few choices for
	 * neighbors
	 */
	class ValueCount implements Comparator<Integer> {
		int val;
		int count;

		public ValueCount(int val, int count) {
			this.val = val;
			this.count = count;
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			// TODO Auto-generated method stub
			return o1.compareTo(o2);
		}
	}

	/*It prefers the value that rules out the fewest choices for the neighboring variables in the constraint graph.*/
	public int[] findLCValues(Cell cell) {

		ValueCount vcount[] = new ValueCount[cell.domain.size()];
		int index = 0;
		for(int val: cell.domain) {
			int count = 0;
			//Count all constraints which does not have this value in domain, because then we might not need to delete it
			for(Cell constraint: cell.constraints) {
				if (constraint.val == 0 && !constraint.domain.contains((Integer) val)) { //Means if it's constraint domain does not contain val, then we don't have to worry about it
					count++;
				}
			}

			vcount[index++] = new ValueCount(val, count);


		}

		Arrays.sort(vcount, new Comparator<ValueCount>() {
			@Override
			public int compare(ValueCount o1, ValueCount o2) {
				return ((Integer) o1.count).compareTo((Integer) o2.count);
			}
		});

		int arr[] = new int[vcount.length];
		for (int i = 0; i < vcount.length; i++) {
			arr[i] = vcount[i].val;
		}

		return arr;
	}

	public int calculateValuesLeftInDomain(Cell cell) {

		// Square
		int startRow = cell.position.row / 3;
		int startCol = cell.position.col / 3;

		Set<Integer> set = new HashSet<>();
		for (int r = startRow * 3; r < startRow * 3 + 3; r++) {

			for (int c = startCol * 3; c < startCol * 3 + 3; c++) {
				int val = board[r][c].val;
				if (val == 0) {
					continue;
				}
				set.add(val);
			}
		}

		// ROW
		for (int i = 0; i < 9; i++) {
			int val = board[cell.position.row][i].val;
			if (val == 0) {
				continue;
			}
			set.add(val);
		}

		// Col
		for (int i = 0; i < 9; i++) {
			int val = board[i][cell.position.col].val;
			if (val == 0) {
				continue;
			}
			set.add(val);
		}

		int count = 0;
		for(Integer val: set) {
			if (cell.domain.contains((Integer) val)) {
				count++;
			}
		}

		return cell.domain.size() - count;
	}

	/* minimum remaining value */
	public Cell findMRVCell() {

		int minimumRemainingValues = Integer.MAX_VALUE;

		Cell c = null;

		for(Cell cellToCheck : this.unassigned) {
			if (cellToCheck.val != 0) {System.out.println("WTH "+cellToCheck); continue;}

			int values = calculateValuesLeftInDomain(cellToCheck);

			if (values < minimumRemainingValues) {
				minimumRemainingValues = values;
				c = cellToCheck;
			}
		}

		return removeFromUnassigned(c) ? c:null;
	}

	public String printBoard() {
		String str="";
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				str=str+board[row][col].val+" ";
			}
			str=str+"\n";
		}
		return str;
	}

	@Override
	public String toString() {
		String str = "";

		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				str = str + board[row][col].val + " ";
				if ((col+1) % 3 == 0) str = str + "| ";
			}
			str = str + "\n";
			if ((row+1) % 3 == 0) str = str + "------+-------+--------\n";
		}

		// TODO Auto-generated method stub
		return str;
	}

	public Cell popFromUnassigned() {
		return unassigned.remove(0);
	}

	public boolean removeFromUnassigned(Cell c) {
		return unassigned.remove(c);
	}

	public void pushBackToUnassigned(Cell cell) {
		unassigned.add(0, cell);
	}

}
