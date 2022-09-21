package Animals;

import Field.*;

import java.awt.*;
import java.util.List;

//class to represent a human
// they hunt foxes, build houses to destroy environment

public class Human extends Animal {


    //overall stats
    private static int BREEDING_AGE = 18;
    public static int MAX_AGE = 100;
    private static double BREEDING_PROBABILITY = 0.06;
    private static double BUILDER_PROBABILITY = 0.50;
    private static double STRUCTURE_PROBABILITY = 0.10;
    private static int MAX_CHILDREN = 1;
    private static boolean CRAMPED_STRUCTURES = false; //can build adjacent structures
    private static boolean ENTER_BUILDINGS = false; //hunters can live in building

    //individual stats
    private boolean builder; //is a builder or hunter

    public Human(boolean random_age, Location location) {
        super(location);
        if(random_age){
            setAge((int)(Math.random()*Human.MAX_AGE));
        }
        this.builder = Math.random() < BUILDER_PROBABILITY;
    }

    @Override
    public String getTypeName() {
        return "Human";
    }

    @Override
    public Color getColor() {
        return new Color(255,0,0); //red
    }

    @Override
    protected void performActions(Field current_field, Field next_field, List<Animal> new_animals) {
        //create babies
        for (int b = 0; b < breed(); b++) {

            Location baby_location = next_field.randomAdjacentLocation(location);
            Human baby_human = new Human(false,baby_location);
            new_animals.add(baby_human);
            next_field.put(baby_human ,baby_location);
        }
        if(builder){
            //create structure
            if(Math.random() < STRUCTURE_PROBABILITY){
                Location structure_location = next_field.randomAdjacentLocation(location);
                if(adjacentStructure(current_field, structure_location)==null || CRAMPED_STRUCTURES){
                    Structure structure = new Structure(structure_location);
                    new_animals.add(structure);
                    next_field.put(structure ,structure_location);
                }
            }
            //move randomly
            setLocation(next_field.freeAdjacentLocation(location), next_field);

        }else{
            //hunter
            Location new_location = findFood(current_field, location); //find fox
            if (new_location == null) {
                new_location = next_field.freeAdjacentLocation(location); //otherwise, move randomly
                if(new_location == null && ENTER_BUILDINGS){
                    new_location = adjacentStructure(current_field,location); //live in building
                }
            }
            setLocation(new_location, next_field);
        }
    }



    //get adjacent fox
    private Location findFood(Field field, Location location) {
        List<Location> adjacentLocations = field.adjacentLocations(location);
        for (Location where : adjacentLocations) {
            Object animal = field.getObjectAt(where);
            if (animal instanceof Fox) {
                Fox fox = (Fox) animal;
                if (fox.isAlive()) {
                    fox.kill();
                    return where;
                }
            }
        }
        return null;
    }

    //is structure adjacent
    private Location adjacentStructure(Field field, Location location) {
        List<Location> adjacentLocations = field.adjacentLocations(location);
        for (Location where : adjacentLocations) {
            Object animal = field.getObjectAt(where);
            if (animal instanceof Structure) {
                return where;
            }
        }
        return null;
    }

    //set location and kill if overcrowded
    private void setLocation(Location new_location, Field next_field){
        if (new_location != null) {
            this.location = new_location;
            next_field.put(this, new_location);
        } else {
            kill(); //overcrowding
        }
    }

    @Override
    protected void checkDeath() {
        if (getAge() > MAX_AGE) {
             kill();
        }
    }


    //get number of children to make
    private int breed() {
        int numBirths = 0;
        if ( getAge() >= BREEDING_AGE && Math.random() <= BREEDING_PROBABILITY) {
            numBirths = (int)(Math.random()*MAX_CHILDREN) + 1;
        }
        return numBirths;
    }


}
