package Simulator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

import Animals.*;
import Field.*;
import Graph.*;
import processing.core.PApplet;

/**
 * A simple predator-prey simulator, based on a field containing rabbitList and
 * foxList.
 *
 * @author David J. Barnes and Michael Kolling. Modified by David Dobervich and Daniel Hutzley
 * 2007-2022
 */
public class Simulator {
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 80;

    // The default height of the grid.
    private static final int DEFAULT_HEIGHT = 80;

    // How many pixels to leave as a buffer between rendered elements
    private static final int VIEW_EDGE_BUFFER = 20;


    // Lists of animals in the field. Separate lists are kept for ease of
    // iteration.
    private ArrayList<Animal> animal_list;

    //all animal classes that should be used
    private ArrayList<Class<? extends Animal>> animals_to_add;
    private ArrayList<Double> spawn_probabilities;

    // The current state of the field.
    private Field field;

    // A second field, used to build the next stage of the simulation.
    private Field updatedField;

    // The current step of the simulation.
    private int step;

    // A graphical view of the simulation.
    private FieldDisplay view;

    // A graph of animal populations over time
    private Graph graph;

    // Processing Applet (the graphics window we draw to)
    private PApplet graphicsWindow;

    // Object to keep track of statistics of animal populations
    private FieldStats stats;

    private int max_starting_animals; //max number of animals to be spawned at the beginning

    /**
     * Construct a simulation field with default size.
     */
    public Simulator() {
        this(DEFAULT_HEIGHT, DEFAULT_WIDTH,DEFAULT_WIDTH*DEFAULT_HEIGHT);
    }

    //add animal class to simulation
    public void addAnimal(Class<? extends Animal> type, double spawn_prob){
        spawn_probabilities.add(spawn_prob);
        animals_to_add.add(type);
    }


    /**
     * Create a simulation field with the given size.
     *
     * @param height Height of the field. Must be greater than zero.
     * @param width  Width of the field. Must be greater than zero.
     * @param max_starting_animals Limit animals spawned at start for performance reasons
     */
    public Simulator(int width, int height, int max_starting_animals) {
        if (width <= 0 || height <= 0) {
            System.out.println("The dimensions must be greater than zero.");
            System.out.println("Using default values.");
            height = DEFAULT_HEIGHT;
            width = DEFAULT_WIDTH;
        }
        this.max_starting_animals = max_starting_animals; //max animals for performance reasons

        animal_list = new ArrayList<Animal>();
        field = new Field(width, height);
        updatedField = new Field(width, height);
        stats = new FieldStats();
        spawn_probabilities= new ArrayList<>();
        animals_to_add = new ArrayList<>();


    }

    //do after animals classes have been added
   public void populate(){
        reset();   // Setup a valid starting point.
    }

    public void setGUI(PApplet p) {
        this.graphicsWindow = p;
        // Create a view of the state of each location in the field.
        view = new FieldDisplay(p, this.field, VIEW_EDGE_BUFFER, VIEW_EDGE_BUFFER, p.width - 2*VIEW_EDGE_BUFFER, p.height / 2 - 2 * VIEW_EDGE_BUFFER);
        graph = new Graph(p, view.getLeftEdge(), view.getBottomEdge()+VIEW_EDGE_BUFFER, view.getRightEdge(), p.height-VIEW_EDGE_BUFFER, 0, 0, 500, 300);

        for (Animal animal : animal_list) {
            graph.setColor(animal.getClass(), animal.getColor().hashCode()); //set graph colors
        }
        graph.title = "";
        for (Class<? extends Animal> c : animals_to_add) {
            graph.title += c.getName() + " , ";
        }
        graph.xlabel = "Time";
        graph.ylabel = "Pop.\t\t";
    }

    /**
     * Run the simulation from its current state for a reasonably long period,
     * e.g. 500 steps.
     */
    public void runLongSimulation() {simulate(500);}

    /**
     * Run the simulation from its current state for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     *
     * @param numSteps The number of steps to run for.
     */
    public void simulate(int numSteps) {
        for (int step = 1; step <= numSteps && isViable(); step++) {simulateOneStep();}
    }

