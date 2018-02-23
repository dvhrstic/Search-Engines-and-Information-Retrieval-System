/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;

import java.util.ArrayList;

public class PostingsList {

    /** The postings list */
    private ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();

    /** Number of postings in this list. */
    public int size() {
	return list.size();
    }

    /** Returns the ith posting. */
    public PostingsEntry get( int i ) {
	return list.get( i );
    }

    public void addID(int docID, int offset){
        if ( list.isEmpty()){
            PostingsEntry postingsEntry = new PostingsEntry();
            postingsEntry.docID = docID;
            postingsEntry.addOffset(offset);
            list.add(postingsEntry);
        }else if(list.get(list.size()-1).docID != docID ){
            PostingsEntry postingsEntry = new PostingsEntry();
            postingsEntry.docID = docID;
            postingsEntry.addOffset(offset);
            list.add(postingsEntry);
        }
        list.get(list.size()-1).addOffset(offset);
    }
}
