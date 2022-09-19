package Animals;

import Field.*;
import java.util.List;

//class to represent a human
// they hunt foxes, build houses to destroy environment

public class Human {


    //overall stats
    private static int BREEDING_AGE = 18;
    public static int MAX_AGE = 100;
    private static double BREEDING_PROBABILITY = 0.12;
    private static double BUILDER_PROBABILITY = 0.50;
    private static double STRUCTURE_PROBABILITY = 0.20;
    private static int MAX_CHILDREN = 2;
    private static int WORK_AGE = 12; //age at which human can do stuff
    private static boolean CRAMPED_STRUCTURES = true; //can build adjacent structures
    private static boolean ENTER_BUILDINGS = false; //hunters can live in building

    //individual stats
    private int age;
    private boolean alive;
    private Location location;
    private boolean builder; //is a builder or hunter

    public Human(int age, Location location) {
        this.age = age;
        this.location = location;
        this.alive = true;
        this.builder = Math.random() < BUILDER_PROBABILITY;
    }

    //step the simulation
    public void step(Field current_field, Field next_field, List<Human> babies, List<Structure> structures) {
        incrementAge();
        if (alive) {
            //create babies
            for (int b = 0; b < breed(); b++) {

                Location baby_location = next_field.randomAdjacentLocation(location);
                Human baby_human = new Human(0,baby_location);
                babies.add(baby_human);
                next_field.put(baby_human ,baby_location);
            }
            if(builder){
                //create structure
                if(Math.random() < STRUCTURE_PROBABILITY){
                    Location structure_location = next_field.randomAdjacentLocation(location);
                    if(adjacentStructure(current_field, structure_location)==null || CRAMPED_STRUCTURES){
                        Structure structure = new Structure(structure_location);
                        structures.add(structure);
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
    }


    //get adjacent fox
    private Location findFood(Field field, Location location) {
        List<Location> adjacentLocations = field.adjacentLocations(location);
        for (Location where : adjacentLocations) {
            Object animal = field.getObjectAt(where);
            if (animal instanceof Fox) {
                Fox fox = (Fox) animal;
                if (fox.isAlive()) {
                    fox.setEaten();
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
            alive = false; //overcrowding
        }
    }

    //increment age and kill if too old
    private void incrementAge() {
        age++;
        if (age > MAX_AGE) {
            alive = false;
        }
    }
    //get number of children to make
    private int breed() {
        int numBirths = 0;
        if ( age >= BREEDING_AGE && Math.random() <= BREEDING_PROBABILITY) {
            numBirths = (int)(Math.random()*MAX_CHILDREN) + 1;
        }
        return numBirths;
    }
    //get if alive
    public boolean isAlive() {
        return alive;
    }

}
