/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

import java.util.ArrayList;
import java.util.*;

public class PostingsList{

    /** The postings list */
    public ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();

    /** Number of postings in this list. */
    public int size() {
	return list.size();
    }

    public int getDf() {return this.size();}

    // Checks whether there is a postingsEntry in a list
    public boolean containsDoc(int docID){
        boolean containsDocID = false;
        for (PostingsEntry posEntry : list){
            if (posEntry.docID == docID){
                containsDocID = true;
            }
        }
        return containsDocID;
    }
    /** Returns the ith posting. */
    public PostingsEntry get( int i ) {
        
        return list.get( i );
    }

    public int indexOf(int docID){
        int index = -1;
        for(int i = 0; i < list.size(); i++){
            if (docID == list.get(i).docID){
                index = i;
                return index;
            }
        }
        return index;
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

    public void sort(){
        System.out.println(list.get(0).size());
        Collections.sort(list, new Comparator<PostingsEntry>() {
            @Override
            public int compare(PostingsEntry pp1, PostingsEntry pp2) {
                return pp1.compareTo(pp2);
            }
        });
        System.out.println(list.get(0).size());
    }
}
