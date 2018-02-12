/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;
import java.util.Collections;
import java.util.Comparator;
//import java.util.*;
import java.util.Iterator;
import java.util.ArrayList;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;

    /** Constructor */
    public Searcher( Index index ) {
        this.index = index;
    }

    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */
    public PostingsList search( Query query, QueryType queryType, RankingType rankingType ) {
        //System.out.println();
        int wcount = 0;
        PostingsList answer = new PostingsList();
        for (Query.QueryTerm searchQuery : query.queryterm){
            System.out.print("Search word(s): " + "'" + searchQuery.term + "'");
            wcount++;
        }
        System.out.println();
        if (wcount < 2){
            return index.getPostings(query.queryterm.get(0).term);
        }else{
            ArrayList<PostingsList> postings = new ArrayList<PostingsList>();
            int i;
            for (i = 0; i < wcount; i++){
                postings.add(index.getPostings(query.queryterm.get(i).term));
            }
            if (queryType == QueryType.INTERSECTION_QUERY){}
            Collections.sort(postings, new Comparator<PostingsList>() {
                @Override
            public int compare(PostingsList p1, PostingsList p2) {
            return Integer.valueOf(p1.size()).compareTo(p2.size());
                }
            });
            }
            answer = postings.get(0);
            postings.remove(0);
            while( !postings.isEmpty() && answer.size() != 0){
                System.out.println();
                if (queryType == QueryType.INTERSECTION_QUERY) answer = intersection(answer, postings.get(0));
                else if (queryType == QueryType.PHRASE_QUERY) answer = phraseIntersection(answer, postings.get(0));
                postings.remove(0);
            }
        }
        return answer;
    }


    public PostingsList phraseIntersection(PostingsList postings1, PostingsList postings2){
        int i1 = 0;
        int i2 = 0;
        PostingsList answer = new PostingsList();
        int i;
        PostingsEntry p1 = new PostingsEntry();
        PostingsEntry p2 = new PostingsEntry();
        System.out.println();
        while(i1 < postings1.size() && i2 < postings2.size()){
            //System.out.println("First: " +  i1 +  " second: " + i2);
            p1 = postings1.get(i1);
            p2 = postings2.get(i2);
            // Optimization when there is no possiblity for contiguousness
            //if (p1.docID == p2.docID && p1.getOffset(0) < p2.getOffset(0) && p1.getOffset(p1.size()-1) + 1 < p2.getOffset(0)) {
            if (p1.docID == p2.docID) {
                int p1_offsetindex = 0;
                int p2_offsetindex = 0;
                while(p1_offsetindex < p1.size()){
                    while(p2_offsetindex < p2.size()){
                        if (p1.getOffset(p1_offsetindex) + 1 == p2.getOffset(p2_offsetindex)){
                            System.out.println("Doc " + p1.docID);
                            //int i;
                            System.out.print("Posting 1");
                            for (i = 0; i < p1.size(); i++){System.out.print(" " + p1.getOffset(i));}
                            System.out.println();
                            System.out.print("Posting 2");
                            for (i = 0; i < p2.size(); i++){System.out.print(" "  + p2.getOffset(i));}
                            answer.addID(p1.docID, p2.getOffset(p2_offsetindex));
                            //p2_offsetindex++;
                        }
                        else if (p2.getOffset(p2_offsetindex) > p1.getOffset(p1_offsetindex)) {
                            break;
                        }
                        p2_offsetindex++;
                    }
                    p1_offsetindex++;
                }
                i1++;
                i2++;
            }else if (p1.docID < p2.docID){
                i1++;
            }else{
                i2++;
            }
        }
        return answer;
    }


   //  public PostingsList phraseIntersection(PostingsList postings1, PostingsList postings2){
   //      int i1 = 0;
   //      int i2 = 0;
   //      PostingsList answer = new PostingsList();
   //      int i;
   //      PostingsEntry p1 = new PostingsEntry();
   //      PostingsEntry p2 = new PostingsEntry();
   //      System.out.println();
   //      while(i1 < postings1.size() && i2 < postings2.size()){
   //          //System.out.println("First: " +  i1 +  " second: " + i2);
   //          p1 = postings1.get(i1);
   //          p2 = postings2.get(i2);
   //          // Optimization when there is no possiblity for contiguousness
   //          //if (p1.docID == p2.docID && p1.getOffset(0) < p2.getOffset(0) && p1.getOffset(p1.size()-1) + 1 < p2.getOffset(0)) {
   //          if (p1.docID == p2.docID) {
   //              PostingsEntry p_new = new PostingsEntry();
   //              p_new = positionalEntryIntersect(p1,p2,p1.docID);
   //              for (int j = 0; j < p_new.size();j++){
   //                  answer.addID(p1.docID, p_new.getOffset(j));
   //              }
   //
   //          }else if (p1.docID < p2.docID){
   //              i1++;
   //          }else{
   //              i2++;
   //          }
   //      }
   //      return answer;
   //  }
   //
   //
   //
   //
   //  private PostingsEntry positionalEntryIntersect(PostingsEntry pp1, PostingsEntry pp2, int docID){
   //     PostingsEntry result = new PostingsEntry();
   //     Iterator<Integer> i1 = pp1.iterator();
   //     Iterator<Integer> i2 = pp2.iterator();
   //     int offset = 1;
   //
   //     if(i1.hasNext() && i2.hasNext())
   //     {
   //         Integer pos1 = i1.next();
   //         Integer pos2 = i2.next();
   //
   //         while(true)
   //         {
   //             Integer diff = pos2 - pos1;
   //
   //             if(diff.intValue() == offset){
   //                 result.docID = docID;
   //                 result.addOffset(pos2);
   //                 if(i1.hasNext() && i2.hasNext()){
   //                     pos1 = i1.next();
   //                     pos2 = i2.next();
   //                 }
   //                 else
   //                     break;
   //             }
   //             else if(diff.intValue() > offset){
   //                 if(i1.hasNext())
   //                     pos1 = i1.next();
   //                 else break;
   //
   //             }
   //             else{
   //                 if(i2.hasNext())
   //                     pos2 = i2.next();
   //                 else break;
   //            }
   //
   //         }
   //     }
   //     return result;
   // }
   //



    public PostingsList intersection(PostingsList postings1, PostingsList postings2){
        //System.out.println("Query words: " + query.queryterm.get(0).term + " and " + query.queryterm.get(1).term);
        int i1 = 0;
        int i2 = 0;
        PostingsList answer = new PostingsList();
        PostingsEntry p1 = postings1.get(i1);
        PostingsEntry p2 = postings2.get(i2);
        while(i1 < postings1.size() && i2 < postings2.size()){
            //System.out.println("First: " +  i1 +  " second: " + i2);
            p1 = postings1.get(i1);
            p2 = postings2.get(i2);
            if (p1.docID == p2.docID) {
                // The offset irrelevant in this situation
                answer.addID(p1.docID, p1.getOffset(0));
                i1++;
                i2++;
            }else if (p1.docID < p2.docID){
                i1++;
            }else{
                i2++;
            }
        }
        return answer;
    }
}
