/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, KTH, 2018
 */

package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.*;
import java.lang.*;


/*
 *   Implements an inverted index as a hashtable on disk.
 *
 *   Both the words (the dictionary) and the data (the postings list) are
 *   stored in RandomAccessFiles that permit fast (almost constant-time)
 *   disk seeks.
 *
 *   When words are read and indexed, they are first put in an ordinary,
 *   main-memory HashMap. When all words are read, the index is committed
 *   to disk.
 */
public class PersistentHashedIndex implements Index {

    /** The directory where the persistent index files are stored. */
    public static final String INDEXDIR = "./index";

    /** The dictionary file name */
    public static final String DICTIONARY_FNAME = "dictionary";

    /** The dictionary file name */
    public static final String DATA_FNAME = "data";

    /** The terms file name */
    public static final String TERMS_FNAME = "terms";

    /** The doc info file name */
    public static final String DOCINFO_FNAME = "docInfo";

    /** The dictionary hash table on disk can fit this many entries. */
    public static final long TABLESIZE = 611953L;  // 50,000th prime number

    /** The dictionary hash table is stored in this file. */
    RandomAccessFile dictionaryFile;

    /** The data (the PostingsLists) are stored in this file. */
    RandomAccessFile dataFile;

    /** Pointer to the first free memory cell in the data file. */
    long free = 0L;

    /** The cache as a main-memory hash map. */
    HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();

    public int MAX_ENTRYSIZE = 0;
    public int word_count = 0;

    // ===================================================================

    public static final int ENTRYSIZE = 680;
    public static final long DICTIONARYSIZE = ENTRYSIZE * 611953L;


    /**
     *   A helper class representing one entry in the dictionary hashtable.
     */
    public class Entry {
        private String dic_entry;
        private String token;
        private int size;
        private long mem_addres;
        Entry(String word, int size, long mem_addres){
            this.token = word;
            this.size = size;
            this.mem_addres = mem_addres;
            this.dic_entry = word + " " + Integer.toString(size) + " " + Long.toString(mem_addres);
        }
        Entry(String dic_entry){
            this.dic_entry = dic_entry;
            // Maybe should be casted to toString()
            this.token =  dic_entry.split(" ", 3)[0];
            this.size =  Integer.parseInt(dic_entry.split(" ", 3)[1]);
            this.mem_addres =  Long.parseLong(dic_entry.split(" ", 3)[2]);
        }
        public String getEntry(){
            return this.dic_entry;
        }
        public String getKeyWord(){
            return this.token;
        }
        public int getPostingSize(){
            return this.size;
        }
        public long getMemAdress(){
            return this.mem_addres;
        }
    }


    // ==================================================================


