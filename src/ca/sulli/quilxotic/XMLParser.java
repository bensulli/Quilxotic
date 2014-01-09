package ca.sulli.quilxotic;


import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;

public class XMLParser {

    public static int readLimit = 5000; // Used to kill a loop if it clearly isn't advancing
    public Book book;

    public Book ParseBook(Book book)
    {

        this.book = book;

        /* READ XML BOOK */
        XmlPullParser parser;
        try {
            parser = XmlPullParserFactory.newInstance().newPullParser();
        } catch (XmlPullParserException e3) {
            // TODO Auto-generated catch block
            e3.printStackTrace();
            parser = null;
        }

        if(book.tempString == null)
        {
            FileInputStream in_s;

            try {
                in_s = new FileInputStream(Launcher.directory + "/" + book.fileName);
            } catch (IOException e2) {
                in_s = null;
                e2.printStackTrace();
            }

            try {
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in_s, null);
            } catch (XmlPullParserException e1) {
                e1.printStackTrace();
            }
        }
        else
        {
            StringReader in_s;

            try {
                in_s = new StringReader(book.tempString);
            } catch (Exception e) {
                in_s = null;
                e.printStackTrace();
            }

            try {
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in_s);

            } catch (XmlPullParserException e1) {
                e1.printStackTrace();
            }
        }

        try {
            ParseXML(parser);
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return book;

    }

    private void ParseXML(XmlPullParser parser) throws XmlPullParserException,IOException
    {

        int eventType = parser.getEventType();
        Page currentPage = null;

        int i = 0; // Need this to kill the loop if something goes wrong

        while (eventType != XmlPullParser.END_DOCUMENT && i < readLimit) // End loop at end of file or if readLimit reached (eg, no events found)
        {
            String name = null;
            i++;

            switch (eventType)
            {
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    while(name == null)
                    {
                        parser.next();
                        name = parser.getName();
                    }

                    if(name.equals("page"))
                    {
                        Log.e(null, "Found a new page!");
                        currentPage = new Page();
                        //AND THEN PARSE VALUES OF THIS CURRENTPAGE INTO PAGE OBJECT
                    }

                    // CONFIG VARIABLES
                    if(name.equals("title"))
                    {
                        book.title = parser.nextText();
                    }
                    else if(name.equals("author"))
                    {
                        book.author = parser.nextText();
                    }
                    else if(name.equals("email"))
                    {
                        book.email = parser.nextText();
                    }
                    else if(name.equals("website"))
                    {
                        book.website = parser.nextText();
                    }
                    else if(name.equals("startPage"))
                    {
                        try
                        {
                            book.startPage = Integer.parseInt(parser.nextText());
                        }
                        catch(Exception e)
                        {
                            book.startPage = 1;
                        }
                    }
                    else if(name.equals("usesHP"))
                    {
                        String nextText = parser.nextText();
                        if (nextText == "1")
                            book.usesHP = true;
                        else
                            book.usesHP = false;
                    }
                    else if(name.equals("usesCash"))
                    {
                        String nextText = parser.nextText();
                        if (nextText == "1")
                            book.usesCash = true;
                        else
                            book.usesCash = false;
                    }
                    else if(name.equals("usesHTML"))
                    {
                        String nextText = parser.nextText();
                        if (nextText == "1")
                            book.usesHTML = true;
                        else
                            book.usesHTML = false;
                    }
                    else if (currentPage != null)
                    {
                        Log.e(null, "Found something other than a page: " + name);



                        // BUILD NEW PAGE
                        if(name.equals("id"))
                        {
                            try
                            {
                                currentPage.id = Integer.parseInt(parser.nextText());
                            }
                            catch(Exception e)
                            {
                                currentPage.id = 0;
                                Reader.UpdateErrors("ERROR: Invalid page id!");
                            }
                        }
                        else if(name.equals("content"))
                        {
                            currentPage.content = parser.nextText();
                        }
                        else if(name.equals("choice1"))
                        {
                            currentPage.choice1 = parser.nextText();
                        }
                        else if(name.equals("choice2"))
                        {
                            currentPage.choice2 = parser.nextText();
                        }
                        else if(name.equals("choice3"))
                        {
                            currentPage.choice3 = parser.nextText();
                        }
                        else if(name.equals("choice1Result"))
                        {
                            try
                            {
                                currentPage.choice1Result = Integer.parseInt(parser.nextText());
                            }
                            catch(Exception e)
                            {
                                Reader.UpdateErrors("ERROR: Invalid choice result for: Page " + currentPage.id + " choice1Result");
                                currentPage.choice1Result = 0;
                            }
                        }
                        else if(name.equals("choice2Result"))
                        {
                            try
                            {
                                currentPage.choice2Result = Integer.parseInt(parser.nextText());
                            }
                            catch(Exception e)
                            {
                                Reader.UpdateErrors("ERROR: Invalid choice result for: Page " + currentPage.id + " choice2Result");
                                currentPage.choice2Result = 0;
                            }
                        }
                        else if(name.equals("choice3Result"))
                        {
                            try
                            {
                                currentPage.choice3Result = Integer.parseInt(parser.nextText());
                            }
                            catch(Exception e)
                            {
                                Reader.UpdateErrors("ERROR: Invalid choice result for: Page " + currentPage.id + " choice3Result");
                                currentPage.choice3Result = 0;
                            }
                        }
                        else if(name.equals("image"))
                        {
                            boolean isEmpty = false;
                            currentPage.image = parser.nextText();
                            if(currentPage.image == "")
                            {
                                Reader.UpdateErrors("INFO: Image for page " + currentPage.id + " is empty. You should add an image or remove the image attribute from the page");
                                isEmpty = true;
                            }


                            String newImage = currentPage.image;

                        }
                        else if(name.equals("hp"))
                        {
                            try
                            {
                                currentPage.hp = Integer.parseInt(parser.nextText());
                            }
                            catch (Exception e)
                            {
                                Reader.UpdateErrors("ERROR: Invalid HP value for: Page " + currentPage.id);
                                currentPage.hp = 0;
                            }
                        }
                        else if(name.equals("cash"))
                        {
                            try
                            {
                                currentPage.cash = Integer.parseInt(parser.nextText());
                            }
                            catch (Exception e)
                            {
                                Reader.UpdateErrors("ERROR: Invalid Cash value for: Page " + currentPage.id);
                                currentPage.cash = 0;
                            }
                        }
                        else if(name.equals("deathMessage"))
                        {
                            currentPage.deathMessage = parser.nextText();
                        }

                        else if(name.equals("vo"))
                        {
                            currentPage.vo = parser.nextText();
                        }

                        else if(name.equals("music"))
                        {
                            currentPage.music = parser.nextText();
                        }



                    }
                    break;
                case XmlPullParser.END_TAG: // If end of file, add current page to list of pages
                    name = parser.getName();
                    if (name.equals("page") && currentPage != null)
                    {
                        book.pages.add(currentPage);
                        Log.e(null,"Page " + book.pages.size() + " completed, adding to array.");
                    }
            }
            eventType = parser.next();
        }
    }

}
