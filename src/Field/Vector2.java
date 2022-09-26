package Field;

//2d vector class
public class Vector2  {
    //values
    public double x;
    public double y;

    //constructors
    public Vector2(double x, double y){
        this.x = x;
        this.y = y;
    }
    

    //from scalar
    public Vector2(double scalar){
        this.x = scalar;
        this.y = scalar;
    }
    //zero vector
    public Vector2(){
        this.x = 0;
        this.y = 0;
    }

    //operators
    public Vector2 add(Vector2 b){
        return new Vector2(this.x+b.x, this.y+b.y);
    }
    public Vector2 subtract(Vector2 b){
        return new Vector2(this.x-b.x, this.y-b.y);
    }
    public Vector2 multiply(Vector2 b){
        return new Vector2(this.x*b.x, this.y*b.y);
    }
    public Vector2 divide(Vector2 b){
        return new Vector2(this.x/b.x, this.y/b.y);
    }

    //comparison
    public boolean equalsExact(Vector2 b){
        return this.x == b.x && this.y == b.y ;
    }
    public boolean equals(Vector2 b, double delta){
        return deltaDouble(this.x , b.x, delta) && deltaDouble(this.y , b.y, delta) ;
    }

    //utilities
    private static boolean deltaDouble(double a, double b, double d){
        return Math.abs(a-b) < d;
    }

    public double distance(Vector2 b){
        return Math.sqrt(Math.pow((this.x-b.x),2)+Math.pow((this.y-b.y),2));
    }

    public double length(){
        return Math.sqrt(x*x + y*y );
    }

    public Vector2 normalized(){
        return this.divide(new Vector2(this.length()));
    }

    //random components between -0.5 and 0.5
    public static Vector2 randomVector(){
        return new Vector2(Math.random() - 0.5, Math.random() - 0.5);
    }

    //get random normalized direction
    public static Vector2 randomDirection(){
        return randomVector().normalized();
    }

    public double dot(Vector2 b){
        return this.x * b.x + this.y * b.y ;
    }

    //move in direction by distance
    Vector2 move(Vector2 dir, double dist){
        return this.add(dir.multiply(new Vector2(dist)));
    }

    //return minimum values from both vectors
    Vector2 getMin(Vector2 b){
        return new Vector2(Math.min(x, b.x), Math.min(y, b.y));
    }
    Vector2 getMax(Vector2 b){
        return new Vector2(Math.max(x, b.x), Math.max(y, b.y));
    }


    @Override
    public String toString() {
        return "(" + "," + x + "," + y  + ')';
    }
}
