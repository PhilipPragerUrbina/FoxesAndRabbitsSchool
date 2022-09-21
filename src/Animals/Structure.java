package Animals;

import Field.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//class to represent a human built object
// does nothing, just takes up space
public class Structure extends Animal{


    public Structure( Location location) {
        super(location);
    }

    @Override
    public Color getColor() {
        return new Color(0,255,0); //green
    }

    @Override
    public void performActions(Field current_field, Field next_field, List<Animal> new_animals) {
        next_field.put(this, location);
    }

    @Override
    protected void checkDeath() {

    }

    @Override
    public String getTypeName() {
        return "Structure";
    }
}
