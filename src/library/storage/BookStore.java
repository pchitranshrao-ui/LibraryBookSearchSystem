package library.storage;

import library.model.Book;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/** Local UTF-8 CSV storage with quoted-field support and atomic replacement. */
public class BookStore {
    private final Path file;
    public BookStore(Path file) { this.file=file; }

    public Book[] load() throws IOException {
        if(!Files.exists(file)) return new Book[0];
        List<Book> books=new ArrayList<>(); int lineNo=0;
        try(BufferedReader reader=Files.newBufferedReader(file)) {
            String line;
            while((line=reader.readLine())!=null){lineNo++;if(lineNo==1&&line.startsWith("id,"))continue;if(line.isBlank())continue;
                try { List<String> f=parse(line); if(f.size()!=6)throw new IllegalArgumentException("expected 6 fields");
                    int year=Integer.parseInt(f.get(4));if(year<0||year>2100)throw new IllegalArgumentException("year out of range");
                    if(!f.get(5).equalsIgnoreCase("true")&&!f.get(5).equalsIgnoreCase("false"))throw new IllegalArgumentException("invalid availability");
                    if(f.get(0).isBlank()||f.get(1).isBlank()||f.get(2).isBlank())throw new IllegalArgumentException("missing required value");
                    books.add(new Book(f.get(0),f.get(1),f.get(2),f.get(3),year,Boolean.parseBoolean(f.get(5))));
                } catch(RuntimeException ex) { throw new IOException("Invalid CSV data at line "+lineNo+": "+ex.getMessage(),ex); }
            }
        }
        return books.toArray(new Book[0]);
    }
    public void save(Book[] books)throws IOException {
        Path parent=file.toAbsolutePath().getParent();if(parent!=null)Files.createDirectories(parent);
        Path tmp=file.resolveSibling(file.getFileName()+".tmp");
        try(BufferedWriter w=Files.newBufferedWriter(tmp)) {w.write("id,title,author,category,year,available\n");for(Book b:books){w.write(csv(b.id)+","+csv(b.title)+","+csv(b.author)+","+csv(b.category)+","+b.year+","+b.available+"\n");}}
        try{Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);}
    }
    private String csv(String s){String v=s==null?"":s.replace("\r\n"," ").replace('\r',' ').replace('\n',' ');return v.indexOf(',')>=0||v.indexOf('"')>=0?'"'+v.replace("\"","\"\"")+'"':v;}
    private List<String> parse(String line){List<String> f=new ArrayList<>();StringBuilder b=new StringBuilder();boolean quoted=false;for(int i=0;i<line.length();i++){char c=line.charAt(i);if(quoted){if(c=='"'&&i+1<line.length()&&line.charAt(i+1)=='"'){b.append('"');i++;}else if(c=='"')quoted=false;else b.append(c);}else if(c==',' ){f.add(b.toString());b.setLength(0);}else if(c=='"'&&b.length()==0)quoted=true;else b.append(c);}if(quoted)throw new IllegalArgumentException("unclosed quoted field");f.add(b.toString());return f;}
}
