package library.model;
public class Book {
 public String id,title,author,category; public int year; public boolean available;
 public Book(String id,String title,String author,String category,int year,boolean available){this.id=id;this.title=title;this.author=author;this.category=category;this.year=year;this.available=available;}
 public Book copy(){return new Book(id,title,author,category,year,available);}
}
