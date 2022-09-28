package Field;
//helper class for evolutionary traits
public class Gene {

    //create a new gene with a random starting value
    public Gene(double min, double max){
        value = (Math.random() * (max-min))+min;
    }

    //create a gene from a parent gene with a certain amount of +- variation
    public Gene(Gene parent, double max_variation){
        value = parent.value + ((Math.random() * max_variation * 2.0)-(max_variation));//random between -variation and +variation
    }

    //get gene value
    public double getValue(){
        return value;
    }
    //gene value
    private double value;
}
