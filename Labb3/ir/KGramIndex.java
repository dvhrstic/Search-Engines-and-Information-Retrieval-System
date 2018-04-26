/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Dmytro Kalpakchi, 2018
 */

package ir;

import java.awt.List;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;


public class KGramIndex {

    /** Mapping from term ids to actual term strings */
    HashMap<Integer,String> id2term = new HashMap<Integer,String>();

    /** Mapping from term strings to term ids */
    HashMap<String,Integer> term2id = new HashMap<String,Integer>();

    /** Index from k-grams to list of term ids that contain the k-gram */
    HashMap<String,List<KGramPostingsEntry>> index = new HashMap<String,List<KGramPostingsEntry>>();

    /** The ID of the last processed term */
    int lastTermID = -1;

    /** Number of symbols to form a K-gram */
    int K = 3;

    public KGramIndex(int k) {
        K = k;
        if (k <= 0) {
            System.err.println("The K-gram index can't be constructed for a negative K value");
            System.exit(1);
        }
    }

    /** Generate the ID for an unknown term */
    private int generateTermID() {
        return ++lastTermID;
    }

    public int getK() {
        return K;
    }


    /**
     *  Get intersection of two postings lists
     */
    private List<KGramPostingsEntry> intersect(List<KGramPostingsEntry> p1, List<KGramPostingsEntry> p2) {
       // int numbIterations = (p1.size() > p2.size()) ? p2.size() : p1.size();
        List<KGramPostingsEntry> result = new List<KGramPostingsEntry>();
        for (KGramPostingsEntry posEntry1 : p1){
            int currTokenID = posEntry1.tokenID;
            
            for(KGramPostingsEntry posEntry2 : p2){
                if(posEntry2.tokenID.equals(currTokenID)){
                    result.add(new KGramIndex(id2term.get(currTokenID)));
                    break;
                }
            }
        }
        return result;
    }


    /** Inserts all k-grams from a token into the index. */
    // Started by putting the ^ char int the beginning and $ sign
    //  at the end of a token
    public void insert( String token ) {
        int numKGrams = token.length() + 3 - k;
        token += "$";
        String newToken = "^" + token;
        token = newToken;

        int k = getK();
        String kGram;
        for (int i = 0; i < numKGrams; i++){
            kGram = token.substring(i, i + k);
            // no kGram in the index Map
            if(!this.index.contains(kGram)){
                
                if (!term2id.containsKey(token.substring(1,token.size()-1)))
                term2id.put(token.substring(1,token.size()-1), generateTermID());

                List<KGramPostingsEntry> posEntry = new List<KGramPostingsEntry>();
                posEntry.add(new KGramPostingsEntry(term2id.get(token.substring(1,token.size()-1))));
                index.put(kGram,posEntry);
 

            }else{
                // append the token to a list from index Map
                String tokenToAdd = token.substring(1,token.size()-1);
                boolean tokenAlreadyExist = false;
                for (KGramPostingsEntry posEntry : index.get(kGram)){
                    if(posEntry.tokenID == term2id.get(tokenToAdd)){
                        tokenAlreadyExist = true;
                        break;
                    }
                }if(!tokenAlreadyExist){
                    index.get(kGram).add(tokenToAdd);
                }
            }
        }
    }

    /** Get postings for the given k-gram */
    public List<KGramPostingsEntry> getPostings(String kgram) {
        if(!this.index.contains(kgram)){
            System.err.println(" No postings list for " + kgram);
            return null;
        }else {
            return index.get(kgram);
        }

    }

    /** Get id of a term */
    public Integer getIDByTerm(String term) {
        return term2id.get(term);
    }

    /** Get a term by the given id */
    public String getTermByID(Integer id) {
        return id2term.get(id);
    }

    private static HashMap<String,String> decodeArgs( String[] args ) {
        HashMap<String,String> decodedArgs = new HashMap<String,String>();
        int i=0, j=0;
        while ( i < args.length ) {
            if ( "-p".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("patterns_file", args[i++]);
                }
            }
            else if ( "-f".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("file", args[i++]);
                }
            }
            else if ( "-k".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("k", args[i++]);
                }
            }
            else if ( "-kg".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("kgram", args[i++]);
                }
            }
            else {
                System.err.println( "Unknown option: " + args[i] );
                break;
            }
        }
        return decodedArgs;
    }

    public static void main(String[] arguments) throws FileNotFoundException, IOException {
        HashMap<String,String> args = decodeArgs(arguments);

        int k = Integer.parseInt(args.getOrDefault("k", "3"));
        KGramIndex kgIndex = new KGramIndex(k);

        File f = new File(args.get("file"));
        Reader reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
        Tokenizer tok = new Tokenizer( reader, true, false, true, args.get("patterns_file") );
        while ( tok.hasMoreTokens() ) {
            String token = tok.nextToken();
            kgIndex.insert(token);
        }

        String[] kgrams = args.get("kgram").split(" ");
        List<KGramPostingsEntry> postings = null;
        for (String kgram : kgrams) {
            if (kgram.length() != k) {
                System.err.println("Cannot search k-gram index: " + kgram.length() + "-gram provided instead of " + k + "-gram");
                System.exit(1);
            }

            if (postings == null) {
                postings = kgIndex.getPostings(kgram);
            } else {
                postings = kgIndex.intersect(postings, kgIndex.getPostings(kgram));
            }
        }
        if (postings == null) {
            System.err.println("Found 0 posting(s)");
        } else {
            int resNum = postings.size();
            System.err.println("Found " + resNum + " posting(s)");
            if (resNum > 10) {
                System.err.println("The first 10 of them are:");
                resNum = 10;
            }
            for (int i = 0; i < resNum; i++) {
                System.err.println(kgIndex.getTermByID(postings.get(i).tokenID));
            }
        }
    }
}
