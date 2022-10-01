import Animals.Fox;
import Animals.Human;
import Animals.Rabbit;
import Simulator.Simulator;
import processing.core.*;

//main class
public class Main extends PApplet {
    private static final int TEXT_EDGE_BUFFER = 0;
    private static final int DELAY = 20; //delay slowing down simulation
    private Simulator simulator;
    private boolean paused = true;
    private int textSize = 10;

    public void settings() {
        size(800, 800);  // window size in pixels
    }

    @Override
    public void setup() {
        calculateTextSize();
        this.simulator = new Simulator(100, 100,100*100); //simulation field size and max animals
        //add animals
        this.simulator.addAnimal(Rabbit.class, 0.08);
        this.simulator.addAnimal(Fox.class, 0.04);
        this.simulator.addAnimal(Human.class, 0.0005);

        //set up
        this.simulator.populate();
        this.simulator.setGUI(this);
    }

    private void calculateTextSize() {
        textSize(textSize);
        float tempTextWidth = 0;
        do {
            textSize++;
            textSize(textSize);
            tempTextWidth = textWidth("Press 'p' to pause and unpause the simulation");
        } while (tempTextWidth < width - 2* TEXT_EDGE_BUFFER);
        textSize--;
        System.out.println("Set text size to " + textSize);
    }

    @Override
    public void draw() {
        background(200);
        if (!paused) {
            simulator.simulateOneStep();
        }
        try {
            Thread.sleep(DELAY);//delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        simulator.drawField();
        simulator.drawGraph();
        displayTextInstructions();
    }

    private void displayTextInstructions() {
        if (paused) {
            textAlign(CENTER, CENTER);
            textSize(textSize);

            fill(20, 200, 35);
            stroke(20, 200, 35);
            rect(0, height/2 - 2*textSize, width,4*textSize);

            fill(0);
            stroke(0);
            text("Press 'p' to pause and unpause the simulation", width/2, height/2 - textSize);
            text("Press 'r' to reset the simulation", width/2, height/2 + textSize);
        }
    }

    // handle key presses
    public void keyReleased() {
        if (key == 'p' || key == 'P') {                // 'p' toggles paused and unpaused
            paused = !paused;
        }

        if (key == 'r' || key == 'R') {                // 'r' resets the simulator
            simulator.reset();
        }
    }

    // if mouse clicked, let the simulator handle the mouse click
    public void mouseClicked() {
        simulator.handleMouseClick(mouseX, mouseY);
    }

    // if mouse is dragged, let the simulator handle the mouse drag
    public void mouseDragged() {
        simulator.handleMouseDrag(mouseX, mouseY);
    }

    public static void main(String[] args) {
        PApplet.main(new String[]{"Main"});
    }
}