    /**
     *  Constructor. Opens the dictionary file and the data file.
     *  If these files don't exist, they will be created.
     */
    public PersistentHashedIndex() {
        try {
            dictionaryFile = new RandomAccessFile( INDEXDIR + "/" + DICTIONARY_FNAME, "rw" );
            dataFile = new RandomAccessFile( INDEXDIR + "/" + DATA_FNAME, "rw" );
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
        try {
            readDocInfo();
        }
        catch ( FileNotFoundException e ) {
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     *  Writes data to the data file at a specified place.
     *
     *  @return The number of bytes written.
     */
    int writeData( String dataString, long ptr ) {
        try {
            dataFile.seek( ptr );
            byte[] data = dataString.getBytes();
            dataFile.write( data );
            return data.length;
        }
        catch ( IOException e ) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     *  Reads data from the data file
     */
    String readData( long ptr, int size ) {
        try {
            dataFile.seek( ptr );
            byte[] data = new byte[size];
            dataFile.readFully( data );
            return new String(data);
        }
        catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }


    // ==================================================================
    //
    //  Reading and writing to the dictionary file.

    /*
     *  Writes an entry to the dictionary hash table file.
     *
     *  @param entry The key of this entry is assumed to have a fixed length
     *  @param ptr   The place in the dictionary file to store the entry
     */
    void writeEntry( Entry entry, long ptr ) {
    word_count+=1;
    if (word_count % 1000 == 0){
        System.out.println(" Word count " + word_count);
    }
    try{
        dictionaryFile.seek(ptr);
        dictionaryFile.writeBytes(entry.getEntry());
        //System.out.println(" Writing : " +  entry.getEntry() + " to adress: " + ptr);

        if (ENTRYSIZE-entry.getEntry().getBytes().length < 0){
            System.err.println(" ENTRYSIZE too small!!!");
            System.exit(0);
        }
        dictionaryFile.skipBytes(ENTRYSIZE-entry.getPostingSize());
    }catch ( IOException e ) {
            e.printStackTrace();
        }
    }
    /**
     *  Reads an entry from the dictionary file.
     *
     *  @param ptr The place in the dictionary file where to start reading.
     */
    Entry readEntry( long ptr ) {
        byte[] b = new byte[ENTRYSIZE];
        String s;
        Entry entry = null;
        try{
            dictionaryFile.seek(ptr);
            dictionaryFile.readFully(b);
            s = new String(b);
            System.out.println(" Reading " + s +  " from " + ptr);
            entry = new Entry(s);
        }catch ( IOException e ) {
            e.printStackTrace();
        }
        return entry;
    }


    // ==================================================================

    /**
     *  Writes the document names and document lengths to file.
     *
     * @throws IOException  { exception_description }
     */
    private void writeDocInfo() throws IOException {
        FileOutputStream fout = new FileOutputStream( INDEXDIR + "/docInfo" );
        for (Map.Entry<Integer,String> entry : docNames.entrySet()) {
            Integer key = entry.getKey();
            String docInfoEntry = key + ";" + entry.getValue() + ";" + docLengths.get(key) + "\n";
            fout.write(docInfoEntry.getBytes());
        }
        fout.close();
    }


    /**
     *  Reads the document names and document lengths from file, and
     *  put them in the appropriate data structures.
     *
     * @throws     IOException  { exception_description }
     */
    private void readDocInfo() throws IOException {
        File file = new File( INDEXDIR + "/docInfo" );
        FileReader freader = new FileReader(file);
        try (BufferedReader br = new BufferedReader(freader)) {
            String line;
            while ((line = br.readLine()) != null) {
               String[] data = line.split(";");
               docNames.put(new Integer(data[0]), data[1]);
               docLengths.put(new Integer(data[0]), new Integer(data[2]));
            }
        }
        freader.close();
    }


    /**
     *  Write the index to files.
     */

    // public int hashFunc(String token) {
    //     // In order to only have positive values
    //     int hash = (token.hashCode() & 0x7fffffff) % (int) (long) TABLESIZE;
    //     int value = -1;
    //     byte[] b = new byte[ENTRYSIZE];
    //     //hash = (int) (long) TABLESIZE;
    //     try{
    //     dictionaryFile.seek(hash * ENTRYSIZE);
    //     value = dictionaryFile.read(b,0,ENTRYSIZE);
    //     int sum = 0;
    //     for(byte s : b){
    //         sum += s;
    //     }
    //     System.out.println( "sum "+ sum);
    //     System.out.println(" Value" + value);
    //     while(value != 0){
    //         hash += ENTRYSIZE;
    //         dictionaryFile.seek(hash * ENTRYSIZE);
    //         value = dictionaryFile.read(b,0,ENTRYSIZE);
    //         }
    //     }
    //     // End of file, start from the begining
    //     catch (IOException e){
    //     System.out.println("In a catch where hash is " + hash + value);
    //     hash = 0;
    //     while (value != 0){
    //         hash += ENTRYSIZE;
    //         try {
    //         dictionaryFile.seek(hash * ENTRYSIZE);
    //         value = dictionaryFile.read(b,0,ENTRYSIZE);
    //         System.out.println(" In the end value : " + value);
    //         }
    //         catch (IOException ioe) {System.err.println(" --NO MORE SPACE FOR ENTRIES--");}
    //         }
    //     }
    //     return hash;
    // }

//    public static int hashFunc(String str) {
//        int hash = 5381;
//
//        //long range = (long) Math.ceil((long)TABLESIZE);
//        for (int i = 0; i < str.length(); i++)
//            hash = (str.charAt(i) + ((hash << 5) - hash)) % (int)(long) TABLESIZE;
//        return hash;
//    }

    public int hashFunc(String token){
        int hash = (token.hashCode() & 0x7fffffff) % (int) (long) TABLESIZE;
        if (hash == (int)(long) TABLESIZE){
            hash = 31;
        }
        return hash * ENTRYSIZE;
    }

    public void writeIndex() {
        int collisions = 0;
        int token_loc = 0;
        long curr_mem_adr = 0;
        int posting_size;
        Entry entry;
        String entryString;
        StringBuilder postings_string;
        PostingsList pos_list;
        String token;
        byte[] b = new byte[ENTRYSIZE];
        try {
            // Write the 'docNames' and 'docLengths' hash maps to a file
            writeDocInfo();
            // Write the dictionary and the postings list
            dictionaryFile.setLength(DICTIONARYSIZE);
            for (Map.Entry<String, PostingsList> pair : index.entrySet()) {
                token = pair.getKey();
                pos_list = pair.getValue();
                postings_string = new StringBuilder();
                token_loc = hashFunc(token);
                dictionaryFile.seek(token_loc);
                dictionaryFile.readFully(b);
                entryString = new String(b);
                //if (word_count == 50){System.exit(0);}
                //System.out.println(" String with length " + entryString.length() + " is " + entryString);
                //System.out.println(" All white spaces " + (entryString.trim().length() == 0));
                while (token_loc >= (int)(long)DICTIONARYSIZE - ENTRYSIZE || entryString.trim().length() != 0) {
                    if (token_loc >= (int)(long)DICTIONARYSIZE - ENTRYSIZE){
                        token_loc = 0;
                    }else{
                        token_loc += ENTRYSIZE;
                    }
                    dictionaryFile.seek(token_loc);
                    //System.out.println(" Value inside while " + value + " with hash " + token_loc);
                    dictionaryFile.readFully(b);
                    entryString = new String(b);
                    collisions+=1;
                    //System.out.println(entryString.trim().length());
                }
                for (int i = 0; i < pos_list.size(); i ++){
                    //System.out.print(pos_list.get(i).docID + " ---> ");
                    postings_string.append(pos_list.get(i).docID);
                    for (int j = 0; j < pos_list.get(i).size(); j++){
                        //System.out.print(" " + pos_list.get(i).getOffset(j));
                        postings_string.append(" " + pos_list.get(i).getOffset(j));
                    }
                    postings_string.append("\n");
                }
                //System.out.println(postings_string.toString());
                posting_size = writeData(postings_string.toString(), curr_mem_adr);
                entry = new Entry(token, posting_size, curr_mem_adr);
                //System.out.println(" ENTRY " + entry.getEntry());
                int curr_entrysize = entry.getEntry().getBytes().length;
                if(curr_entrysize > MAX_ENTRYSIZE){
                    MAX_ENTRYSIZE = curr_entrysize;
                }
                writeEntry(entry, token_loc);
                curr_mem_adr += posting_size;
                //System.out.println(" curr_mem_adr " + curr_mem_adr + " token_loc " + token_loc);
            }
        }catch ( IOException e) {
            System.err.println(" Token loc " + token_loc + " DICTIONARYSIZE " + DICTIONARYSIZE + (token_loc == (int)(long) DICTIONARYSIZE));
            e.printStackTrace();
        }

        System.err.println( collisions + " collisions." );


    }


    // ==================================================================


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        Entry entry;
        long mem_adress;
        int hash = hashFunc( token );
        String posting_string;
        Scanner scanner;
        Scanner scanner2;
        String line;
        String line2;
        entry = readEntry( hash * ENTRYSIZE );
        int i;
        int doc_id;
        PostingsList postingsList = new PostingsList();
        while(entry.token != token){
            hash += ENTRYSIZE;
            entry = readEntry( hash * ENTRYSIZE );
        }
        posting_string = readData(entry.getMemAdress(), entry.getPostingSize());
        scanner = new Scanner(posting_string);
        while (scanner.hasNextLine()) {
            line = scanner.next();
            i = 0;
            doc_id = 0;
            scanner2 = new Scanner(line);
            while(scanner2.hasNext()){
                if (i == 0){
                    doc_id = Integer.parseInt(scanner2.nextLine());
                }else{
                    postingsList.addID( doc_id, Integer.parseInt(scanner2.nextLine()));
                }
            }
        }
        scanner.close();
        return postingsList;
    }


    /**
     *  Inserts this token in the main-memory hashtable.
     */
    public void insert( String token, int docID, int offset ) {
    if (index.containsKey(token)){
        index.get(token).addID(docID, offset);
    }else{
        PostingsList postingsList = new PostingsList();
        postingsList.addID(docID, offset);
        index.put(token, postingsList);
        }
    }


    /**
     *  Write index to file after indexing is done.
     */
    public void cleanup() {
        System.err.println( index.keySet().size() + " unique words" );
        System.err.print( "Writing index to disk..." );
        long start = System.currentTimeMillis();
        writeIndex();
        long exec_time = System.currentTimeMillis() - start;
        System.out.println(" Max entry size " + MAX_ENTRYSIZE);
        System.out.println(" Time(sec): " + exec_time/1000);
        System.err.println( "done!" );
     }

}
