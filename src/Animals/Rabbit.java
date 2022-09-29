package Animals;

import Field.*;
import java.awt.*;
import java.util.List;

 //A simple model of a rabbit.
 //Rabbits age, move, breed, and die.
public class Rabbit extends Animal {
    //settings
	private static int BREEDING_AGE = 5;
    private static int MAX_AGE = 30;
    private static double BREEDING_PROBABILITY = 0.06;
    private static int MAX_LITTER_SIZE = 5;
    private static double SPEED = 2;

    //new rabbit at position
    public Rabbit(boolean startWithRandomAge, Vector2 location)
    {
        super(location); //set location
        if(startWithRandomAge) {
            setAge( (int)(Math.random()*MAX_AGE)); //random age
        }
    }
    @Override
    public String getTypeName() {
        return "Rabbit";
    }

    @Override
    public Color getColor() {return new Color(100,100,100); }//grey

    @Override
    protected void performActions(Field current_field, Field next_field, List<Animal> new_animals) {
        for(int b = 0; b < breed(); b++) {
            Vector2 baby_position = next_field.randomNearbyLocation(location,radius*2,radius,100);
            if(baby_position == null){continue;} //no position found
            Rabbit newRabbit = new Rabbit(false,baby_position); //create new rabbit
            new_animals.add(newRabbit); //add
            next_field.put(newRabbit);
        }
        //get nearest predator
        Animal nearest_fox = current_field.closestAnimalOfType(location, "Fox");
        Vector2 newLocation =  next_field.randomNearbyLocation(location,SPEED,radius,100);//random movement
        if(nearest_fox != null){ //if there are predators
            //A->B   = (B-A).normalized  = formula for going toward or away direction
            Vector2 direction = location.subtract(nearest_fox.getLocation()).normalized();// direction away from fox
            Vector2 location_in_direction = location.add(direction.multiply(new Vector2(SPEED))); //get position in that direction
            if (next_field.isEmpty(location_in_direction)) {newLocation= location_in_direction;} //is valid location
        }
        // Only transfer to the updated field if there was a free location
        if(newLocation != null) {this.location =newLocation;next_field.put(this);}
        else {kill();} //overcrowding
    }

    @Override
    protected void checkDeath() {
        if(getAge() > MAX_AGE) {kill();}//check age
    }

    // Generate a number representing the number of births,
    private int breed() {if(getAge() >= BREEDING_AGE && Math.random() <= BREEDING_PROBABILITY) {return  (int)(Math.random()*MAX_LITTER_SIZE) + 1;}return 0;}
}
