import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
/**
 * A CollisionDetector can hold objects at a location and detect which objects collide with it as defined by being within a certain radius
*/
public class CollisionDetector<T>{
    private final HashMap<T, Coordinate> LOCATION;
    private final HashSet<T>[][] AREA;
    private final Coordinate TOPLEFTCORNER;
    private final int ROWAREALENGTH;
    private final int COLUMNAREALENGTH;
    private final boolean DEBUG = false;
    /*
    public static void main(String[] args){
        String hello = "hello";
        String dog = "dog";
        String cat = "cat";
        String Y = "Y";
        CollisionDetector<String> c = new CollisionDetector<>(1001, 1001);
        c.add(hello, 1000, 1000);
        c.add(dog, 800, 1000);
        c.add(cat, 1000, 1001);
        c.add(Y, 1000, 1000);
        c.remove(dog);
        for(String item: c.findCollisions(Y, 1)){
            System.out.println(item);
        }
    }
    */
    /**
     * Contrusts a new CollisionDetector of width 'width', height 'height',
     * number of internal rows, numOfRows and number of internal colums of 'numOfColumns'.
    */
    CollisionDetector(int width, int height, int numOfRows, int numOfColumns){
        if(width <= 0 || height <= 0 || numOfColumns <= 0 || numOfColumns <= 0){
            throw new IllegalArgumentException();
        }
        this.LOCATION = new HashMap<>();
        this.AREA = new HashSet[numOfRows + 1][numOfColumns + 1];
        this.TOPLEFTCORNER = new Coordinate(0, 0);
        this.COLUMNAREALENGTH = width / numOfColumns;
        this.ROWAREALENGTH = height / numOfRows;
        populateArea();
        checkRep();
    }
    /**
     * Contrusts a new CollisionDetector of width 'width', height 'height'.
    */
    CollisionDetector(int width, int height){
        if(width <= 0 || height <= 0){
            throw new IllegalArgumentException();
        }
        this.COLUMNAREALENGTH = 100;
        this.ROWAREALENGTH = 100;
        this.LOCATION = new HashMap<>();
        this.AREA = new HashSet[height/this.ROWAREALENGTH + 1][width/this.COLUMNAREALENGTH + 1];
        this.TOPLEFTCORNER = new Coordinate(0, 0);
        populateArea();
        checkRep();
    }
    /**
     * Add object 'obj' to 'this' at coordinates x,y.
     * 
     * @param obj object to be added
     * @param x x coordiate of obj
     * @param y y coordiate of obj
     * @requires x and y are in range
     * @modifies 'this' adds 'obj' at x,y
     * @throws IllegalArgumentException 'obj' is already present or is null
    */
    public void add(T obj, int x, int y){
        checkRep();
        if(this.LOCATION.containsKey(obj)){
            throw new IllegalArgumentException("Cannot add object that is already present.");
        }if(obj == null){
            throw new IllegalArgumentException("No null inputs to add.");
        }
        Coordinate coordinate = new Coordinate(x, y);
        Coordinate areaLocation = findAreaLocation(x, y);
        HashSet<T> area = this.AREA[areaLocation.y][areaLocation.x];
        area.add(obj);
        this.LOCATION.put(obj, coordinate);
        checkRep();
    }
    /**
     * Removes object 'obj' from 'this' if present.
     * 
     * @param obj object to be removed
     * @modifies 'this', removes obj if present
    */
    public void remove(T obj){
        checkRep();
        if(!contains(obj)){
            return;
        }
        Coordinate location = this.LOCATION.get(obj);
        Coordinate areaLocation = findAreaLocation(location.x, location.y);
        HashSet<T> area = this.AREA[areaLocation.y][areaLocation.x];
        area.remove(obj);
        this.LOCATION.remove(obj);
        checkRep();
    }
    /**
     * Checks to see if two objects collide with given radius (inclusive).
     * 
     * @param obj1 first object that could collide
     * @param obj2 collide object that could collide
     * @param radius radius of collison cirlce (inclusive)
     * @throws IllegalArgumentException iff null input(s) or one 'T' is not present
     * @return true iff distance between obj1 and obj2 is less than or equal to radius
    */
    public boolean checkCollision(T obj1, T obj2, int radius){
        this.checkRep();
        if(obj1 == null || obj2 == null){
            throw new IllegalArgumentException("checkCollision parameters cannot be null.");
        }
        if(!(this.contains(obj1) && this.contains(obj2))){
            throw new IllegalArgumentException("Both elements must be present to check for collision.");
        }
        if(obj1 == obj2){
            this.checkRep();
            return false;
        }
        int[] c1 = this.findLocation(obj1);
        int[] c2 = this.findLocation(obj2);
        double distance = this.distance(c1, c2);
        this.checkRep();
        return distance <= radius;
    }
    /**
     * Return all elements in 'items' that collide with 'obj' with a radius of 'radius'.
     * 
     * @param obj object to check collison with
     * @param items items to check collison with
     * @radius required radius for collision (inclusive)
     * @throws IllegalArgumentException iff null input given
     * @requires all T are present and not null
     * @return all items in 'items' that collide with obj in a 'radius' radius (inclusive)
    */
    public HashSet<T> checkCollisions(T obj, HashSet<T> items, int radius){
        this.checkRep();
        if(items == null || obj == null){
            throw new IllegalArgumentException("null input.");
        }
        HashSet<T> goal = new HashSet<>();
        for(T item: items){
            if(this.checkCollision(obj, item, radius)){
                goal.add(item);
            }
        }
        this.checkRep();
        return goal;
    }
    /**
     * Moves object 'obj' to x,y
     * 
     * @param obj object to be removed
     * @param x x coordinate of destination
     * @param y y coordinate of destination
     * @requires x and y are in range
     * @modifies 'this' moves 'obj' to x,y
     * @throws IllegalArgumentException if 'obj' is not present
    */
    public void move(T obj, int x, int y){
        checkRep();
        if(!this.contains(obj)){
            throw new IllegalArgumentException("Cannot move item that is not present");
        }
        this.remove(obj);
        this.add(obj, x, y);
        checkRep();
    }
    /**
     * Returns of HashSet of all T that collide with 'obj' where anything within 'radius' distance away (inclusive) collides with obj.
     * 
     * @param obj object to check collision with
     * @param radius radius of collision area
     * @throws IllegalArgumentException if 'obj' is not present
     * @return all T that collise with 'obj' with a collision radius of 'radius' (inclusive)
    */
    public HashSet<T> findCollisions(T obj, int radius){
        checkRep();
        if(!this.contains(obj)){
            throw new IllegalArgumentException("Cannot find collision for non-existant object.");
        }
        radius = Math.abs(radius);
        Coordinate objLocation = this.LOCATION.get(obj);
        HashSet<T> goal = new HashSet<>();
        int xLowerBound = this.findAreaLocation(objLocation.x - radius, objLocation.y).x;
        int yLowerBound = this.findAreaLocation(objLocation.x, objLocation.y - radius).y;
        int xUpperBound = this.findAreaLocation(objLocation.x + radius, objLocation.y).x;
        int yUpperBound = this.findAreaLocation(objLocation.x, objLocation.y + radius).y;
        if(xLowerBound < 0){
            xLowerBound = 0;
        }
        if(yLowerBound < 0){
            yLowerBound = 0;
        }
        if(yUpperBound >= this.AREA.length){
            yUpperBound = this.AREA.length - 1;
        }
        if(xUpperBound >= this.AREA[0].length){
            xUpperBound = this.AREA[0].length - 1;
        }
        for(int i = xLowerBound; i != xUpperBound + 1; i++){
            for(int j = yLowerBound; j != yUpperBound + 1; j++){
                HashSet<T> area = this.AREA[j][i];
                HashSet<T> collisions = this.checkCollisions(obj, area, radius);
                for(T collision: collisions){
                    goal.add(collision);
                }
            }
        }
        checkRep();
        return goal;
    }
    /**
     * Find the x,y location of 'obj' or return null if it is not present.
     * 
     * @param obj object to find location of
     * @return {x,y} of 'obj'
     * @return null iff 'obj' is not present
    */
    public int[] findLocation(T obj){
        checkRep();
        if(!this.contains(obj)){
            return null;
        }
        int[] goal = new int[2];
        Coordinate location = this.LOCATION.get(obj);
        goal[0] = location.x;
        goal[1] = location.y;
        checkRep();
        return goal;
    }
    /**
     * Return number of items in 'this'.
     * 
     * @return number of items stored
    */
    public int size(){
        return this.LOCATION.size();
    }
    /**
     * Return if 'obj' is present.
     * 
     * @return true iff 'obj' is present
    */
    public boolean contains(T obj){
        return this.LOCATION.containsKey(obj);
    }
    /**
     * Return set of all present elements.
     * 
     * @return all elements contained in 'this'
    */
    public Set<T> getAllItems(){
        return this.LOCATION.keySet();
    }
    /**
     * Find which block a coordinate falls under. Coordinates out of
     * bounds are assumed to belong to the closest block.
     * 
     * @param x x coordinate
     * @param x y coordinate
     * @return Coordinate of bloch where x,y would be stored
    */
    private Coordinate findAreaLocation(int x, int y){
        int goalX = (x - this.TOPLEFTCORNER.x) / this.COLUMNAREALENGTH;
        int goalY = (y - this.TOPLEFTCORNER.y) / this.ROWAREALENGTH;
        return new Coordinate(goalX, goalY);
    }
    /**
     * Fills all area indexes in 'this' with an empty HashSets.
     * 
     * @modifies 'this' fills in and replaces all areas with empty HashMap
    */
    private void populateArea(){
        for(int i = 0; i != this.AREA.length; i++){
            for(int j = 0; j != this.AREA[i].length; j++){
                AREA[i][j] = new HashSet<>();
            }
        }
    }
    private void checkRep(){
        if(!DEBUG){
            return;
        }
        asert(this.AREA.length > 0, "Grid is not wide enough");
        asert(this.AREA[0].length > 0, "Grid is not tall enough");
        for(int i = 0; i != this.AREA.length; i++){
            for(int j = 0; j != this.AREA[0].length; j++){
                HashSet<T> items = this.AREA[i][j];
                for(T item: items){
                    asert(this.LOCATION.containsKey(item), "item is not present in HashMap LOCATION.");
                    int numOfItem = 0;
                    for(int ii = 0; ii != this.AREA.length; ii++){
                        for(int jj = 0; jj != this.AREA[0].length; jj++){
                            HashSet<T> items2 = this.AREA[ii][jj];
                            for(T item2: items2){
                                if(item == item2){
                                    asert(items == items2, "duplicate item in different areas.");
                                    numOfItem++;
                                }
                            }
                        }
                    }
                    asert(numOfItem == 1, "There is not exactly one of an item.");
                }
                }
            }
            for(T item: this.LOCATION.keySet()){
                Coordinate coordinate = this.LOCATION.get(item);
                HashSet<T> hashSet = this.AREA[coordinate.y / this.ROWAREALENGTH][coordinate.x / this.COLUMNAREALENGTH];
                asert(hashSet.contains(item), "Coordinate does not lead to item");
            }
        }
    private void asert(boolean statement, String errorMessage){
        if(!statement){
            throw new RuntimeException(errorMessage);
        }
    }
    private double distance(int[] first, int[] second){
        int x = first[0] - second[0];
        int y = first[1] - second[1];
        return Math.pow(Math.pow(x, 2) + Math.pow(y, 2), 0.5);
    }
    private class Coordinate{
        public int x;
        public int y;
        Coordinate(int x, int y){
            this.x = x;
            this.y = y;
        }
    }
}