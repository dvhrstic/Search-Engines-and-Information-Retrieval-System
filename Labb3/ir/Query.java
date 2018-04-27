/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */
package ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.nio.charset.*;
import java.io.*;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.*;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;


/**
 *  A class for representing a query as a list of words, each of which has
 *  an associated weight.
 */
public class Query {

    /**
     *  Help class to represent one query term, with its associated weight. 
     */
    class QueryTerm {
	String term;
	double weight;
	QueryTerm( String t, double w ) {
	    term = t;
	    weight = w;
	}
    }

    /** 
     *  Representation of the query as a list of terms with associated weights.
     *  In assignments 1 and 2, the weight of each term will always be 1.
     */
    public ArrayList<QueryTerm> queryterm = new ArrayList<QueryTerm>();

    /**  
     *  Relevance feedback constant alpha (= weight of original query terms). 
     *  Should be between 0 and 1.
     *  (only used in assignment 3).
     */
    double alpha = 0.5;

    /**  
     *  Relevance feedback constant beta (= weight of query terms obtained by
     *  feedback from the user). 
     *  (only used in assignment 3).
     */
    double beta = 1 - alpha;
    
    
    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
    
    
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
	StringTokenizer tok = new StringTokenizer( queryString );
	while ( tok.hasMoreTokens() ) {
	    queryterm.add( new QueryTerm(tok.nextToken(), 1.0) );
	}    
    }
    
    
    /**
     *  Returns the number of terms
     */
    public int size() {
	return queryterm.size();
    }
    
    
    /**
     *  Returns the Manhattan query length
     */
    public double length() {
	double len = 0;
	for ( QueryTerm t : queryterm ) {
	    len += t.weight; 
	}
	return len;
    }
    
    
    /**
     *  Returns a copy of the Query
     */
    public Query copy() {
	Query queryCopy = new Query();
	for ( QueryTerm t : queryterm ) {
	    queryCopy.queryterm.add( new QueryTerm(t.term, t.weight) );
	}
	return queryCopy;
    }
    
    
    /**
     *  Expands the Query using Relevance Feedback
     *
     *  @param results The results of the previous query.
     *  @param docIsRelevant A boolean array representing which query results the user deemed relevant.
     *  @param engine The search engine object
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Engine engine ) {
        
        int numRel = 0;
        int docID = 0;
        //HashMap<String, Double> term2weight = new HashMap<String, Double>();
        for (boolean isRel : docIsRelevant){
           
            if(isRel){
                //String currentDir = System.getProperty("user.dir") + "\\davisWiki\\";
                //System.out.println(currentDir);
                String docName = engine.index.docNames.get(results.get(docID).docID);
                //String totalPath = currentDir + docName;
                System.out.println(docName);
                File f = new File(docName);
                processFiles(f ,engine);
                numRel++;
            }
            docID++;
        }
        double initialSum = 0.0;
        for (QueryTerm query : queryterm) {
            //term2weight.put(query.term, query.weight);
            initialSum += query.weight;
        }

        for(QueryTerm query: queryterm){
            query.weight *= alpha / initialSum;
            //term2weight.put(query.term, query.weight);
        }
        
        double idf;
        double tf;
        PostingsList termPosList;
        for (int i = 0; i < docIsRelevant.length; i++){
        
         if(docIsRelevant[i]){
        
            int relDoc = results.get(i).docID;     
        
            for (QueryTerm query : queryterm){  
                //System.out.println(query.term);          
                //System.out.println("Term : " + query.term);
                termPosList = engine.index.getPostings(query.term);
                idf = Math.log(engine.index.docNames.size() / termPosList.getDf());
            
                for(int k = 0; k <  termPosList.size(); k++){
                    if(termPosList.get(k).docID == relDoc){
                        tf = results.get(results.indexOf(termPosList.get(k).docID)).getTf(); 
                        query.weight += beta * (idf * tf) / engine.index.docLengths.get(relDoc);
                        //term2weight.put(query.term, query.weight);
                        //query.weight += beta * (idf * tf);
                    }
                }
            }
        }
    }

    double weightSum = 0.0; 
    for (QueryTerm query : queryterm){
        //Normalization of each entry
        if (numRel != 0)query.weight /= numRel;
        weightSum += query.weight;
    }

    for(int j = 0; j < queryterm.size(); j++){
        queryterm.get(j).weight /= weightSum;
    }

    }

    public void processFiles( File f, Engine engine) {
        // do not try to index fs that cannot be read

        if ( f.canRead() ) {
        
            // First register the document and get a docID
            try {
                //  Read the first few bytes of the file to see if it is
                // likely to be a PDF
                Reader reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
                char[] buf = new char[4];
                reader.read( buf, 0, 4 );
                reader.close();
                if ( buf[0] == '%' && buf[1]=='P' && buf[2]=='D' && buf[3]=='F' ) {
                // We assume this is a PDF file
                try {
                    String contents = engine.indexer.extractPDFContents( f );
                    reader = new StringReader( contents );
                }
                catch ( IOException e ) {
                    // Perhaps it wasn't a PDF file after all
                    reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
                }
                }
                else {
                // We hope this is ordinary text
                reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
                }
                Tokenizer tok = new Tokenizer( reader, true, false, true, engine.indexer.patterns_file );
                int offset = 0;
                while ( tok.hasMoreTokens() ) {
                    boolean containsTerm = false;
                    String token = tok.nextToken();
                    for(QueryTerm qt : this.queryterm){
                        if(qt.term.equals(token)){
                            containsTerm = true;
                            break;
                        }
                    }
                    if(!containsTerm){
                        this.queryterm.add(new QueryTerm (token, 0.0));
                        //System.out.println(token);
                    }

                }

                reader.close();
            }
            catch ( IOException e ) {
                System.err.println( "Warning: IOException during indexing." );
            }
            }else{
                System.err.println("NOT GOOD");
            }
        
        }


}


