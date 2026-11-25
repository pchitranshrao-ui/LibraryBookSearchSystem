package library;

import library.algorithms.Algorithms;
import library.model.Book;
import library.storage.BookStore;
import java.nio.file.*;
import java.util.Arrays;

/** Dependency-free algorithm and persistence regression checks. */
public final class AlgorithmChecks {
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    private static Book b(String id,String title,String author,int year){return new Book(id,title,author,"Fiction",year,true);}
    private static void ordered(Book[] out,String key){for(int i=1;i<out.length;i++)check(Algorithms.cmp(out[i-1],out[i],key)<=0,"not ordered by "+key);}
    public static void main(String[] args)throws Exception {
        Book[] source={b("C","Zebra, \"quoted\"","Zed",2001),b("A","Alpha","Amy",1990),b("B","Alpha","Bob",2010),b("D","Delta","Dee",2000)};
        String[] algos={"Bubble Sort","Selection Sort","Insertion Sort"};
        for(String algo:algos)for(String key:new String[]{"Book ID","Title","Author","Publication Year"}){
            Algorithms.Result r=Algorithms.sort(source,key,algo);ordered(r.books,key);check(r.books.length==source.length,"record loss "+algo);check(source[0].id.equals("C"),"input array mutated");
        }
        check(Algorithms.sort(new Book[0],"Book ID",algos[0]).books.length==0,"empty sort");
        check(Algorithms.sort(new Book[]{b("1","one","A",1)},"Book ID",algos[2]).books.length==1,"single sort");
        check(Algorithms.sort(new Book[]{b("1","x","A",1),b("2","x","B",1)},"Title",algos[2]).books.length==2,"duplicate sort");
        Algorithms.Result reversedBubble=Algorithms.sort(new Book[]{b("C","c","c",3),b("B","b","b",2),b("A","a","a",1)},"Book ID","Bubble Sort");check(reversedBubble.comparisons==3&&reversedBubble.moves==3,"bubble counters");
        Algorithms.Result sortedBubble=Algorithms.sort(new Book[]{b("A","a","a",1),b("B","b","b",2),b("C","c","c",3)},"Book ID","Bubble Sort");check(sortedBubble.comparisons==2&&sortedBubble.moves==0,"bubble early exit counters");
        check(Algorithms.sort(new Book[]{b("C","c","c",3),b("B","b","b",2),b("A","a","a",1)},"Book ID","Selection Sort").comparisons==3,"selection comparisons");
        Algorithms.Result insertion=Algorithms.sort(new Book[]{b("C","c","c",3),b("B","b","b",2),b("A","a","a",1)},"Book ID","Insertion Sort");check(insertion.comparisons==3&&insertion.moves==3,"insertion counters");
        for(boolean binary:new boolean[]{false,true}){
            check(Algorithms.search(source,"A","Book ID",binary).books.length==1,"first/id search");
            check(Algorithms.search(source,"D","Book ID",binary).books.length==1,"last/id search");
            check(Algorithms.search(source,"missing","Title",binary).books.length==0,"missing search");
            check(Algorithms.search(source,"Alpha","Title",binary).books.length==1,"duplicate search key");
            check(Algorithms.search(new Book[0],"x","Title",binary).books.length==0,"empty search");
            Algorithms.Trace trace=Algorithms.searchTrace(source,"D","Book ID",binary);check(!trace.steps.isEmpty()&&trace.result.books.length==1,"search trace");
        }
        for(String algo:algos){Algorithms.Trace trace=Algorithms.sortTrace(source,"Book ID",algo);check(!trace.steps.isEmpty(),"sort trace");check(Arrays.equals(Arrays.stream(trace.result.books).map(x->x.id).toArray(),new String[]{"A","B","C","D"}),"trace output");}
        Book[] large=new Book[500];for(int i=0;i<large.length;i++)large[i]=b(String.format("%04d",large.length-i),"Title "+i,"Author",i);ordered(Algorithms.sort(large,"Book ID","Insertion Sort").books,"Book ID");

        Path dir=Files.createTempDirectory("atlas-store-check");try{Path file=dir.resolve("books.csv");BookStore store=new BookStore(file);check(store.load().length==0,"missing CSV is empty library");Book[] roundtrip={b("X,1","Title, \"special\"","Author",1999)};store.save(roundtrip);Book[] loaded=new BookStore(file).load();check(loaded.length==1&&loaded[0].id.equals("X,1")&&loaded[0].title.equals(roundtrip[0].title),"CSV quoted restart round trip");Files.writeString(file,"id,title,author,category,year,available\nbad,row\n");boolean rejected=false;try{store.load();}catch(java.io.IOException ex){rejected=true;}check(rejected,"malformed CSV surfaced");}finally{try(var paths=Files.walk(dir)){paths.sorted((x,y)->y.compareTo(x)).forEach(x->{try{Files.deleteIfExists(x);}catch(Exception ignored){}});}}
        System.out.println("PASS: algorithm and persistence checks");
    }
}
