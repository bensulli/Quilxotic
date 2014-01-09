package ca.sulli.quilxotic;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sullivan on 1/8/14.
 */
public final class Processor {

    public static boolean HTMLUsed(Book book)
    {
        try
        {

        List<String> lines = new ArrayList<String>();

        BufferedReader in = new BufferedReader(new FileReader(new File(Launcher.directory + "/" + book.fileName)));
        String line = in.readLine();
        while (line != null) {
            line = line.replaceAll("\\s+","");
            if (line.startsWith("<usesHTML>1")) {
                return true;
            }
            lines.add(line);
            line = in.readLine();
        }
        }
        catch(Exception e)
        {
            Log.e(null, "Failed to read status of HTML usage");
        }
        return false;
    }

    public static Book ProcessHTML(Book book)
    {

        book.tempString = "";

        try
        {
        List<String> lines = new ArrayList<String>();

        BufferedReader in = new BufferedReader(new FileReader(new File(Launcher.directory + "/" + book.fileName)));
        String line = in.readLine();
        while (line != null) {
            line = line.replaceAll("<content>","<content><![CDATA[");
            line = line.replaceAll("</content>","]]></content>");
            book.tempString = book.tempString + line;
            line = in.readLine();
            }
        }
    catch(Exception e)
    {
        Log.e(null, "Error processing html...");
    }
        return book;
    }

}
