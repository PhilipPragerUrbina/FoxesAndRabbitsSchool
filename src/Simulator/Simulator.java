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

    /**
     * Construct a simulation field with default size.
     */
    public Simulator() {
        this(DEFAULT_HEIGHT, DEFAULT_WIDTH);
    }

    /**
     * Create a simulation field with the given size.
     *
     * @param height Height of the field. Must be greater than zero.
     * @param width  Width of the field. Must be greater than zero.
     */
    public Simulator(int width, int height) {
        if (width <= 0 || height <= 0) {
            System.out.println("The dimensions must be greater than zero.");
            System.out.println("Using default values.");
            height = DEFAULT_HEIGHT;
            width = DEFAULT_WIDTH;
        }

        animal_list = new ArrayList<Animal>();
        field = new Field(width, height);
        updatedField = new Field(width, height);
        stats = new FieldStats();
        spawn_probabilities= new ArrayList<>();
        animals_to_add = new ArrayList<>();


    }

    //do after animals classes have been added
   public void populate(){
        // Setup a valid starting point.
        reset();
    }

    public void setGUI(PApplet p) {
        this.graphicsWindow = p;



        // Create a view of the state of each location in the field.
        view = new FieldDisplay(p, this.field, VIEW_EDGE_BUFFER, VIEW_EDGE_BUFFER, p.width - 2*VIEW_EDGE_BUFFER, p.height / 2 - 2 * VIEW_EDGE_BUFFER);



        graph = new Graph(p, view.getLeftEdge(), view.getBottomEdge()+VIEW_EDGE_BUFFER, view.getRightEdge(), p.height-VIEW_EDGE_BUFFER, 0,
                0, 500, 200);



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
    public void runLongSimulation() {
        simulate(500);
    }

    //add animal class to simulation
    public void addAnimal(Class<? extends Animal> type, double spawn_prob){
        spawn_probabilities.add(spawn_prob);
        animals_to_add.add(type);
    }

    /**
     * Run the simulation from its current state for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     *
     * @param numSteps The number of steps to run for.
     */
    public void simulate(int numSteps) {
        for (int step = 1; step <= numSteps && isViable(); step++) {
            simulateOneStep();
        }
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
            graph.setDataRanges(0, 500, 0, 200);
        }

        // Show the starting state in the view.
        // view.showStatus(step, field);
    }

    /**
     * Populate a field with foxList and rabbitList.
     *
     * @param field The field to be populated.
     */
    private void initializeBoard(Field field) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Random rand = new Random();
        field.clear();
        for (int row = 0; row < field.getHeight(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {
                Class<? extends Animal> c = randomAnimal(rand,0);
                if(c != null){
                    Constructor<? extends Animal> con =  c.getConstructor(boolean.class, Vector2.class);
                    Animal new_animal = (Animal)con.newInstance(true,new Vector2(row,col));
                    animal_list.add(new_animal);
                    field.put(new_animal);
                }

            }
        }
        Collections.shuffle(animal_list);
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

    // Perform an action when the mouse was clicked.
    // parameters are the x, y screen coordinates the user clicked on.
    // Note: you probably want to modify handleMouseClick(Field.Location) which
    // gives you the location they clicked on in the grid.
    public void handleMouseClick(float mouseX, float mouseY) {
        Vector2 loc = new Vector2(mouseX, mouseY); // get grid at
        // click.
        if (loc == null) return;
//todo fix
      /*  for (int x = (int)loc.x - 2; x < loc.x + 2; x++) {
            for (int y = (int)loc.y - 2; y < loc.y + 2; y++) {
                Vector2 locToCheck = view.gridLocationAt(new Vector2(x, y));
                if (field.isLegalLocation(locToCheck)) {
                    Animal animal = field.closestAnimal(locToCheck); //get animal at location
                   animal_list.remove(animal); //remove
                    field.remove(animal);
                    updatedField.remove(animal);
                }
            }
        }*/
    }

    private void handleMouseClick(Vector2 l) {
        System.out.println("Change handleMouseClick in Simulator.Simulator.java to do something!");
    }

    public void handleMouseDrag(int mouseX, int mouseY) {
        Vector2 loc = new Vector2(mouseX, mouseY); // get grid at
        // click.
        if (loc == null)
            return; // if off the screen, exit
        handleMouseDrag(loc);
    }

    private void handleMouseDrag(Vector2 l) {
        System.out.println("Change handleMouseDrag in Simulator.Simulator.java to do something!");
    }
}
