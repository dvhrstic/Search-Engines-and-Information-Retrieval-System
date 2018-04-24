/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */
package ir;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.nio.charset.*;
import java.io.*;


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
        for (boolean isRel : docIsRelevant){
            if(isRel)numRel++;
        }

        for(QueryTerm query: queryterm){
            query.weight *= alpha;
        }
        
        double idf;
        double tf;
        PostingsList termPosList;
        for (int i = 0; i < docIsRelevant.length; i++){
        
         if(docIsRelevant[i]){
        
            int relDoc = results.get(i).docID;     
        
            for (QueryTerm query : queryterm){            
                termPosList = engine.index.getPostings(query.term);
                idf = Math.log(engine.index.docNames.size() / termPosList.getDf());
            
                for(int k = 0; k <  termPosList.size(); k++){
                    if(termPosList.get(k).docID == relDoc){
                        tf = results.get(results.indexOf(termPosList.get(k).docID)).getTf(); 
                        query.weight += beta * (idf * tf) / numRel;
                    }
                }
            }
        }
    }

    double weightSum = 0.0; 
    for (QueryTerm query : queryterm){
        weightSum += query.weight;
    }

    for(int j = 0; j < queryterm.size(); j++){
        queryterm.get(j).weight /= weightSum;
    }

    }
}


