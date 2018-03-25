import java.util.*;
import java.io.*;
import java.lang.reflect.Array;

public class PageRank {

    /**
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory;
     */
    final static int MAX_NUMBER_OF_DOCS = 1000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**
     *   The transition matrix. p[i][j] = the probability that the
     *   random surfer clicks from page i to page j.
     */
    double[][] p = new double[MAX_NUMBER_OF_DOCS][MAX_NUMBER_OF_DOCS];

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
     *   In the initializaton phase, we use a negative number to represent
     *   that there is a direct link from a document to another.
     */
    final static double LINK = -1.0;

    /**
     *   Convergence criterion: Transition probabilities do not
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;


    /* --------------------------------------------- */


    public PageRank( String filename ) {
	int noOfDocs = readDocs( filename );
	initiateProbabilityMatrix( noOfDocs );
	iterate( noOfDocs, 100 );
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and fills the data structures. When this method
     *   finishes executing, <code>p[i][j] = LINK</code> if there is a direct
     *   link from i to j, and <code>p[i][j] = 0</code> otherwise.
     *   <p>
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
        //System.out.println(title);
        Integer fromdoc = docNumber.get( title );

		//  Have we seen this document before?
		if ( fromdoc == null ) {
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
        }
        //System.out.println(this.docName[fromdoc]);
        //System.out.println();
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
		    // Set the probability to LINK for now, to indicate that there is
		    // a link from d to otherDoc.
		    if ( p[fromdoc][otherDoc] >= 0 ) {
			p[fromdoc][otherDoc] = LINK;
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
     *   Initiates the probability matrix.
     */
    void initiateProbabilityMatrix( int numberOfDocs ) {

    double[][] pNormal = new double [numberOfDocs][numberOfDocs];

    // First row all the probabilities are 1/docNum
    for (int i = 0; i < numberOfDocs; i++){
        for(int j = 0; j < numberOfDocs; j++){
            if (p[i][j] == -1){
                pNormal[i][j] = (1.0 - BORED)/ out[i] + BORED / numberOfDocs;
            }else{
                pNormal[i][j] = BORED / numberOfDocs;
            }
        }
    }
    p = pNormal;
    }




    /* --------------------------------------------- */


    /*
     *   Chooses a probability vector a, and repeatedly computes
     *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
     */
    void iterate( int numberOfDocs, int maxIterations ) {

    double[] aCurr = new double[numberOfDocs];
    double[] aNew = new double[numberOfDocs];
    Arrays.fill(aCurr, 1.0 / numberOfDocs);
    Arrays.fill(aNew, 0.0);
    double pageRank;
    int numIter = 0;
    // TODO lägg till en vilkor för att kolla vektorerna
    double diff = Math.abs(manhattan(subtract(aCurr,aNew)));
    while (numIter < maxIterations && diff > EPSILON){

        aNew = multiply(aCurr, p);
        diff = manhattan(subtract(aCurr,aNew));
        // TODO maybe copy needs
        aCurr = aNew;        
        numIter++;
    }

    //System.out.println(aCurr[0] + "," + aCurr[1]);
    double[] aSorted = new double[numberOfDocs];
    aSorted = copyFromTo(aCurr, aSorted);
    Arrays.sort(aSorted);
    aSorted = reverseArray(aSorted);
    System.out.println(aSorted.length == aCurr.length);
    
    int topDocs = 30;
    int docIndex;
    double[] resDocs = new double[topDocs];
    for(int i = 0; i < topDocs;i++){
        docIndex = positionOf(aCurr, aSorted[i]);
        System.out.println(docName[docIndex] + " "  + aCurr[docIndex]);
        //aCurr[i] = -1;

    }
   // System.out.println(Arrsays.toString(resDocs));
}

    public double[] reverseArray(double[] arr){
        for(int i=0; i<arr.length/2; i++){
            double temp = arr[i];
            arr[i] = arr[arr.length -i -1];
            arr[arr.length -i -1] = temp;
          }
        return arr;

    }

    public int positionOf(double[] a, double prob){
        for (int i = 0; i < a.length; i++){
            if(a[i] == prob){
                return i;
            }
        }
        return -1;
    }

    public double[] copyFromTo(double[] aCurr, double[] aNew ){
        for(int i = 0; i < aCurr.length; i++){
            aNew[i] = aCurr[i];
        }
        return aNew;
    }

    // Multiplying a vector with a matrix
    public double[] multiply(double[] x, double[][] a) {
        int m = a.length;
        int n = x.length;
        if (x.length != m) throw new RuntimeException("Illegal matrix dimensions.");
        double[] y = new double[n];
        for (int j = 0; j < n; j++)
            for (int i = 0; i < m; i++)
                y[j] += a[i][j] * x[i];
        return y;
    }

    public static double[] subtract(double[] x,double[] y) {
        if (x.length != y.length)
            throw new IllegalArgumentException("dimensions disagree");
        double[] toReturn = new double[x.length];
        for (int i = 0; i < x.length; i++)
            toReturn[i] = x[i] - y[i];
        return toReturn;
    }

    public static double manhattan(double[] x) {
        double result = 0;
        for (int i = 0; i < x.length; i++)
            result += Math.abs(x[i]);
        return result;
    }

    /* --------------------------------------------- */


    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}
