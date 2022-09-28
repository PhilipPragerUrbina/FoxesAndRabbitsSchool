package Field;

import Animals.Animal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Represent a rectangular grid filled with animals of arbitrary positions using a floating point coordinate system
 * 
 * @author David J. Barnes and Michael Kolling. Modified by David Dobervich
 *         2007-2022. Modified by Philip Prager
 */
public class Field implements Serializable {

	private static final Random rand = new Random();

	// The height and width of the field.
	private Vector2 dimensions;
	//get dimensions
	public double getHeight() {
		return dimensions.y;
	}
	public double getWidth() {
		return dimensions.x;
	}

	// Storage for the items on the board.
	private ArrayList<Animal> animals;

	public ArrayList<Animal> getAnimals(){
		return animals; //get animals for drawing
	}

	//a field of given dimensions
	public Field(double width, double height) {
		this.dimensions = new Vector2(width,height);
		animals = new ArrayList<>();

	}

	//empty the field
	public void clear() {
		animals.clear();
	}

	//add animal
	public void put(Animal obj) {animals.add(obj);}
	public void remove(Animal obj){animals.remove(obj);}

	//get the nearest object to location. Return null if there are no animals.
	public Animal closestAnimal(Vector2 location) {
		if(animals.isEmpty()){return null;} //check if there are none
		double closest_distance = animals.get(0).getLocation().distance(location); //first animal be default
		Animal closest_animal = animals.get(0);
		for (Animal a : animals) {
			double distance = a.getLocation().distance(location);
			if(distance < closest_distance){
				closest_distance = distance;
				closest_animal = a;
			}
		}
		return closest_animal;
	}
	//get the nearest animal of a certain type. Return null if none
	public Animal closestAnimalOfType(Vector2 location, String type) {
		double closest_distance = Double.MAX_VALUE;
		Animal closest_animal = null;
		for (Animal a : animals) {
			if(a.getTypeName().equals(type)){
				double distance = a.getLocation().distance(location);
				if(distance < closest_distance){
					closest_distance = distance;
					closest_animal = a;
				}
			}
		}
		return closest_animal;
	}

	//get a random free nearby location, return null if none
	// specify a radius of where to search and how often to try until fail
	public Vector2 randomNearbyLocation(Vector2 location, double spawn_radius, int tries) {
		for (int i = 0; i < tries; i++) {
			double random_offset_x = (rand.nextDouble() - 0.5) * spawn_radius;
			double random_offset_y = (rand.nextDouble() - 0.5) * spawn_radius;
			Vector2 potential_location = new Vector2(location.x + random_offset_x, location.y + random_offset_y);
			if(isLegalLocation(potential_location)){//is on field
				//check for collisions
				if(isEmpty(potential_location)){
					return potential_location;
				}
			}
		}
		return null;
	}

	//same as other one, but checks for free spot rather than point
	public Vector2 randomNearbyLocation(Vector2 location, double spawn_radius, double free_radius, int tries) {
		for (int i = 0; i < tries; i++) {
			double random_offset_x = (rand.nextDouble() - 0.5) * spawn_radius;
			double random_offset_y = (rand.nextDouble() - 0.5) * spawn_radius;
			Vector2 potential_location = new Vector2(location.x + random_offset_x, location.y + random_offset_y);
			if(isLegalLocation(potential_location)){//is on field
				//check for collisions
				if(isEmpty(potential_location,free_radius)){
					return potential_location;
				}
			}
		}
		return null;
	}

	//is a location within field
	public boolean isLegalLocation(Vector2 l) {
		return ((l.x > 0) && (l.y < getHeight()) &&
				(l.y > 0) && (l.x < getWidth()));
	}

	//is point free
	public boolean isEmpty(Vector2 location) {
		if(!isLegalLocation(location)){return false;}; //check if even in field
		for (Animal animal : animals) { //loop through all animals
			if(animal.getLocation().distance(location) < animal.getRadius()){ //if within other animal
				return false;
			}
		}
		return true;
	}

	// is spot(circle) free
	public boolean isEmpty(Vector2 location, double radius) {
		if(!isLegalLocation(location)){return false;}; //check if even in field
		for (Animal animal : animals) { //loop through all animals
			if(animal.getLocation() == null){
				System.err.println("Someone did not find a position!");
			}
			double distance =animal.getLocation().distance(location);
			double other_radius = animal.getRadius();
			if(distance < radius + other_radius){
				return false; //check if 2 circles intersect
			}
		}
		return true;
	}




}