package Animals;

import Field.*;
import java.awt.*;
import java.util.List;


 //A simple model of a fox. Foxes age, move, chase rabbits, and die.
public class Fox extends Animal{
	//settings
	private static int BREEDING_AGE = 3;  //age at which can breed
	private static int MAX_AGE = 50;  //age at which it dies
	private static double BREEDING_PROBABILITY = 0.15; //how likely to breed
	private static int MAX_LITTER_SIZE = 6;   //max number of children at a time
	private static int RABBIT_FOOD_VALUE = 10; //how much food a rabbit gives
	private static double EATING_RANGE = 2; //How far a fox can reach
	 private static double SPEED = 2; //how fast a fox is

	// The fox's food level, which is increased by eating rabbits.
	private int foodLevel;


	//create a new fox at a location, and if it should have a random age
	public Fox(boolean startWithRandomAge, Vector2 location) {
		super(location); //set location
		if (startWithRandomAge) { //random start
			setAge((int)(Math.random()*MAX_AGE));
			foodLevel = (int)(Math.random()*RABBIT_FOOD_VALUE);
		} else {
			foodLevel = RABBIT_FOOD_VALUE; //basic start
		}
	}
	@Override
	public Color getColor() {
		return new Color(155,100,0); //orange
	}
	@Override
	public String getTypeName() {return "Fox";}

	@Override
	protected void performActions(Field current_field, Field updated_field, List<Animal> new_animals) {
			// New foxes are born into nearby locations.
			for (int b = 0; b <  breed(); b++) {
				Vector2 baby_location= updated_field.randomNearbyLocation(location,radius*2,radius,100); //random location
				if(baby_location == null){continue;} //no location found
				Fox newFox = new Fox(true,baby_location);//create new fox
				newFox.setFoodLevel(this.foodLevel); //set food
				new_animals.add(newFox); //add
				updated_field.put(newFox);
			}
			//location to move to
			Vector2 new_location = null;


			Animal closest_prey = current_field.closestAnimalOfType( location, "Rabbit"); //find the closest food
			if(closest_prey != null && closest_prey.getLocation().distance(location) < EATING_RANGE){ //if food is in range
				foodLevel = RABBIT_FOOD_VALUE; //get nutritional value
				closest_prey.kill(); //kill animal
				new_location = closest_prey.getLocation(); //go to their location
			}
			if (new_location == null) { // no food in range
				new_location = updated_field.randomNearbyLocation(location,SPEED,radius,100); //random direction
				if(closest_prey != null){ //if rabbits exist
					Vector2 direction = closest_prey.getLocation().subtract(location).normalized(); //get direction
					Vector2 location_in_direction = location.add(direction.multiply(new Vector2(SPEED))); //move toward rabbit
					if (updated_field.isEmpty(location_in_direction,radius)) {new_location= location_in_direction;} //check if location is free
				}
			}
			if (new_location != null) {this.location = new_location;updated_field.put(this);} //set location
			else {kill();}//overcrowding
	}

	@Override
	protected void checkDeath() {
		if (getAge() > MAX_AGE) { //increment age
			kill();
		}
		//increment hunger
		foodLevel--;
		if (foodLevel <= 0) {
			kill();
		}
	}

	//Generate a number representing the number of births, if it can breed.
	private int breed() {
		if (getAge() >= BREEDING_AGE && Math.random() <= BREEDING_PROBABILITY) {return  (int)(Math.random()*MAX_LITTER_SIZE) + 1;}
		return 0;
	}
	//set the food level
	public void setFoodLevel(int fl) {this.foodLevel = fl;}
}
