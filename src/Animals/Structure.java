package Animals;

import Field.*;
import java.awt.*;
import java.util.List;

//class to represent a human built object
// does nothing, just takes up space
public class Structure extends Animal{
    private final static double RADIUS = 2.0; //how wide building is

    //create new structure at location
    public Structure( Vector2 location) {super(location);setRadius(RADIUS);}
    @Override
    public Color getColor() {
        return new Color(0,255,0); //green
    }
    @Override
    public String getTypeName() {
        return "Structure";
    }
    @Override
    public void performActions(Field current_field, Field next_field, List<Animal> new_animals) {next_field.put(this);}
    @Override
    protected void checkDeath() {}

}
