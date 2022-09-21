package Animals;

import Animals.*;
import Field.*;
import Graph.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple model of a fox. Foxes age, move, eat rabbits, and die.
 * 
 * @author David J. Barnes and Michael Kolling.  Modified by David Dobervich 2007-2022
 */
public class Fox extends Animal{
	// ----------------------------------------------------
	// Characteristics shared by all foxes (static fields).
	// ----------------------------------------------------
	private static int BREEDING_AGE = 3;
	// The age to which a fox can live.
	private static int MAX_AGE = 50;
	// The likelihood of a fox breeding.
	private static double BREEDING_PROBABILITY = 0.15;
	// The maximum number of births.
	private static int MAX_LITTER_SIZE = 6;
	// The food value of a single rabbit. In effect, this is the
	// number of steps a fox can go before it has to eat again.
	private static int RABBIT_FOOD_VALUE = 6;
	// A shared random number generator to control breeding.

	// -----------------------------------------------------
	// Individual characteristics (attributes).
	// -----------------------------------------------------

	// The fox's food level, which is increased by eating rabbits.
	private int foodLevel;

	/**
	 * Create a fox. A fox can be created as a new born (age zero and not
	 * hungry) or with random age.
	 * 
	 * @param startWithRandomAge
	 *            If true, the fox will have random age and hunger level.
	 */
	public Fox(boolean startWithRandomAge, Location location) {
		super( location);
		if (startWithRandomAge) {
			setAge((int)(Math.random()*MAX_AGE));
			foodLevel = (int)(Math.random()*RABBIT_FOOD_VALUE);
		} else {
			foodLevel = RABBIT_FOOD_VALUE;
		}
	}



	/**
	 * This is what the fox does most of the time: it hunts for rabbits. In the
	 * process, it might breed, die of hunger, or die of old age.
	 * 
	 * @param current_field
	 *            The field currently occupied.
	 * @param updated_field
	 *            The field to transfer to.
	 * @param new_animals
	 *            A list to add newly born foxes to.
	 */


	@Override
	protected void performActions(Field current_field, Field updated_field, List<Animal> new_animals) {
			// New foxes are born into adjacent locations.
			int births = breed();
			for (int b = 0; b < births; b++) {
				Location loc= updated_field.randomAdjacentLocation(location);
				Fox newFox = new Fox(true,loc);
				newFox.setFoodLevel(this.foodLevel);
				new_animals.add(newFox);
				updated_field.put(newFox, loc);
			}
			// Move towards the source of food if found.
			Location newLocation = findFood(current_field, location);
			if (newLocation == null) { // no food found - move randomly
				newLocation = updated_field.freeAdjacentLocation(location);
			}
			if (newLocation != null) {
				setLocation(newLocation);
				updated_field.put(this, newLocation);
			} else {
				// can neither move nor stay - overcrowding - all locations
				// taken
				kill();
			}
	}

	@Override
	protected void checkDeath() {
		if (getAge() > MAX_AGE) {
			kill();
		}
		incrementHunger();
	}

	/**
	 * Make this fox more hungry. This could result in the fox's death.
	 */
	private void incrementHunger() {
		foodLevel--;
		if (foodLevel <= 0) {
			kill();
		}
	}

	/**
	 * Tell the fox to look for rabbits adjacent to its current location. Only
	 * the first live rabbit is eaten.
	 * 
	 * @param field
	 *            The field in which it must look.
	 * @param location
	 *            Where in the field it is located.
	 * @return Where food was found, or null if it wasn't.
	 */
	private Location findFood(Field field, Location location) {
		List<Location> adjacentLocations = field.adjacentLocations(location);

		for (Location where : adjacentLocations) {
			Object animal = field.getObjectAt(where);
			if (animal instanceof Rabbit) {
				Rabbit rabbit = (Rabbit) animal;
				if (rabbit.isAlive()) {
					rabbit.kill();
					foodLevel = RABBIT_FOOD_VALUE;
					return where;
				}
			}
		}

		return null;
	}

	/**
	 * Generate a number representing the number of births, if it can breed.
	 * 
	 * @return The number of births (may be zero).
	 */
	private int breed() {
		int numBirths = 0;
		if (canBreed() && Math.random() <= BREEDING_PROBABILITY) {
			numBirths = (int)(Math.random()*MAX_LITTER_SIZE) + 1;
		}
		return numBirths;
	}

	/**
	 * A fox can breed if it has reached the breeding age.
	 */
	private boolean canBreed() {
		return getAge() >= BREEDING_AGE;
	}

	@Override
	public String getTypeName() {
		return "Fox";
	}

	/**
	 * Set the animal's location.
	 * 
	 * @param row
	 *            The vertical coordinate of the location.
	 * @param col
	 *            The horizontal coordinate of the location.
	 */
	public void setLocation(int row, int col) {
		this.location = new Location(row, col);
	}

	/**
	 * Set the fox's location.
	 * 
	 * @param location
	 *            The fox's location.
	 */

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setFoodLevel(int fl) {
		this.foodLevel = fl;
	}
}
