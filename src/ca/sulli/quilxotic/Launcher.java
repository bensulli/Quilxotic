package ca.sulli.quilxotic;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class Launcher extends Activity {

    public static Reader reader;
    public static Book book;
    public static String chosenBook;
    public static boolean debug;

    /* FILE IO */
    public static File sdCard;
    public static File directory;
    public static File[] fileList;
    public static ArrayList<String> fileNameList;

    /* INTERFACE */
    public Spinner bookSpinner;
    public CheckBox debugCheck;
    public Button beginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* CUSTOM LAYOUT SETUP */
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //Remove title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //Remove notification bar

        fileNameList = new ArrayList();

        setContentView(R.layout.launcher);

        bookSpinner = (Spinner)findViewById(R.id.bookSpinner);
        debugCheck = (CheckBox)findViewById(R.id.debugCheck);
        beginBtn = (Button)findViewById(R.id.beginBtn);

        sdCard = Environment.getExternalStorageDirectory();
        directory = new File(sdCard.getAbsolutePath() + "/Quilxotic");

        if(!directory.exists())
            directory.mkdir();

        Log.e(null,"Copying content to SDCard");
        CopyContent();
        Log.e(null,"Done copying content to SDCard");

        fileList = new File(directory.getAbsolutePath()).listFiles();
        int fileCount = new File(directory.getAbsolutePath()).list().length;
        Log.e(null,fileCount + " files found in /Quilxotic...");

        for (File file : fileList){
            if(file.getName().contains("xml"))
                fileNameList.add(file.getName());
            Log.d(null, "Added file: " + file.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, fileNameList);

        bookSpinner.setAdapter(adapter);
        bookSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                chosenBook = bookSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        }); {
        }
}

    private void CopyContent()
    {

        File f = new File(getCacheDir()+"/example.xml");
        if (f.exists())
            f.delete();

        if (!f.exists()) try {

            InputStream is = getAssets().open("example.xml");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();


            FileOutputStream fos = new FileOutputStream(f);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) { throw new RuntimeException(e); }

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String originPath = "/data/ca.sulli.quilxotic/cache/example.xml";
                String destPath = "Quilxotic/example.xml";
                File currentXML = new File(data, originPath);
                File backupXML = new File(sd, destPath);

                if (backupXML.exists())
                    backupXML.delete();

                if (currentXML.exists()) {
                    FileChannel src = new FileInputStream(currentXML).getChannel();
                    FileChannel dst = new FileOutputStream(backupXML).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                } else {
                    Log.e(null, "File does not exist: " + currentXML.toString() + "or destination XML exists");
                }
            } else {
                Log.e(null, "SDCard not writable, backup aborted.");
            }
        } catch (Exception ex) {
            Log.e(null, "Error backing up database to sdcard.", ex);
        }
    }

    public void OpenBookBtn(View v)
    {
        book = new Book();
        book.fileName = chosenBook;
        if (debugCheck.isChecked())
            debug = true;
        Intent myIntent = new Intent(Launcher.this, Reader.class);
        //myIntent.putExtra("book", book); //Optional parameters
        Launcher.this.startActivity(myIntent);
    }

}



