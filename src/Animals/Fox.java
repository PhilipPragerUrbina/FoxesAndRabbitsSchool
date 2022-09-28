package Animals;

import Field.*;

import java.awt.*;
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
	private static int RABBIT_FOOD_VALUE = 10;
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
	public Fox(boolean startWithRandomAge, Vector2 location) {
		super( location);
		if (startWithRandomAge) {
			setAge((int)(Math.random()*MAX_AGE));
			foodLevel = (int)(Math.random()*RABBIT_FOOD_VALUE);
		} else {
			foodLevel = RABBIT_FOOD_VALUE;
		}
	}

	@Override
	public Color getColor() {
		return new Color(155,100,0); //orange
	}




	@Override
	protected void performActions(Field current_field, Field updated_field, List<Animal> new_animals) {
			// New foxes are born into adjacent locations.
			int births = breed();
			for (int b = 0; b < births; b++) {
				Vector2 loc= updated_field.randomNearbyLocation(location,3.0,radius,100);
				if(loc == null){
					continue;
				}
				Fox newFox = new Fox(true,loc);
				newFox.setFoodLevel(this.foodLevel);
				new_animals.add(newFox);
				updated_field.put(newFox);
			}
			// Move towards the source of food if found.
			Animal closest_prey = current_field.closestAnimalOfType( location, "Rabbit"); //find food

		Vector2 newLocation = null; //is close
		//todo add these constants
		//todo create an easy distance function in field
		if(closest_prey != null && closest_prey.getLocation().distance(location) < 2){//food range
				foodLevel = RABBIT_FOOD_VALUE; //eat
				closest_prey.kill();
				newLocation = closest_prey.getLocation();
			}

			if (newLocation == null) { // no food found - move randomly
				newLocation = updated_field.randomNearbyLocation(location,2.0,radius,100);
				Animal nearest_rabbite = current_field.closestAnimalOfType(location, "Rabbit");
//todo clean up and comment
				if(nearest_rabbite != null){
					Vector2 location_in_direction = nearest_rabbite.getLocation().subtract(location).normalized();
					location_in_direction = location.add(location_in_direction.multiply(new Vector2(2.0)));//random speed todo
					if (updated_field.isEmpty(location_in_direction)) {
						newLocation= location_in_direction;
					}
				}
			}
			if (newLocation != null) {
				setLocation(newLocation);
				updated_field.put(this);
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
		this.location = new Vector2(row, col);
	}

	/**
	 * Set the fox's location.
	 * 
	 * @param location
	 *            The fox's location.
	 */

	public void setLocation(Vector2 location) {
		this.location = location;
	}

	public void setFoodLevel(int fl) {
		this.foodLevel = fl;
	}
}
