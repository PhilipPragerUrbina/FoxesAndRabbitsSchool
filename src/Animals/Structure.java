package Animals;

import Field.*;
import java.util.List;

//class to represent a human built object
// does nothing, just takes up space
public class Structure {
    private Location location;

    public Structure( Location location) {
        this.location = location;
    }

    //keep the structure there
    public void keep(Field next_field) {
        next_field.put(this, location);
    }

}
