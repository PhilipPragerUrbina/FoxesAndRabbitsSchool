package Animals;

import Field.*;

import java.awt.*;
import java.util.List;

//superclass for animals
public abstract class Animal {
    private boolean is_alive = true; //should be kept in simulation
    private int age; //how long has been alive
    protected Vector2 location; //where is it on the field


    public Animal(Vector2 location){
        this.location = location;
        this.age =0;
    }


    //step the simulation
    public void step(Field current_field, Field next_field, List<Animal> new_animals){
        age++;
        checkDeath();
        if(is_alive){
            performActions(current_field,next_field,new_animals);
        }
    };

    //abstract methods
    protected abstract void performActions(Field current_field, Field next_field, List<Animal> new_animals); //perform actions every step

    protected abstract void checkDeath(); //kill animal if needed

    public abstract String getTypeName(); //get the name of the animal

    public abstract Color getColor();

    //getters and setters
    protected int getAge(){
        return age;
    }
    protected void setAge(int age){this.age = age;}
    public boolean isAlive(){
        return is_alive;
    }
    public void kill(){
        is_alive = false;
    }

}
