package library.algorithms;

import library.model.Book;
import java.util.ArrayList;
import java.util.List;

/** Array-based textbook algorithms plus trace events emitted by the real loops. */
public final class Algorithms {
    private Algorithms() {}

    public static final class Result {
        public final Book[] books;
        public final long comparisons, moves, nanos;
        public Result(Book[] books, long comparisons, long moves, long nanos) {
            this.books = books; this.comparisons = comparisons; this.moves = moves; this.nanos = nanos;
        }
    }

    public static final class Step {
        public final Book[] state;
        public final int first, second, low, mid, high, found;
        public final long comparisons, moves;
        public final String description;
        Step(Book[] a, int first, int second, int low, int mid, int high, int found,
             long comparisons, long moves, String description) {
            state = a.clone(); this.first=first; this.second=second; this.low=low; this.mid=mid;
            this.high=high; this.found=found; this.comparisons=comparisons; this.moves=moves;
            this.description=description;
        }
    }
    public static final class Trace { public final List<Step> steps; public final Result result;
        Trace(List<Step> steps, Result result) { this.steps=steps; this.result=result; } }

    private static Book[] copy(Book[] in) { Book[] out=new Book[in.length]; for(int i=0;i<in.length;i++)out[i]=in[i].copy(); return out; }
    private static final int MAX_TRACE_STEPS=5000;
    private static void add(List<Step> s,Book[] a,int i,int j,long c,long m,String d) { if(s!=null&&s.size()<MAX_TRACE_STEPS)s.add(new Step(a,i,j,-1,-1,-1,-1,c,m,d)); }

    public static Result sort(Book[] input,String key,String algo) { return runSort(input,key,algo,false).result; }
    public static Trace sortTrace(Book[] input,String key,String algo) { return runSort(input,key,algo,true); }
    private static Trace runSort(Book[] input,String key,String algo,boolean record) {
        Book[] a=copy(input); List<Step> steps=record?new ArrayList<>():null; long c=0,m=0,t=System.nanoTime();
        if(algo.equals("Bubble Sort")) {
            for(int i=0;i<a.length;i++){boolean changed=false;for(int j=0;j<a.length-i-1;j++){c++;add(steps,a,j,j+1,c,m,"Compare positions "+j+" and "+(j+1));if(cmp(a[j],a[j+1],key)>0){Book x=a[j];a[j]=a[j+1];a[j+1]=x;m++;changed=true;add(steps,a,j,j+1,c,m,"Swap positions "+j+" and "+(j+1));}}if(!changed)break;}
        } else if(algo.equals("Selection Sort")) {
            for(int i=0;i<a.length;i++){int min=i;for(int j=i+1;j<a.length;j++){c++;add(steps,a,min,j,c,m,"Compare current minimum with position "+j);if(cmp(a[j],a[min],key)<0)min=j;}if(min!=i){Book x=a[i];a[i]=a[min];a[min]=x;m++;add(steps,a,i,min,c,m,"Place minimum at position "+i);}}
        } else {
            for(int i=1;i<a.length;i++){Book x=a[i];int j=i-1;while(j>=0){c++;add(steps,a,j,i,c,m,"Compare key at position "+i+" with position "+j);if(cmp(a[j],x,key)<=0)break;a[j+1]=a[j];m++;add(steps,a,j,j+1,c,m,"Shift book from "+j+" to "+(j+1));j--;}a[j+1]=x;add(steps,a,j+1,i,c,m,"Insert key at position "+(j+1));}
        }
        if(record){if(steps.isEmpty())add(steps,a,-1,-1,c,m,"No comparisons required");else if(steps.size()==MAX_TRACE_STEPS)steps.add(new Step(a,-1,-1,-1,-1,-1,-1,c,m,"Trace checkpoint limit reached; algorithm completed"));}
        return new Trace(steps,new Result(a,c,m,System.nanoTime()-t));
    }
    public static int cmp(Book a,Book b,String key){switch(key){case "Book ID":return a.id.compareToIgnoreCase(b.id);case "Title":return a.title.compareToIgnoreCase(b.title);case "Author":return a.author.compareToIgnoreCase(b.author);default:return Integer.compare(a.year,b.year);}}

    public static Result search(Book[] input,String term,String key,boolean binary){return searchTrace(input,term,key,binary).result;}
    public static Trace searchTrace(Book[] input,String term,String key,boolean binary) {
        List<Step> steps=new ArrayList<>();long c=0,t=System.nanoTime();Book[] a=copy(input);if(binary)a=sort(a,key,"Insertion Sort").books;
        int found=-1;
        if(binary){int lo=0,hi=a.length-1;while(lo<=hi){int mid=(lo+hi)>>>1;c++;steps.add(new Step(a,mid,mid,lo,mid,hi,-1,c,0,"Compare midpoint "+mid+" in range "+lo+"–"+hi));int d=cmpTerm(a[mid],term,key);if(d==0){found=mid;steps.add(new Step(a,mid,mid,lo,mid,hi,mid,c,0,"Match found at position "+mid));break;}if(d<0)lo=mid+1;else hi=mid-1;}}
        else for(int i=0;i<a.length;i++){c++;steps.add(new Step(a,i,-1,i,i,i,-1,c,0,"Examine position "+i));if(cmpTerm(a[i],term,key)==0){found=i;steps.add(new Step(a,i,-1,i,i,i,i,c,0,"Match found at position "+i));break;}}
        if(steps.isEmpty()||found<0)steps.add(new Step(a,-1,-1,-1,-1,-1,-1,c,0,a.length==0?"No books to search":"No matching book found"));
        return new Trace(steps,new Result(found<0?new Book[0]:new Book[]{a[found]},c,0,System.nanoTime()-t));
    }
    private static int cmpTerm(Book b,String s,String key){if(key.equals("Book ID"))return b.id.compareToIgnoreCase(s);if(key.equals("Title"))return b.title.compareToIgnoreCase(s);if(key.equals("Author"))return b.author.compareToIgnoreCase(s);return 1;}
}
