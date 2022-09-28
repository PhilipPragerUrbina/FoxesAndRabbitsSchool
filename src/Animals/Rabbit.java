package Animals;

import Field.*;

import java.awt.*;
import java.util.List;

/**
 * A simple model of a rabbit.
 * Rabbits age, move, breed, and die.
 * 
 * @author David J. Barnes and Michael Kolling.  Modified by David Dobervich 2007-2022
 */
public class Rabbit extends Animal {
    // ----------------------------------------------------
    // Characteristics shared by all rabbits (static fields).
    // ----------------------------------------------------
	private static int BREEDING_AGE = 5;
	
    // The age to which all rabbits can live.
    private static int MAX_AGE = 30;
    
    // The likelihood of a rabbit breeding.
    private static double BREEDING_PROBABILITY = 0.06;
    
    // The maximum number of births.
    private static int MAX_LITTER_SIZE = 5;



    /**
     * Create a new rabbit. A rabbit may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param startWithRandomAge If true, the rabbit will have a random age.
     */
    public Rabbit(boolean startWithRandomAge, Vector2 location)
    {
        super(location);
        if(startWithRandomAge) {
            setAge( (int)(Math.random()*MAX_AGE));
        }
    }

    @Override
    public String getTypeName() {
        return "Rabbit";
    }

    @Override
    public Color getColor() {
        return new Color(100,100,100); //gray
    }

    @Override
    protected void performActions(Field current_field, Field next_field, List<Animal> new_animals) {
        int births = breed();
        for(int b = 0; b < births; b++) {
            Vector2 loc = next_field.randomNearbyLocation(location,3.0,radius,100);
            if(loc == null){
                continue;
            }
            Rabbit newRabbit = new Rabbit(false,loc);
            newRabbit.setLocation(loc);
            new_animals.add(newRabbit);
            next_field.put(newRabbit);
        }
        Animal nearest_fox = current_field.closestAnimalOfType(location, "Fox");

        Vector2 newLocation =  next_field.randomNearbyLocation(location,2.0,radius,100);
        if(nearest_fox != null){
            //A->B   = (B-A).normalized
            //formula for going toward or away direction
            Vector2 location_in_direction = location.subtract(nearest_fox.getLocation()).normalized();//away from fox
            location_in_direction = location.add(location_in_direction.multiply(new Vector2(2.0)));//speed
            if (next_field.isEmpty(location_in_direction)) {
                newLocation= location_in_direction;
            }
        }




        // Only transfer to the updated field if there was a free location
        if(newLocation != null) {
            setLocation(newLocation);
            next_field.put(this);
        }
        else {
           kill();
        }
    }


    @Override
    protected void checkDeath() {
        if(getAge() > MAX_AGE) {
           kill();
        }
    }

    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @return The number of births (may be zero).
     */
    private int breed()
    {
        int births = 0;
        if(canBreed() && Math.random() <= BREEDING_PROBABILITY) {
            births = (int)(Math.random()*MAX_LITTER_SIZE) + 1;
        }
        return births;
    }

    /**
     * A rabbit can breed if it has reached the breeding age.
     * @return true if the rabbit can breed, false otherwise.
     */
    private boolean canBreed()
    {
        return getAge() >= BREEDING_AGE;
    }
    

    
    /**
     * Set the animal's location.
     * @param row The vertical coordinate of the location.
     * @param col The horizontal coordinate of the location.
     */
    public void setLocation(int row, int col)
    {
        this.location = new Vector2(row, col);
    }

    /**
     * Set the rabbit's location.
     * @param location The rabbit's location.
     */
    public void setLocation(Vector2 location)
    {
        this.location = location;
    }
}
