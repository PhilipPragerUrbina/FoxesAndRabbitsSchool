package Simulator;

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

    // The probability that a fox will be created in any given grid position.
    private static final double FOX_CREATION_PROBABILITY = 0.02;

    private static final double HUMAN_CREATION_PROBABILITY = 0.0005;

    // The probability that a rabbit will be created in any given grid position.
    private static final double RABBIT_CREATION_PROBABILITY = 0.08;

    // Lists of animals in the field. Separate lists are kept for ease of
    // iteration.
    private ArrayList<Animal> animal_list;

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

        // Setup a valid starting point.
        reset();
    }

    public void setGUI(PApplet p) {
        this.graphicsWindow = p;



        // Create a view of the state of each location in the field.
        view = new FieldDisplay(p, this.field, VIEW_EDGE_BUFFER, VIEW_EDGE_BUFFER, p.width - 2*VIEW_EDGE_BUFFER, p.height / 2 - 2 * VIEW_EDGE_BUFFER);



        graph = new Graph(p, view.getLeftEdge(), view.getBottomEdge()+VIEW_EDGE_BUFFER, view.getRightEdge(), p.height-VIEW_EDGE_BUFFER, 0,
                0, 500, field.getHeight() * field.getWidth());


        for (Animal animal : animal_list) {
            graph.setColor(animal.getClass(), animal.getColor().hashCode()); //set graph colors
            view.setColor(animal.getClass(), animal.getColor().hashCode()); //set field colors

        }

        graph.title = "Animals.Fox and Animals.Rabbit and human and structure Populations";
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
        initializeBoard(field);

        if (graph != null) {
            graph.clear();
            graph.setDataRanges(0, 500, 0, field.getHeight() * field.getWidth());
        }

        // Show the starting state in the view.
        // view.showStatus(step, field);
    }

    /**
     * Populate a field with foxList and rabbitList.
     *
     * @param field The field to be populated.
     */
    private void initializeBoard(Field field) {
        Random rand = new Random();
        field.clear();
        for (int row = 0; row < field.getHeight(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {
//todo cleanup this if statements
                //start with the smallest probability
                 if (rand.nextDouble() <= HUMAN_CREATION_PROBABILITY) {
                    Human human = new Human(true, new Location(row,col));
                    animal_list.add(human);
                    field.put(human, row, col);
                }
                else  if (rand.nextDouble() <= FOX_CREATION_PROBABILITY) {
                    Fox fox = new Fox(true,new Location(row,col));
                     animal_list.add(fox);
                    field.put(fox, row, col);
                } else if (rand.nextDouble() <= RABBIT_CREATION_PROBABILITY) {
                    Rabbit rabbit = new Rabbit(true,new Location(row,col));
                     animal_list.add(rabbit);
                    field.put(rabbit, row, col);
                }
            }
        }
        Collections.shuffle(animal_list);
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
        Location loc = view.gridLocationAt(mouseX, mouseY); // get grid at
        // click.
        if (loc == null) return;

        for (int x = loc.getCol() - 8; x < loc.getCol() + 8; x++) {
            for (int y = loc.getRow() - 8; y < loc.getRow() + 8; y++) {
                Location locToCheck = new Location(y, x);
                if (field.isLegalLocation(locToCheck)) {
                    //todo fix this field class
                    Animal animal = (Animal) field.getObjectAt(locToCheck);
                   animal_list.remove(animal);

                    field.put(null, locToCheck);
                    updatedField.put(null, locToCheck);
                }
            }
        }
    }

    private void handleMouseClick(Location l) {
        System.out.println("Change handleMouseClick in Simulator.Simulator.java to do something!");
    }

    public void handleMouseDrag(int mouseX, int mouseY) {
        Location loc = this.view.gridLocationAt(mouseX, mouseY); // get grid at
        // click.
        if (loc == null)
            return; // if off the screen, exit
        handleMouseDrag(loc);
    }

    private void handleMouseDrag(Location l) {
        System.out.println("Change handleMouseDrag in Simulator.Simulator.java to do something!");
    }
}
