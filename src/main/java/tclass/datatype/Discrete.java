/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
  * An implementation of the Discrete data type. By default, this contains
  * two classes: true, false. 
  * 
  * @author Waleed Kadous
  * @version $Id: Discrete.java,v 1.1.1.1 2002/06/28 07:36:16 waleed Exp $
  */

package tclass.datatype;   

import java.util.StringTokenizer;

import tclass.DataTypeI;
import tclass.InvalidParameterException;
import tclass.Param;
import tclass.ParamVec;
import tclass.util.Debug;
import tclass.util.StringMap;

public class Discrete implements DataTypeI {
    
  /** for serialization. */
  private static final long serialVersionUID = -4021483744130676788L;
    boolean isOrdered = false; 
    boolean complexCost = false; 
    StringMap valmap = new StringMap(); 
    float[][] costs; //Is used for things that have complex cost
                      // functions.
    
    
    public Discrete(){
	// The default construction is with two classes, true and false. 
	// False has a value of 0, true a value of 1.  
	valmap.add("false"); 
	valmap.add("true"); 
    }

    /** 
     * Get the names
     */ 

    public String getName(){
	return "discrete";
    }

    public int size(){
	return valmap.size(); 
    }
    
    public String elAt(int i){
	return valmap.getString(i); 
    }

    /**
     * Convert between a user-input value for the class and the
     * internal float representation. Can be complicated sometimes. 
     * For example, if this is a discrete class, then a hash table
     * mapping discrete values to a float must be maintained to do
     * the read. 
     */
    
    public float read(String s){
	// We are given a value as a string. To convert, we look them up. 
	return (float) valmap.getInt(s); 
    }

    @Override
    public String toString(){
	String retval = "discrete. values = "; 
	for(int i=0; i < valmap.size(); i++){ 
	    retval += valmap.getString(i) + " "; 
	}
	if(complexCost){
	    retval += "\nCost of misclassifications: \n"; 
	    int numVals = valmap.size(); 
	    for(int i=0; i< numVals; i++){
		for(int j=0; j < numVals; j++){
		    retval += costs[i][j] + "\t"; 
		}
		retval += "\n"; 
	    }
	}
	return retval; 
	
    }

    /**
     * Compute the distance between two instances belonging to the
     * same data type. Should meet all the criteria for a metric. 
     */ 

    public float distance(float a, float b){
	if(!complexCost){
	    if(a == b)
		return 0; 
	    else 
		return 1; 
	}
	else {
	    return costs[(int) a][(int) b]; 
	}
    }
    
    /**
     * Clone the current object. 
     *
     */ 

    @Override
    public Object clone()
    {
	try {
	    return super.clone(); 
	}
	catch (CloneNotSupportedException e){
	    // Can't happen, or so the java programming book says
	    throw new InternalError(e.toString()); 
	}
    }

    public boolean isOrdered(){
	return isOrdered; 
    }
	
    
    public int cmp(float a, float b){
	//Because we know StringMap is well-behaved, we can just do the 
	//expected thing with a and b
	if(!isOrdered) return 0; 
	//So now we know it must be ordered. 
	if(a<b) return -1; 
	if(a==b) return 0; 
	if(a>b) return 1; 
	return 0; 
    }
    /**
     * Convert back from our internal representation back into a
     * more user-friendly one. 
     */ 
    
    public String print(float a){
	return valmap.getString((int) a); 
    }

    /**  
     * 
     * Describes any parameters this Data type can handle. 
     * 
     * @return A vector of parameters.  
     */     
    public ParamVec getParamList(){
	ParamVec p =  new ParamVec(); 
	p.add(new Param("values", 
			"A quote-enclosed, space-delimited list of classes", 
			"\"true false\""));
	p.add(new Param("costmetric", 
			"simple (misclassification of all classes returns a distance of 1) or "
			+ "complex (misclassifications use a table of costs)",
			"simple")); 
	p.add(new Param("cost", "\"a b cost\", where cost is the cost of " +
			"misclassifying a as b. There can be many such clauses", "0 if a = b, 1 otherwise"));
	
	p.add(new Param("ordered", "classes are ordered or not", "false")); 
	return p; 
    }
    
    /** 
     * Configures this instance so that parameter <i>p</i> has 
     * value <i>v</i>.  
     * 
     * @param p the parameter to set.  
     * @param v the value of the parameter.  
     * 
     */ 
 
    public void setParam(String p, String v) throws InvalidParameterException {
	Debug.dp(Debug.FN_CALLS, "Setting " + p + " = " + v); 
	if(p.equals("values")){
	    valmap.clear(); 
	    StringTokenizer st = new StringTokenizer(v); 
	    while(st.hasMoreTokens()){
		valmap.add(st.nextToken()); 
	    }	    
	}
	else if(p.equals("ordered")){
	    if(v.equals("true")){
		isOrdered = true; 
	    }
	    else if(v.equals("false")){
		isOrdered = false; 
	    }
	    else {
		throw new InvalidParameterException(p, v, "Ordered can only take values true or false"); 
	    }
	}
	else if(p.equals("costmetric")){
	    if(v.equals("simple")){
		complexCost = false; 
	    }
	    else if(v.equals("complex")){
		complexCost = true; 
		//Uh oh. Have to use the big array. 
		int numVals = valmap.size(); 
		costs = new float[numVals][numVals]; 
		//Pre-initialise everything to 1, then put in 
		//the zeros. 
		for(int i=0; i < numVals; i++){
		    for(int j=0; j < numVals; j++){
			costs[i][j]= (float) 1.0; 
		    }
		}
		for(int i=0; i < numVals; i++){
		    costs[i][i]= (float) 0.0; 
		}
	    }
	    else 
		throw new InvalidParameterException(p, v, 
						    "Acceptable values for costmetric: simple, complex"); 
	    
	}
	else if(p.equals("cost")){
	    //We have to pull it to pieces. 
	    StringTokenizer st = new StringTokenizer(v); 
	    String a = st.nextToken(); 
	    String b = st.nextToken(); 
	    String cost = st.nextToken(); 
	    int valA = valmap.getInt(a); 
	    int valB = valmap.getInt(b); 
	    float dCost = Float.valueOf(cost).floatValue(); 
	    if(valA == -1 || valB == -1)
		throw new InvalidParameterException(p,v,
						    "One of the two class labels " + a + " " + b + " does not exist."); 
	    costs[valA][valB] = dCost; 
	    	   
	}
	else
	    throw new InvalidParameterException(p,v,"No such parameter"); 
    }
    
    
}
