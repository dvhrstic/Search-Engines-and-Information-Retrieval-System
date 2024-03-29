/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.Serializable;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

    public int docID;
    public double score = 0;
    private ArrayList<Integer> offsets = new ArrayList<Integer>();


    /**
     *  PostingsEntries are compared by their score (only relevant
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
	return Double.compare( other.score, score );
    }

    public int getOffset(int i ){
        return offsets.get(i);
    }

    public int size(){
        return offsets.size();
    }

    public double getScore(){
        return score;
    }

    public void addOffset(int offset){
        if (!offsets.contains(offset)) offsets.add(offset);
    }
    //
    // YOUR CODE HERE
    //
}
