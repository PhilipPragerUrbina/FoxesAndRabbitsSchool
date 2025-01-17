package Field;

import Animals.Animal;

import java.util.Collection;
import java.util.HashMap;

/**
 * This class collects and provides some statistical data on the state 
 * of a field. It is flexible: it will create and maintain a counter 
 * for any class of object that is found within the field.
 * 
 * @author David J. Barnes and Michael Kolling. Modified by David Dobervich 2007-2022
 */
public class FieldStats {

    // Current counts for all object types
    // DANGER PUBLIC THING!  
    private HashMap<Class, Counter> counts;

    private boolean countsValid = false;

    /**
     * Construct a Field.FieldStats object.
     */
    public FieldStats()
    {
        // Set up a collection for counters for each type of animal that
        // we might find
        counts = new HashMap<Class, Counter>();
    }

    /**
     * Get details of what is in the field.
     * @return A string describing what is in the field.
     */
    public String getPopulationDetails(Field field)
    {
        StringBuffer buffer = new StringBuffer();
        if(!countsValid) {
            generateCounts(field);
        }
        for(Class key : counts.keySet()) {
            Counter info = counts.get(key);
            buffer.append(info.getName());
            buffer.append(": ");
            buffer.append(info.getCount());
            buffer.append(' ');
        }
        return buffer.toString();
    }
    
    /**
     * Invalidate the current set of statistics; reset all 
     * counts to zero.
     */
    public void reset()
    {
        countsValid = false;
        for(Class key : counts.keySet()) {
            Counter count = counts.get(key);
            count.reset();
        }
    }

    /**
     * Increment the count for one class of animal.
     * @param animalClass The class of animal to increment.
     */
    public void incrementCount(Class animalClass)
    {
        Counter count = counts.get(animalClass);
        if(count == null) {
            // We do not have a counter for this species yet.
            // Create one.
            count = new Counter(animalClass);
            counts.put(animalClass, count);
        }

        count.increment();
    }

    /**
     * Indicate that an animal count has been completed.
     */
    public void countFinished()
    {
        countsValid = true;
    }

    /**
     * Determine whether the simulation is still viable.
     * I.e., should it continue to run.
     * @return true If there is more than one species alive.
     */
    public boolean isViable(Field field)
    {
        // How many counts are non-zero.
        int nonZero = 0;
        if(!countsValid) {
            generateCounts(field);
        }

        for(Class key : counts.keySet()) {
            Counter info = counts.get(key);
            if(info.getCount() > 0) {
                nonZero++;
            }
        }

        return nonZero > 1;
    }
    
    /**
     * Generate counts of the number of foxes and rabbits.
     * These are not kept up to date as foxes and rabbits
     * are placed in the field, but only when a request
     * is made for the information.
     * @param field The field to generate the stats for.
     */
    public void generateCounts(Field field)
    {
        reset();
        for (Animal a : field.getAnimals()) {
            incrementCount(a.getClass());
        }
        countsValid = true;
    }
    
    public Collection<Counter> getCounts() {
    	return this.counts.values();
    }
}