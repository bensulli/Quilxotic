package ca.sulli.quilxotic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.TextView;

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
    public boolean audioEnabled = false;

    /* FILE IO */
    public static File sdCard;
    public static File directory;
    public static File[] fileList;
    public static ArrayList<String> fileNameList;
    public boolean saveExists;
    public static boolean loadSave = false;
    public static SaveState saveState;

    /* INTERFACE */
    private Spinner bookSpinner;
    private CheckBox debugCheck;
    private CheckBox audioCheck;
    private TextView titleText;
    private TextView authorText;
    private TextView emailText;
    private TextView websiteText;
    private Button continueBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* CUSTOM LAYOUT SETUP */
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //Remove title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //Remove notification bar

        fileNameList = new ArrayList();
        book = new Book();

        setContentView(R.layout.launcher);

        bookSpinner = (Spinner)findViewById(R.id.bookSpinner);
        debugCheck = (CheckBox)findViewById(R.id.debugCheck);
        audioCheck = (CheckBox)findViewById(R.id.audioCheck);
        continueBtn = (Button)findViewById(R.id.continueBtn);

        titleText = (TextView)findViewById(R.id.titleText);
        authorText = (TextView)findViewById(R.id.authorText);
        emailText = (TextView)findViewById(R.id.emailText);
        websiteText = (TextView)findViewById(R.id.websiteText);

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
                book.fileName = chosenBook;
                XMLParser parser = new XMLParser();
                book = parser.ParseBook(book);

                if(book.title != null)
                    titleText.setText("Title: " + book.title);

                if(book.title != null)
                    authorText.setText("Author: " + book.author);

                if(book.title != null)
                    emailText.setText("Email: " + book.email);

                if(book.title != null)
                    websiteText.setText("Website: " + book.website);

                if(SaveLoad.SaveExists(book.fileName))
                {
                    continueBtn.setEnabled(true);
                    saveExists = true;
                }
                else
                {
                    continueBtn.setEnabled(false);
                    saveExists = false;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                continueBtn.setEnabled(false);
            }
        }); {
        }
}

    private void CopyContent()
    {

        File f = new File(getCacheDir()+"/Info.xml");
        if (f.exists())
            f.delete();

        if (!f.exists()) try {

            InputStream is = getAssets().open("Info.xml");
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
                String originPath = "/data/ca.sulli.quilxotic/cache/Info.xml";
                String destPath = "Quilxotic/Info.xml";
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
        if(saveExists)
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Overwrite?")
                    .setMessage("A save for this book exists. Are you sure you want to overwrite it and start again?")
                    .setIcon(android.R.drawable.ic_dialog_alert);

            builder.setPositiveButton("Yes, start fresh!", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id){
                    if(audioCheck.isChecked())
                        audioEnabled = true;

                    if (debugCheck.isChecked())
                        debug = true;
                    Intent myIntent = new Intent(Launcher.this, Reader.class);
                    Launcher.this.startActivity(myIntent);
                }
            });

            builder.setNegativeButton("No!", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {

                }
            });

            builder.show();

        }
        else
        {
            if(audioCheck.isChecked())
                audioEnabled = true;

            if (debugCheck.isChecked())
                debug = true;
            Intent myIntent = new Intent(Launcher.this, Reader.class);
            Launcher.this.startActivity(myIntent);
        }
    }

    public void ContinueBookBtn(View v)
    {
        saveState = SaveLoad.Load(book.fileName);

        if(saveState != null)
            loadSave = true;

        if(audioCheck.isChecked())
            audioEnabled = true;

        if (debugCheck.isChecked())
            debug = true;
        Intent myIntent = new Intent(Launcher.this, Reader.class);
        Launcher.this.startActivity(myIntent);
    }

}




