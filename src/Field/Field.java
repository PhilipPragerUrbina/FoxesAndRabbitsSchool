package Field;

import Animals.Animal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Represent a rectangular grid filled with animals of arbitrary positions using a floating point coordinate system
 * 
 * @author David J. Barnes and Michael Kolling. Modified by David Dobervich
 *         2007-2022. Modifed by Philip Prager
 */
public class Field implements Serializable {

	private static final Random rand = new Random();

	// The height and width of the field.
	private Vector2 dimensions;

	// Storage for the items on the board.
	private ArrayList<Animal> animals;


	//a field of given dimensions
	public Field(double width, double height) {
		this.dimensions.y = height;
		this.dimensions.x = width;
		animals = new ArrayList<>();

	}

	/**
	 * Empty the field.
	 */
	public void clear() {
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				board[row][col] = null;
			}
		}
	}

	/**
	 * Place an animal at the given location. If there is already an animal at
	 * the location it will be lost.
	 * 
	 * @param obj
	 *            The object (animal) to be placed.
	 * @param row
	 *            Row coordinate of the location.
	 * @param col
	 *            Column coordinate of the location.
	 */
	public void put(Animal obj, int row, int col) {
		put(obj, new Vector2(row, col));
	}
	
	/**
	 * Place an animal at the given location. If there is already an animal at
	 * the location it will be lost.
	 * 
	 * @param obj
	 *            The object (animal) to be placed.
	 * @param location
	 *            Where to place the animal.
	 */
	public void put(Animal obj, Vector2 location) {
		board[location.getRow()][location.getCol()] = obj;
	}

	/**
	 * Return the animal at the given location, if any.
	 * 
	 * @param location
	 *            Where in the field.
	 * @return The animal at the given location, or null if there is none.
	 */
	public Animal getObjectAt(Vector2 location) {
		return getObjectAt(location.getRow(), location.getCol());
	}

	/**
	 * Return the animal at the given location, if any.
	 * 
	 * @param row
	 *            The desired row.
	 * @param col
	 *            The desired column.
	 * @return The animal at the given location, or null if there is none.
	 */
	public Animal getObjectAt(int row, int col) {
		return board[row][col];
	}

	/**
	 * Generate a random location that is adjacent to the given location, or is
	 * the same location. The returned location will be within the valid bounds
	 * of the field.
	 * 
	 * @param location
	 *            The location from which to generate an adjacency.
	 * @return A valid location within the grid area. This may be the same
	 *         object as the location parameter.
	 */
	public Vector2 randomAdjacentLocation(Vector2 location) {
		int row = location.getRow();
		int col = location.getCol();
		// Generate an offset of -1, 0, or +1 for both the current row and col.
		int nextRow = row + rand.nextInt(3) - 1;
		int nextCol = col + rand.nextInt(3) - 1;
		// Check in case the new location is outside the bounds.
		if (!isLegalLocation(nextRow, nextCol)) {
			return location;
		} else if (nextRow != row || nextCol != col) {
			return new Vector2(nextRow, nextCol);
		} else {
			return location;
		}
	}
		//return the location of an adjacent type and kill if specified
	public Vector2 getAdjacentOfType(Vector2 location, String name){
		return getAdjacentOfType(location,name,false);
	}
	public Vector2 getAdjacentOfType(Vector2 location, String name, boolean kill  ) {
		List<Vector2> adjacentLocations = adjacentLocations(location);
		for (Vector2 where : adjacentLocations) {
			Animal animal = getObjectAt(where);
			if (animal != null && animal.getTypeName() == name) {
				if(animal.isAlive()){
					if(kill){animal.kill();}
					return where;
				}
			}
		}
		return null;
	}

	/**
	 * Try to find a free location that is adjacent to the given location. If
	 * there is none, then return the current location if it is free. If not,
	 * return null. The returned location will be within the valid bounds of the
	 * field.
	 * 
	 * @param location
	 *            The location from which to generate an adjacency.
	 * @return A valid location within the grid area. This may be the same
	 *         object as the location parameter, or null if all locations around
	 *         are full.
	 */
	public Vector2 freeAdjacentLocation(Vector2 location) {
		List<Vector2> adjacent = adjacentLocations(location);
		for (Vector2 next : adjacent) {
			if (board[next.getRow()][next.getCol()] == null) {
				return next;
			}
		}
		// check whether current location is free
		if (board[location.getRow()][location.getCol()] == null) {
			return location;
		} else {
			return null;
		}
	}

	public Vector2 freeAdjacentLocation(int row, int col) {
		return freeAdjacentLocation(new Vector2(row, col));
	}

	/**
	 * Generate an iterator over a shuffled list of locations adjacent to the
	 * given one. The list will not include the location itself. All locations
	 * will lie within the grid.
	 * 
	 * @param location
	 *            The location from which to generate adjacencies.
	 * @return An iterator over locations adjacent to that given.
	 */
	public List<Vector2> adjacentLocations(Vector2 location) {
		int row = location.getRow();
		int col = location.getCol();
		List<Vector2> locations = new LinkedList<Vector2>();
		for (int roffset = -1; roffset <= 1; roffset++) {
			int nextRow = row + roffset;
			if (nextRow >= 0 && nextRow < height) {
				for (int coffset = -1; coffset <= 1; coffset++) {
					int nextCol = col + coffset;
					// Exclude invalid locations and the original location.
					if (nextCol >= 0 && nextCol < width
							&& (roffset != 0 || coffset != 0)) {
						locations.add(new Vector2(nextRow, nextCol));
					}
				}
			}
		}
		Collections.shuffle(locations, rand);
		return locations;
	}

	public List<Vector2> adjacentLocations(int row, int col) {
		return adjacentLocations(new Vector2(row, col));
	}

	/**
	 * Return the depth of the field.
	 * 
	 * @return The depth of the field.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Return the width of the field.
	 * 
	 * @return The width of the field.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Determines how moving in the given direction affects the row number.
	 * 
	 * @param direction
	 *            the direction in which to move
	 * @return the amount by which the row number will change
	 */
	static int rowChange(int direction) {
		int change = 0;
		switch (direction) {
		case N:
		case NE:
		case NW:
			change = -1;
			break;
		case S:
		case SE:
		case SW:
			change = +1;
			break;
		}
		return change;
	}

	/**
	 * Determines how moving in the given direction affects the column number.
	 * 
	 * @param direction
	 *            the direction in which to move
	 * @return the amount by which the column number will change
	 */
	static int columnChange(int direction) {
		int change = 0;
		switch (direction) {
		case W:
		case NW:
		case SW:
			change = -1;
			break;
		case E:
		case NE:
		case SE:
			change = +1;
		}
		return change;
	}

	/**
	 * Determines whether the given row and column numbers represent a legal
	 * location in the field.
	 * 
	 * @param row
	 *            the row number
	 * @param col
	 *            the column number
	 */
	public boolean isLegalLocation(int row, int col) {
		return ((row >= 0) && (row < getHeight()) &&
				(col >= 0) && (col < getWidth()));
	}

	public boolean isLegalLocation(Vector2 l) {
		return isLegalLocation(l.getRow(), l.getCol());
	}

	public boolean isEmpty(int row, int col) {
		return this.board[row][col] == null;
	}

	public boolean isEmpty(Vector2 l) {
		return isEmpty(l.getRow(), l.getCol());
	}





}