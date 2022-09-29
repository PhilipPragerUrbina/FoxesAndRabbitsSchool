package Animals;

import Field.*;

import java.awt.*;
import java.util.List;

//class to represent a human
// they hunt foxes, build houses to destroy environment
public class Human extends Animal {
    //overall stats
    private static int BREEDING_AGE = 18; //age when can breed
    public static int MAX_AGE = 100; //age until die
    private static double BREEDING_PROBABILITY = 0.06; //self-explanatory
    private static double BUILDER_PROBABILITY = 0.50; //probability to be a builder rather than hunter
    private static double STRUCTURE_PROBABILITY = 0.10; //probability to build a structure
    private static int MAX_CHILDREN = 1; //how many children can have at a time
    private static double SPEED = 2.0; //how fast can move
    private static double HUNTING_RANGE = 2.0; //how far can kill
    private static double STRUCTURE_RANGE = 3.0;//how far can place structure

    private boolean builder; //is a builder or hunter

    //create a new human at location
    public Human(boolean random_age, Vector2 location) {
        super(location); //set location
        if(random_age){setAge((int)(Math.random()*Human.MAX_AGE));} //random age
        this.builder = Math.random() < BUILDER_PROBABILITY; //set if builder
    }

    @Override
    public String getTypeName() {return "Human";}
    @Override
    public Color getColor() {return new Color(255,0,0);}//red

    @Override
    protected void performActions(Field current_field, Field next_field, List<Animal> new_animals) {
        //create babies
        for (int b = 0; b < breed(); b++) {
            Vector2 baby_location = next_field.randomNearbyLocation(location,radius*2,radius,100);
            if(baby_location == null){continue;}
            Human baby_human = new Human(false,baby_location);
            new_animals.add(baby_human);
            next_field.put(baby_human);
        }
        if(builder){ //is builder
            if(Math.random() < STRUCTURE_PROBABILITY){     //create structure
                Vector2 structure_location = next_field.randomNearbyLocation(location,STRUCTURE_RANGE,radius,100);
                if(structure_location != null){ //found position
                    Structure structure = new Structure(structure_location);
                    new_animals.add(structure);
                    next_field.put(structure);
                }
            }
            //move randomly
            setLocation(next_field.randomNearbyLocation(location,SPEED,radius,100), next_field);
        }else{     //hunter
            Vector2 new_location = null;
            Animal nearest_prey = current_field.closestAnimalOfType(location, "Fox");
            if(nearest_prey != null && nearest_prey.getLocation().distance(location) < HUNTING_RANGE){//nearest prey
                new_location = nearest_prey.getLocation();
                nearest_prey.kill(); //eat
            }
            if (new_location == null) { //no prey found
                new_location = next_field.randomNearbyLocation(location,SPEED,radius,100); // move randomly
            }
            setLocation(new_location, next_field);
        }
    }
    //set location and kill if overcrowded
    private void setLocation(Vector2 new_location, Field next_field){
        if (new_location != null) {this.location = new_location;next_field.put(this);}
        else {kill();}//overcrowding
    }

    @Override
    protected void checkDeath() {if (getAge() > MAX_AGE) {kill();} }//check age

    //get number of children to make
    private int breed() {if ( getAge() >= BREEDING_AGE && Math.random() <= BREEDING_PROBABILITY) {return (int)(Math.random()*MAX_CHILDREN) + 1;}return 0;}
}
