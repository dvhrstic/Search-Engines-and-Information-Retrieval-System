import java.util.*;
import java.io.*;
/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */
import java.lang.*;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;

    Map<Integer, Double> pageRanks;
    /** Constructor */
    public Searcher( Index index ) {
        this.index = index;
    }

    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */
    public PostingsList search( Query query, QueryType queryType, RankingType rankingType ) {
        // Reads the pageranks file
        pageRanks = new HashMap<Integer, Double>();

        try(BufferedReader br = new BufferedReader(new FileReader("pageranks.txt"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
        
            while (line != null) {
                pageRanks.put(Integer.parseInt(line.split("\\s+")[0]),Double.parseDouble(line.split("\\s+")[1]));
                line = br.readLine();
            }
           // String everything = sb.toString();
        } catch (IOException e) {System.err.println(" File exception");}

        int ij = 0;
        
/*         for (Map.Entry<Integer, Double> rankEntry : pageRanks.entrySet()) {
            if(ij <= 30){System.out.println(rankEntry.getKey() + ": " + rankEntry.getValue());
            ij++;
          }else break;
        } */

        //System.out.println(" Test 337 has " + pageRanks.get(2);
        int wcount = 0;
        PostingsList answer = new PostingsList();
        System.out.print("Search word(s)");
        for (Query.QueryTerm searchQuery : query.queryterm){
            System.out.print("'" + searchQuery.term + "'");
            wcount++;
        }
        System.out.println();
        if (wcount < 2){
            if (index.getPostings(query.queryterm.get(0).term) != null){
                if (queryType == QueryType.RANKED_QUERY){ return singleRanked(index.getPostings(query.queryterm.get(0).term), rankingType);}
                else{return index.getPostings(query.queryterm.get(0).term);}
                }
            else
            {
                System.out.println("There is no such a token");
                return null;
            }
        }else{
            if (queryType == QueryType.RANKED_QUERY){return  multiRanked(query, rankingType);}
            ArrayList<PostingsList> postings = new ArrayList<PostingsList>();
            int i;
            for (i = 0; i < wcount; i++){
                if(index.getPostings(query.queryterm.get(i).term) == null){
                    System.out.println("There is no such a token!");
                    return null;
                }
                postings.add(index.getPostings(query.queryterm.get(i).term));
            }
            if (queryType == QueryType.INTERSECTION_QUERY){
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
                // TODO else if (QUERY_RANK)
                postings.remove(0);
            }
        }
        return answer;
    }
    /**
     * @param query containing all the search words
     * @return sorted PostingsList
     */
    private PostingsList multiRanked(Query query, RankingType rankingType){
        PostingsList rankedPos = new PostingsList();
        double wTq;
        double wFtd;
        for (Query.QueryTerm searchQuery : query.queryterm){
            String queryWord = searchQuery.term;
            PostingsList postingsCurr = index.getPostings(queryWord);
            wTq = Math.log(index.docNames.size() / postingsCurr.getDf());

            System.out.println(" Word: " + queryWord);

            for (int j = 0; j < postingsCurr.size(); j++){
                PostingsEntry postingsEntry = postingsCurr.get(j);
                if (!rankedPos.containsDoc(postingsEntry.docID)){
                    for (int i = 0; i < postingsEntry.size();i++) {
                        rankedPos.addID(postingsEntry.docID, postingsEntry.getOffset(i));
                    }
                }
                    //System.out.println(" Doc id :" + postingsEntry.docID);
                    wFtd = postingsEntry.getTf() * wTq;
                    //System.out.println(" position " + postingsCurr.indexOf(postingsEntry));
                   //rankedPos.get(rankedPos.indexOf(postingsEntry.docID)).score += wTq * wFtd;
                    rankedPos.get(rankedPos.indexOf(postingsEntry.docID)).score += wFtd;
                    //System.out.println(rankedPos.get(postingsCurr.indexOf(postingsEntry)).score);
            }
        }
        for (int k = 0; k < rankedPos.size(); k++){
            // docID is meant for pageRank hashmap in order
            //  to find appropriate document. While rankedPos
            //  uses k to find the respective text.
            int docID = rankedPos.get(k).docID;
            //int rankedDocInx = rankedPos.indexOf(docID);
            if (rankingType == RankingType.TF_IDF){
                rankedPos.get(k).score = rankedPos.get(k).score / index.docLengths.get(rankedPos.get(k).docID);
            }else if (rankingType == RankingType.COMBINATION){
                if (!pageRanks.containsKey(docID)){
                    rankedPos.get(k).score = 0.5 * rankedPos.get(k).score / index.docLengths.get(rankedPos.get(k).docID);    
                }else{
                rankedPos.get(k).score = 0.5 * pageRanks.get(docID) + 0.5 * rankedPos.get(k).score / index.docLengths.get(rankedPos.get(k).docID);
                }
            }else if (rankingType == RankingType.PAGERANK){
                if (!pageRanks.containsKey(docID)){
                    rankedPos.get(k).score = 0.0;
                }else{
                    rankedPos.get(k).score = pageRanks.get(docID);
                }
            }
        }
        rankedPos.sort();
        return rankedPos;
    }


    private PostingsList singleRanked(PostingsList postingsList, RankingType rankingType){
        int N = index.docNames.size();
        int df = postingsList.size();
        int i, tf, doc_len, doc_id;
        int totalTf = 0;
        PostingsEntry p = postingsList.get(0);
        for (i = 0; i < postingsList.size(); i++) {
            p = postingsList.get(i);
            tf = p.getTf();
            totalTf += tf;
            doc_id = p.docID;
            doc_len = index.docLengths.get(doc_id);
            if (rankingType == RankingType.TF_IDF){
                postingsList.get(i).score = tf * Math.log(N/df) / doc_len;
            }else if (rankingType == RankingType.COMBINATION){
                if (!pageRanks.containsKey(doc_id)){
                    postingsList.get(i).score = 0.5 * tf * Math.log(N/df) / doc_len;
                }else{
                postingsList.get(i).score = 0.5 * pageRanks.get(doc_id) + 0.5 * tf * Math.log(N/df) / doc_len;
                }
            } else if (rankingType == RankingType.PAGERANK){
                if (!pageRanks.containsKey(doc_id)){
                    postingsList.get(i).score = 0.0;
                }else{
                postingsList.get(i).score =  pageRanks.get(doc_id);
                }
            }
        }
        postingsList.sort();
        return postingsList;
    }

    private PostingsList phraseIntersection(PostingsList postings1, PostingsList postings2){
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
            // Optimization when there is no possibility for contiguousness
            //if (p1.docID == p2.docID && p1.getOffset(0) < p2.getOffset(0) && p1.getOffset(p1.size()-1) + 1 < p2.getOffset(0)) {
            if (p1.docID == p2.docID) {
                int p1_offsetindex = 0;
                int p2_offsetindex = 0;
                while(p1_offsetindex < p1.size()){
                    while(p2_offsetindex < p2.size()){
                        if (p1.getOffset(p1_offsetindex) + 1 == p2.getOffset(p2_offsetindex)){
                            answer.addID(p1.docID, p2.getOffset(p2_offsetindex));
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
