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

    private static final double HUMAN_CREATION_PROBABILITY = 0.015;

    // The probability that a rabbit will be created in any given grid position.
    private static final double RABBIT_CREATION_PROBABILITY = 0.08;

    // Lists of animals in the field. Separate lists are kept for ease of
    // iteration.
    private ArrayList<Rabbit> rabbitList;
    private ArrayList<Fox> foxList;
    private ArrayList<Human> humanList;
    private ArrayList<Structure> structureList;

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

        rabbitList = new ArrayList<Rabbit>();
        foxList = new ArrayList<Fox>();
        humanList = new ArrayList<>();
        structureList = new ArrayList<>();
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
        view.setColor(Rabbit.class, p.color(155, 155, 155)); //gray
        view.setColor(Structure.class, p.color(0,255,0)); //green
        view.setColor(Human.class, p.color(255, 0, 0)); //red
        view.setColor(Fox.class, p.color(200, 0, 255));//purple

        graph = new Graph(p, view.getLeftEdge(), view.getBottomEdge()+VIEW_EDGE_BUFFER, view.getRightEdge(), p.height-VIEW_EDGE_BUFFER, 0,
                0, 500, field.getHeight() * field.getWidth());

        graph.title = "Animals.Fox and Animals.Rabbit and human and structure Populations";
        graph.xlabel = "Time";
        graph.ylabel = "Pop.\t\t";
        graph.setColor(Rabbit.class, p.color(155, 155, 155));
        graph.setColor(Fox.class, p.color(200, 0, 255));
        graph.setColor(Structure.class, p.color(0,255,0));
        graph.setColor(Human.class, p.color(255, 0 ,0));
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

        // New List to hold newborn rabbitList.
        ArrayList<Rabbit> babyRabbitStorage = new ArrayList<Rabbit>();

        // Loop through all Rabbits. Let each run around.
        for (int i = 0; i < rabbitList.size(); i++) {
            Rabbit rabbit = rabbitList.get(i);
            rabbit.run(updatedField, babyRabbitStorage);
            if (!rabbit.isAlive()) {
                rabbitList.remove(i);
                i--;
            }
        }

        // Add new born rabbitList to the main list of rabbitList.
        rabbitList.addAll(babyRabbitStorage);

        // Create new list for newborn foxList.
        ArrayList<Fox> babyFoxStorage = new ArrayList<Fox>();

        // Loop through Foxes; let each run around.
        for (int i = 0; i < foxList.size(); i++) {
            Fox fox = foxList.get(i);
            fox.hunt(field, updatedField, babyFoxStorage);
            if (!fox.isAlive()) {
                foxList.remove(i);
                i--;
            }
        }

        // Add new born foxList to the main list of foxList.
        foxList.addAll(babyFoxStorage);

        // Create new list for newborn humanList.
        ArrayList<Human> babyHumanStorage = new ArrayList<Human>();
        ArrayList<Structure> builtStructures = new ArrayList<Structure>();

        // Loop through Humans; let each live around.
        for (int i = 0; i < humanList.size(); i++) {
            Human human = humanList.get(i);
            human.step(field, updatedField, babyHumanStorage,builtStructures);
            if (!human.isAlive()) {
                humanList.remove(i);
                i--;
            }
        }


        // keep structures
        for (int i = 0; i < structureList.size(); i++) {
            Structure building = structureList.get(i);
            building.keep(updatedField);
        }

        humanList.addAll(babyHumanStorage);
        structureList.addAll(builtStructures);


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
        rabbitList.clear();
        foxList.clear();
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

                //start with the smallest probability
                 if (rand.nextDouble() <= HUMAN_CREATION_PROBABILITY) {
                    Human human = new Human((int)(Math.random()*Human.MAX_AGE), new Location(row,col));
                    humanList.add(human);
                    field.put(human, row, col);
                }
                else  if (rand.nextDouble() <= FOX_CREATION_PROBABILITY) {
                    Fox fox = new Fox(true);
                    fox.setLocation(row, col);
                    foxList.add(fox);
                    field.put(fox, row, col);
                } else if (rand.nextDouble() <= RABBIT_CREATION_PROBABILITY) {
                    Rabbit rabbit = new Rabbit(true);
                    rabbit.setLocation(row, col);
                    rabbitList.add(rabbit);
                    field.put(rabbit, row, col);
                }
            }
        }
        Collections.shuffle(rabbitList);
        Collections.shuffle(foxList);
        Collections.shuffle(humanList);
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
                    Object animal = field.getObjectAt(locToCheck);
                    if (animal instanceof Rabbit)
                        rabbitList.remove((Rabbit) animal);
                    if (animal instanceof Fox)
                        foxList.remove((Fox) animal);
                    if (animal instanceof Human)
                        humanList.remove((Human) animal);
                    if (animal instanceof Structure)
                        structureList.remove((Structure) animal);
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