    /**
     * Run the simulation from its current state for a single step. Iterate over
     * the whole field updating the state of each fox and rabbit.
     */
    public void simulateOneStep() {
        step++;
        ArrayList<Animal> new_animals = new ArrayList<>();
        // Loop through Humans; let each live around.
        for (int i = 0; i < animal_list.size(); i++) {
            Animal animal = animal_list.get(i);
            animal.step(field, updatedField, new_animals);
            if (!animal.isAlive()) {
                animal_list.remove(i);
                i--;
            }
        }
        animal_list.addAll(new_animals);

        // Swap the field and updatedField at the end of the step.
        Field temp = field;
        field = updatedField;
        updatedField = temp;
        updatedField.clear();
        stats.generateCounts(field);
        updateGraph();
    }

    public void updateGraph() {
        Counter count;
        for (Counter c : stats.getCounts()) {
            graph.plotPoint(step, c.getCount(), c.getClassName());
        }
    }

    /**
     * Reset the simulation to a starting position.
     */
    public void reset() {
        step = 0;
        animal_list.clear();
        field.clear();
        updatedField.clear();
        try {
            initializeBoard(field);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (graph != null) {
            graph.clear();
            graph.setDataRanges(0, 500, 0, 300);
        }
    }

    /**
     * Populate a field with foxList and rabbitList.
     *
     * @param field The field to be populated.
     */
    private void initializeBoard(Field field) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Random rand = new Random(); //random generator
        field.clear(); //start from scratch
        for (int i = 0; i < max_starting_animals; i++) { //for each animal that can be added
            Class<? extends Animal> c = randomAnimal(rand,0); //get random animal type
            if(c != null){ //if animal should be added
                Constructor<? extends Animal> con =  c.getConstructor(boolean.class, Vector2.class);//get animal class
                //get random free location within field
                Vector2 location = null;
                for (int j = 0; j < max_starting_animals; j++) {//max tries until fail
                    Vector2 random_location = new Vector2(Math.random() * field.getWidth(), Math.random() * field.getHeight());
                    if(field.isEmpty(random_location)){//if valid location
                        location = random_location;
                        break;
                    };
                }
                if(location != null) { //if free location was found
                    Animal new_animal = (Animal) con.newInstance(true, location); //create instance
                    animal_list.add(new_animal);
                    field.put(new_animal);
                }
            }
        }
        for (int row = 0; row < field.getHeight(); row++) { //go through positions in field
            for (int col = 0; col < field.getWidth(); col++) {

                if(animal_list.size() > max_starting_animals){
                    return; //stop adding animals for performance reasons
                }

            }
        }
    }

    //start the following recursive function properly
    private Class<? extends Animal> randomAnimal(Random rand) {
    return randomAnimal(rand,0);//threshold should start at 0
    }

    //get a random animal type recursively
    //very computationally expensive, but you dont have a lot of animal classes, and it tries the probabilities in increasing order as it should be
    private Class<? extends Animal> randomAnimal(Random rand, double threshold) {
        double minimum_p = Double.MAX_VALUE; //smallest probability so far
        Class<? extends Animal> final_c = null; //associated class
        for (int i = 0; i < animals_to_add.size(); i++) { //for each
            Class<? extends  Animal> c = animals_to_add.get(i);
            double p = spawn_probabilities.get(i);
            if (p < minimum_p && p > threshold) {//if smaller but also more than the last iteration
                minimum_p = p; //update
                final_c = c;
            }
        }
        if(final_c == null){
            return null;
        }
        if(rand.nextDouble() < minimum_p){
            return final_c; //randomly selected
        }
        return randomAnimal(rand, minimum_p);//try again with a larger probability

    }

    /**
     * Determine whether the simulation is still viable.
     * I.e., should it continue to run.
     * @return true If there is more than one species alive.
     */
    private boolean isViable() {
        return stats.isViable(field);
    }

    public Field getField() {
        return this.field;
    }

    // Draw field if we have a gui defined
    public void drawField() {
        if ((graphicsWindow != null) && (view != null)) {
            view.drawField(this.field);
        }
    }

    public void drawGraph() {
        graph.draw();
    }


    public void handleMouseClick(float mouseX, float mouseY) {
        Vector2 loc = new Vector2(mouseX, mouseY); // get grid at
        Vector2 pos = view.gridLocationAt(loc);
      //add something here
    }
    public void handleMouseDrag(int mouseX, int mouseY) {
        Vector2 loc = new Vector2(mouseX, mouseY); // get grid at
        Vector2 pos = view.gridLocationAt(loc);
     // add something here

    }

}
