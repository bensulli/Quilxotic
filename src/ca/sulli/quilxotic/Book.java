package ca.sulli.quilxotic;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Book {

    public int startPage;
    public String title;
    public String author;
    public String email;
    public String website;
    public boolean usesHP;
    public boolean usesCash;
    public String fileName;
    public boolean usesHTML;
    public String tempString;
    public List<Page> pages = new ArrayList<Page>();

}
