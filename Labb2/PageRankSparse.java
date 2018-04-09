import java.util.*;
import java.io.*;

public class PageRankSparse {

    /**
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    HashMap<String,Integer> docNumber = new HashMap<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a HashMap, whose keys are
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a HashMap whose keys are
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding
     *   key i is null.
     */
    HashMap<Integer,HashMap<Integer,Boolean>> link = new HashMap<Integer,HashMap<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;


    /* --------------------------------------------- */


    public PageRankSparse( String filename ) {
	int noOfDocs = readDocs( filename );
	iterate( noOfDocs, 1000 );
    }



    /* --------------------------------------------- */


    /**
     *   Reads the documents and fills the data structures.
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new HashMap<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
 	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */


    /*
     *   Chooses a probability vector a, and repeatedly computes
     *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
     */

     void iterate( int numberOfDocs, int maxIterations ) {
       // By doing this we do not have to set a[j] = 0
       List<Double> aCurr = new ArrayList<Double>(Collections.nCopies(numberOfDocs,1.0/numberOfDocs));
       List<Double> aNew = new ArrayList<Double>(Collections.nCopies(numberOfDocs,0.0));
       System.out.println(link.size());
       System.out.println(numberOfDocs);
       double diff = Math.abs(manhattan(subtract(aCurr,aNew)));
       int numIter = 0;
       while (numIter < maxIterations && diff > EPSILON){
        
        System.out.println(" Diff : " + diff);
        for (int j = 0; j < numberOfDocs; j++) {
           aNew.set(j, BORED / numberOfDocs);
             double sum = 0.0;
             
             for (int i = 0; i < numberOfDocs; i++){
                 if(link.containsKey(i)){
                    HashMap<Integer, Boolean> outlinks = link.get(i);
                    if (outlinks.containsKey(j)){                       
                            sum += (1 - BORED) * aCurr.get(i) / out[i];                          
                    }
                }
                else{
                        sum += aCurr.get(i) * (1 - BORED) / numberOfDocs;
                    }
                    
                 }
             aNew.set(j, sum);
           }
           
         
         double aNewSum = 0.0;
         for (double prob : aNew) {
           aNewSum = aNewSum + prob;
         }
         double extraTerm = (1.0 - aNewSum) / numberOfDocs;
         for (int k = 0; k < aNew.size(); k++){
           aNew.set(k, aNew.get(k) + extraTerm);
         }
         //double sum2 = aNew.stream().mapToDouble(Double::doubleValue).sum();
         //double sum3 = aCurr.stream().mapToDouble(Double::doubleValue).sum();
         //System.out.println(sum2 + " " + sum3);
         //System.out.println(aCurr.get(100) + ", " + aNew.get(100));
         diff = Math.abs(manhattan(subtract(aCurr,aNew)));

         aCurr = new ArrayList<Double>(aNew);
         
         numIter++;
       }

       System.out.println(" Diff is " + diff);
       System.out.println("number of iterations: " + numIter);
       Map<Double, String> topDocs = new TreeMap<Double, String>(Collections.reverseOrder());
       int docIx = 0;
       for(double prob : aNew){
         topDocs.put(prob, docName[docIx]);
         docIx++;
       }
       try {
       PrintWriter writer = new PrintWriter("pageranks.txt", "UTF-8");
       int i = 1;
       for (Map.Entry<Double, String> docEntry : topDocs.entrySet()) {
         if(i >= 30)System.out.println(docEntry.getValue() + ": " + docEntry.getKey());
         writer.println(docEntry.getValue() + " " + docEntry.getKey());
         i++;
       }
       writer.close();
    } catch (IOException e){
            System.err.println("Something went wrong");
        }
    
    }

     
    



     public static double[] subtract(List<Double> x,List<Double> y) {
         if (x.size() != y.size())
             throw new IllegalArgumentException("dimensions disagree");
         double[] toReturn = new double[x.size()];
         for (int i = 0; i < x.size(); i++)
             toReturn[i] = x.get(i) - y.get(i);
         return toReturn;
     }

     public static double manhattan(double[] x) {
         double result = 0;
         for (int i = 0; i < x.length; i++)
             result += Math.abs(x[i]);
         return result;
     }


    // void iterate( int numberOfDocs, int maxIterations ) {
    //   // By doing this we do not have to set a[j] = 0
    //   List<Double> a = new ArrayList<Double>(Collections.nCopies(numberOfDocs,1.0/numberOfDocs));
    //   System.out.println(link.size());
    //   System.out.println(numberOfDocs);
    //   for (Map.Entry<Integer, HashMap<Integer, Boolean>> entry : link.entrySet()) {
    //     Integer key = entry.getKey();
    //     HashMap<Integer, Boolean> outlinks = entry.getValue();
    //
    //     for (Map.Entry<Integer, Boolean> outlink : outlinks.entrySet()) {
    //       Integer toDoc = outlink.getKey();
    //       Boolean conectionExist = outlink.getValue();
    //       //System.out.print(" " + (int)toDoc + " - " + (Boolean)conectionExist);
    //       }
    //   }
    // }


    /* --------------------------------------------- */



    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRankSparse( args[0] );
	}
    }
}
