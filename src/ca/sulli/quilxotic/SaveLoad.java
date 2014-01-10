package ca.sulli.quilxotic;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Sullivan on 1/9/14.
 */
public final class SaveLoad {

    public static void Save(SaveState saveState)
    {
        String fileName = saveState.fileName;
        fileName = fileName.replaceAll(".xml",".dat");
        File data = new File(Environment.getDataDirectory().toString() + "/data/ca.sulli.quilxotic/saves/");

        if(!data.exists())
            data.mkdir();

        File saveFile = new File(data, fileName);

        if(saveFile.exists())
            saveFile.delete();

        try
        {
            FileOutputStream fout = new FileOutputStream(saveFile);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(saveState);
        }
        catch (Exception e)
        {
            Log.e(null, "Couldn't write to file! - " + e);
        }
    }

    public static SaveState Load(String fileName)
    {
        fileName = fileName.replaceAll(".xml",".dat");

        SaveState o = new SaveState();

        try
        {
            FileInputStream fileIn = new FileInputStream(Environment.getDataDirectory().toString() + "/data/ca.sulli.quilxotic/saves/" + fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            o = (SaveState) in.readObject();
        }
        catch (Exception e)
        {
            Log.e(null,"Couldn't read savefile! - " + e);
            o = null;
        }

        return o;
    }

    public static boolean SaveExists(String title)
    {

        title = title.replaceAll(".xml",".dat");
        File saveFile = new File(Environment.getDataDirectory().toString() + "/data/ca.sulli.quilxotic/saves/" + title);

        if(saveFile.exists())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
