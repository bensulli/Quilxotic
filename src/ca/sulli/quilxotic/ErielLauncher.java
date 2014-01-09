package ca.sulli.quilxotic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import ca.sulli.quilxotic.R.color;

import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class ErielLauncher extends Activity {

	/* FOR DEBUGGING */
	public static boolean DEV_MODE = false; // Defaults to false, but this is actually set in onCreate
	
	/* INST CONTENT OBJECTS */
	public int pageNum;
	public ImageView pageImage;
	public TextView content;
	public Button choice1;
	public Button choice2;
	public Button choice3;
	public ImageView hpCover;
	public TextView errorText;
    public ScrollView scrollView;
    public MediaPlayer voPlayer;
    public MediaPlayer musicPlayer;
	
	/* INST GAME OBJECTS */
	public TextView cashText;
	public int hp; 
	public int cash;
	public boolean alive = true;
	public static int startingHealth = 100;
	public static int startingCash = 0;

    /* INST CONFIG OPTIONS */
    public static String title;
    public static String author;
    public static String contact;
    public static int startPage;
    public static boolean usesHP;
    public static boolean usesCash;
    public static String localFolder;
    public static String titleImage;

    private final static int MAX_VOLUME = 100;
	
	/* XML ERROR LOG */
	public String errorLog = "";
	
	/* FILE IO */
	public static String book = "content.xml";
	public static String bookURI;
	public static boolean bookExists;
    public static File sdCard;
    public static File directory;
    public static File bookFile;

	/* ARRAY OF PAGES */
	public ArrayList<Page> pages = null;
	public Page onPage;
	
	public static int readLimit = 5000; // Used to kill a loop if it clearly isn't advancing
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /* CUSTOM LAYOUT SETUP */
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //Remove title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //Remove notification bar

        setContentView(R.layout.activity_eriel_launcher);
        
        //if((getApplicationContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0)
        //	DEV_MODE = true; // Set DEV MODE if package is not signed (eg, not in the market)
        CopyContent();

        sdCard = Environment.getExternalStorageDirectory();
        directory = new File(sdCard.getAbsolutePath() + "/Quilxotic");
        
        if(!directory.exists())
        	directory.mkdir();
       
        bookFile = new File(directory, book);
        bookURI = bookFile.getAbsolutePath();
        
		if(!bookFile.exists())
        {
        	bookExists = false;
        	
        }
        else if (bookFile.exists())
        {
        	bookExists = true;
        }

        // DEBUG ONLY
        File[] fileList = directory.listFiles();


        Initialize(); // Initial gruntwork

        
        if(bookExists)
        {
	        if(ValidateIDs()) // Ensure there are no duplicate page IDs
	        	UpdateErrors("CRITICAL: IDs and/or Destinations did not validate! See above for info.");
	        
	        // SET ONPAGE TO FIRST PAGE
	        for(int x = 0; x <= pages.size(); x++)
	        {
	        	if(pages.get(x).id == startPage)
	        	{
	        		onPage = pages.get(x);
	        		UpdatePage(onPage);
	        		break;
	        	}
	        }
        }
        
    }

    private boolean ValidateIDs() {
    	
    	Set<Integer> ids = new HashSet();   	
    	Set<Integer> dests = new HashSet();
    	boolean valid = true;
    	
    	for(int x = 0; x < pages.size(); x++)
         {
    		if (!ids.add(pages.get(x).id))
    		{
    			UpdateErrors("ERROR: Duplicate Page ID found - " + x);
    			valid = false;
    		}
    		
    		dests.add(pages.get(x).choice1Result);
    		dests.add(pages.get(x).choice2Result);
    		dests.add(pages.get(x).choice3Result);
         }
    	
    	for(int y = 1; y < dests.size(); y++)
    	{
    		if(!ids.contains(y))
    		{
    			valid = false;
    			UpdateErrors("ERROR: Destination " + y + " doesn't exist.");
    		}
    	}
    	
    	return valid;
    	
    	
    	
		
	}

	private void UpdatePage(Page onPage) { 
		hp = hp + onPage.hp; // This is "+" because damage is expressed as a negative number in the XML
		if (hp > 0)
		{
            Resources r = getApplicationContext().getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    265,
                    r.getDisplayMetrics()
            );

            double  healthCoverWidth = px * (100 - hp) * 0.01;
            hpCover.getLayoutParams().width = (int)healthCoverWidth;
			cash = cash + onPage.cash;
			cashText.setText(Integer.toString(cash) + "G");
			
	    	Log.e(null,"Updating layout...");
	    	String contentString = onPage.content;
	    	
	    	content.setText(contentString);

	    	if (CheckRequirements(onPage.choice1Result) == false) {
	    		choice1.setTextColor(color.disabled_text);
	    		choice1.setClickable(false);
	    	}
	    	
	    	choice1.setText(onPage.choice1);
			
	    	if (CheckRequirements(onPage.choice2Result) == false) {
    			choice2.setTextColor(color.disabled_text);
    			choice2.setClickable(false);
	    	}
	    				
	    	choice2.setText(onPage.choice2);
				
			choice2.setVisibility(View.VISIBLE);
			
			if (onPage.choice3 == null || onPage.choice3 == "")
			{
				choice3.setVisibility(View.GONE);
			}
			else
			{	
				choice3.setVisibility(View.VISIBLE);
				
				if (CheckRequirements(onPage.choice3Result) == false) {
	    			choice3.setTextColor(color.disabled_text);
	    			choice3.setClickable(false);
				}
		    				
				choice3.setText(onPage.choice3);
			}
			

			String newImage = onPage.image;
            int resID;

            if(newImage != "" && newImage != null)
            {
			    resID = getResources().getIdentifier(newImage, "drawable", getPackageName());
            }
            else
            {
                resID = 0;
            }

            if(resID != 0)
            {
                pageImage.setImageResource(resID);

                r = getApplicationContext().getResources();
                px = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        250,
                        r.getDisplayMetrics()
                );
                pageImage.setMinimumHeight(px);
            }
            else if(resID == 0)
            {
                File imageFile = new File(directory + "/" + newImage + ".png");

                if(imageFile.isFile())
                {
                    pageImage.setImageURI(Uri.fromFile(imageFile));

                   r = getApplicationContext().getResources();
                    px = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            250,
                            r.getDisplayMetrics()
                    );
                    pageImage.setMinimumHeight(px);
                }
            }

			
			
		}
		else if (hp <= 0)
		{
			Die();
		}

        scrollView.fullScroll(ScrollView.FOCUS_UP);

        if(onPage.vo != "" && onPage.music != null)
            PlayVO(onPage.vo);

        if(onPage.music != "" && onPage.music != null)
        {
            if(onPage.music != "0")
                PlayMusic(onPage.music);
            else if (onPage.music == "0")
                StopMusic();
        }
	}

    private void PlayVO(String vo)
    {
        voPlayer = MediaPlayer.create(this, Uri.fromFile(new File(directory + "/" + vo + ".ogg")));
        voPlayer.start();
    }

    private void StopVO()
    {
        voPlayer.stop();
    }

    private void PlayMusic(String music)
    {
        musicPlayer = MediaPlayer.create(this, Uri.fromFile(new File(directory + "/" + music + ".ogg")));

        final float volume = (float) (1 - (Math.log(MAX_VOLUME - 80) / Math.log(MAX_VOLUME)));
        musicPlayer.setVolume(volume, volume);
        musicPlayer.start();
    }

    private void StopMusic()
    {
        musicPlayer.stop();
    }
    
    private boolean CheckRequirements(int choice) {
		
    	int requiredCash = 0;
    	
    	try
    	{
    		Page destPage = pages.get(FindPage(choice));
        	requiredCash = destPage.cash; 
    	}
    	catch(Exception e)
    	{
    		UpdateErrors("ERROR: Choice destination \'" + choice + "\' doesn't exist in XML!");
    		return false;
    	}
    	

    	
    	if (requiredCash < 0)
    	{
    		requiredCash = requiredCash * -1; // Inverted since negative numbers are used in the XML for cost
    	}
    	else
    		requiredCash = 0;
    	
    	if (requiredCash > cash)
    	{
    		return false;
    	}
    	else
    	{
    		return true;
    	}
	}

	public void Die()
    {
    	hpCover.getLayoutParams().width = 366;
    	cashText.setText("0");
    	String newImage = "epitaph";
		int resID = getResources().getIdentifier(newImage, "drawable", getPackageName());
		pageImage.setImageResource(resID);
		
		alive = false;
		
    	content.setText(onPage.deathMessage);
		
		choice1.setText("Restart!");
		choice2.setVisibility(View.GONE);
		choice3.setVisibility(View.GONE);
		
		
    }

    public int FindPage(int destination)
    {

        int returner = 0;

        for(int x = 0; x < pages.size(); x++)
        {
            if(pages.get(x).id == destination)
            {
                returner = x;
                break;
            }
        }

        return returner;
    }

    public void Choose(View v)
    {

        int destinationPage;

        StopVO();

    	switch(v.getId()) {
    	case (R.id.choice1Btn):
    		
    		if(alive == true)
    		{
                destinationPage = onPage.choice1Result;
                onPage = pages.get(FindPage(destinationPage));
    			UpdatePage(onPage);
    		}
    		else
    		{
    			alive = true;
    			hp = startingHealth;

                destinationPage = startPage;
                onPage = pages.get(FindPage(destinationPage));

    			UpdatePage(onPage);
    		}
    		break;
    	case (R.id.choice2Btn):
            destinationPage = onPage.choice2Result;
            onPage = pages.get(FindPage(destinationPage));
            UpdatePage(onPage);
    		break;
    	case (R.id.choice3Btn):
            destinationPage = onPage.choice3Result;
            onPage = pages.get(FindPage(destinationPage));
            UpdatePage(onPage);
    		break;
    	}
    }
    
	private void Initialize() {
        /* LINK LAYOUT OBJECTS */
        pageImage = (ImageView)findViewById(R.id.pageImage);
        content = (TextView)findViewById(R.id.contentTxt);
        choice1 = (Button)findViewById(R.id.choice1Btn);
        choice2 = (Button)findViewById(R.id.choice2Btn);
        choice3 = (Button)findViewById(R.id.choice3Btn);
        hpCover = (ImageView)findViewById(R.id.healthCover);
        errorText = (TextView)findViewById(R.id.errorText);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        
        /* LINK GAME OBJECTS */
        cashText = (TextView)findViewById(R.id.cashTxt);
        hp = startingHealth;
        cash = startingCash;
        cashText.setText(Integer.toString(cash));
        hpCover.getLayoutParams().width = 1;


        if(bookExists)
        {
	        /* READ XML BOOK */
	        XmlPullParser parser;
			try {
				parser = XmlPullParserFactory.newInstance().newPullParser();
			} catch (XmlPullParserException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
				parser = null;
			}
			
			FileInputStream in_s;
			
			try {
				in_s = new FileInputStream(bookURI);
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
	        
	 
	        try {
				ParseXML(parser);
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else if (!bookExists)
        {
        	UpdateErrors("No content.xml book found. Add it in the /Quilxotic directory that's already been created by running this app.");
        }
	}

    private void CopyContent()
    {

        File f = new File(getCacheDir()+"/content.xml");
        if (!f.exists()) try {

            InputStream is = getAssets().open("content.xml");
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
                String originPath = "/data/ca.sulli.quilxotic/cache/content.xml";
                String destPath = "Quilxotic/content.xml";
                File currentXML = new File(data, originPath);
                File backupXML = new File(sd, destPath);

                if (currentXML.exists() && !backupXML.exists()) {
                    FileChannel src = new FileInputStream(currentXML).getChannel();
                    FileChannel dst = new FileOutputStream(backupXML).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                } else {
                    Log.e(null, "File does not exist: " + originPath);
                }
            } else {
                Log.e(null, "SDCard not writable, backup aborted.");
            }
        } catch (Exception ex) {
            Log.e(null, "Error backing up database to sdcard.", ex);
        }
    }

	private void UpdateErrors(String newError)
	{
		errorLog = errorLog.concat("\n" + newError);
		if(DEV_MODE)
		{
			errorText.setText(errorLog);
		}
		
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
    		case XmlPullParser.START_DOCUMENT:
    			pages = new ArrayList();
    		case XmlPullParser.START_TAG:
    			name = parser.getName();
    			while(name == null)
    			{
    				parser.next();
    				name = parser.getName();
    			}

                if(name.equals("page"))
    			{
    				currentPage = new Page();
    				//AND THEN PARSE VALUES OF THIS CURRENTPAGE INTO PAGE OBJECT
    			}

                // CONFIG VARIABLES
                if(name.equals("title"))
                {
                    title = parser.nextText();
                }
                else if(name.equals("author"))
                {
                    author = parser.nextText();
                }
                else if(name.equals("contact"))
                {
                    contact = parser.nextText();
                }
                else if(name.equals("titleImage"))
                {
                    titleImage = parser.nextText();
                }
                else if(name.equals("startPage"))
                {
                    try
                    {
                        startPage = Integer.parseInt(parser.nextText());
                    }
                    catch(Exception e)
                    {
                        startPage = 1;
                    }
                }
                else if(name.equals("usesHP"))
                {
                    String nextText = parser.nextText();
                    if (nextText == "1")
                        usesHP = true;
                    else
                        usesHP = false;
                }
                else if(name.equals("usesCash"))
                {
                    String nextText = parser.nextText();
                    if (nextText == "1")
                        usesCash = true;
                    else
                        usesCash = false;
                }
                else if(name.equals("localFolder"))
                {
                    localFolder = parser.nextText();
                }


                else if (currentPage != null)
                {
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
                            UpdateErrors("ERROR: Invalid page id!");
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
                            UpdateErrors("ERROR: Invalid choice result for: Page " + currentPage.id + " choice1Result");
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
                            UpdateErrors("ERROR: Invalid choice result for: Page " + currentPage.id + " choice2Result");
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
                            UpdateErrors("ERROR: Invalid choice result for: Page " + currentPage.id + " choice3Result");
                            currentPage.choice3Result = 0;
                        }
                    }
                    else if(name.equals("image"))
                    {
                        boolean isEmpty = false;
                        currentPage.image = parser.nextText();
                        if(currentPage.image == "")
                        {
                            UpdateErrors("INFO: Image for page " + currentPage.id + " is empty. You should add an image or remove the image attribute from the page");
                            isEmpty = true;
                        }


                        String newImage = currentPage.image;
                        int resID = getResources().getIdentifier(newImage, "drawable", getPackageName());

                        File imageFile = new File(directory, newImage + ".png");
                        String imageURI = imageFile.getAbsolutePath() + ".png";

                        if(resID == 0 && !imageFile.isFile() && isEmpty == false)
                            UpdateErrors("WARNING: Image specified doesn't exist in the /drawable or /Quilxotic folders - " + currentPage.image + ".png");

                    }
                    else if(name.equals("hp"))
                    {
                        try
                        {
                            currentPage.hp = Integer.parseInt(parser.nextText());
                        }
                        catch (Exception e)
                        {
                            UpdateErrors("ERROR: Invalid HP value for: Page " + currentPage.id);
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
                            UpdateErrors("ERROR: Invalid Cash value for: Page " + currentPage.id);
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
                pages.add(currentPage);
            }
            }
        eventType = parser.next();
        }
    UpdateErrors("INFO: " + book + " imported with " + pages.size() + " pages.");

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.eriel_launcher, menu);
        return true;
    }
    
}
