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

    public Human(boolean random_age, Vector2 location) {
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

            Vector2 baby_location = next_field.randomNearbyLocation(location,3.0,radius,100);
            if(baby_location == null){
                continue;
            }
            Human baby_human = new Human(false,baby_location);
            new_animals.add(baby_human);
            next_field.put(baby_human);
        }
        if(builder){
            //create structure
            if(Math.random() < STRUCTURE_PROBABILITY){
                Vector2 structure_location = next_field.randomNearbyLocation(location,3.0,radius,100);


             //   if(current_field.closestAnimalOfType(location, "Structure") != null && current_field.closestAnimalOfType(location, "Structure").getLocation().distance(location) > 2.0 ){
                if(structure_location != null){
                    Structure structure = new Structure(structure_location);
                    new_animals.add(structure);
                    next_field.put(structure);
                }

               // }
            }
            //move randomly
            setLocation(next_field.randomNearbyLocation(location,3.0,radius,100), next_field);

        }else{
            //hunter
            Animal hunted = current_field.closestAnimalOfType(location, "Fox");
            Vector2 new_location = null;
            if(hunted != null && hunted.getLocation().distance(location) < 3.0){
                new_location = hunted.getLocation();
                hunted.kill();
            }
            if (new_location == null) {
                new_location = next_field.randomNearbyLocation(location,3.0,radius,100);; //otherwise, move randomly
                if(new_location == null && ENTER_BUILDINGS){
                   // new_location = current_field.getAdjacentOfType(location, "Structure"); //live in building
                }
            }
            setLocation(new_location, next_field);
        }
    }







    //set location and kill if overcrowded
    private void setLocation(Vector2 new_location, Field next_field){
        if (new_location != null) {
            this.location = new_location;
            next_field.put(this);
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
