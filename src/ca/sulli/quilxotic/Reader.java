package ca.sulli.quilxotic;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import ca.sulli.quilxotic.R.color;

import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class Reader extends Activity {

	/* FOR DEBUGGING */
	public static boolean DEV_MODE = false; // Defaults to false, set based on checkbox on launch
	
	/* INST CONTENT OBJECTS */
	public int pageNum;
	public ImageView pageImage;
	public TextView content;
	public Button choice1;
	public Button choice2;
	public Button choice3;
	public ImageView hpCover;
	public static TextView errorText;
    public ScrollView scrollView;
    public MediaPlayer voPlayer;
    public MediaPlayer musicPlayer;
    public boolean wasPlayingMusic = false;
    public boolean wasPlayingVo = false;
    public WebView contentView;
    public boolean suspended;
    private final static int MAX_VOLUME = 100;
    public boolean audioEnabled;

	/* INST GAME OBJECTS */
	public TextView cashText;
	public int hp; 
	public int cash;
	public boolean alive = true;
	public int startingHealth = 100;
	public int startingCash = 0;
	
	/* XML ERROR LOG */
	public static String errorLog = "";

	/* TEH BOOK */
	public Book book;
    public Page onPage;


// ********************
// *** STARTUP ***
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
        contentView = (WebView)findViewById(R.id.contentView);

        /* LINK GAME OBJECTS */
        cashText = (TextView)findViewById(R.id.cashTxt);
        hp = startingHealth;
        cash = startingCash;
        cashText.setText(Integer.toString(cash));
        hpCover.getLayoutParams().width = 1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.eriel_launcher, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /* CUSTOM LAYOUT SETUP */
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //Remove title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //Remove notification bar

        setContentView(R.layout.reader);

        Intent intent = getIntent();

        book = Launcher.book;

        Initialize(); // Initial gruntwork


        if(Processor.HTMLUsed(book))
        {
            book = Processor.ProcessHTML(book);
        }
        else
        {
            Validator.CheckTags(book);
        }


        XMLParser parser = new XMLParser();
        book = parser.ParseBook(book);

        if(Launcher.debug)
            DEV_MODE = true;

        onPage = book.pages.get(FindPage(book.startPage));
        UpdatePage(onPage);

    }
// ********************


// ********************
// *** LIFECYCLE ***
    @Override
    protected void onStop() {
        super.onStop();

        if(!suspended)
        {
            if (musicPlayer.isPlaying())
            {
                musicPlayer.pause();
                wasPlayingMusic = true;
            }
            else
            {
                wasPlayingMusic = false;
            }

            if (voPlayer.isPlaying())
            {
                voPlayer.pause();
                wasPlayingVo = true;
            }
            else
            {
                wasPlayingVo = false;
            }
        }
        suspended = true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(audioEnabled)

            if(suspended)
            {
            if(wasPlayingMusic)
                musicPlayer.start();

            if(wasPlayingVo)
                voPlayer.start();
            }

        suspended = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(!suspended)
        {
            if (musicPlayer.isPlaying())
            {
                musicPlayer.pause();
                wasPlayingMusic = true;
            }
            else
            {
                wasPlayingMusic = false;
            }

            if (voPlayer.isPlaying())
            {
                voPlayer.pause();
                wasPlayingVo = true;
            }
            else
            {
                wasPlayingVo = false;
            }
        }
        suspended = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(suspended)
        {
            if(wasPlayingMusic)
                musicPlayer.start();

            if(wasPlayingVo)
                voPlayer.start();
        }

        suspended = false;
    }


// ********************

// ********************
// *** VALIDATION ***
    private boolean ValidateIDs() {
    	
    	Set<Integer> ids = new HashSet();   	
    	Set<Integer> dests = new HashSet();
    	boolean valid = true;
    	
    	for(int x = 0; x < book.pages.size(); x++)
         {
    		if (!ids.add(book.pages.get(x).id))
    		{
    			UpdateErrors("ERROR: Duplicate Page ID found - " + x);
    			valid = false;
    		}
    		
    		dests.add(book.pages.get(x).choice1Result);
    		dests.add(book.pages.get(x).choice2Result);
    		dests.add(book.pages.get(x).choice3Result);
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
	    	
	    	//content.setText(contentString);
            contentView.loadData(contentString,"text/html",null);

	    	if (CheckRequirements(onPage.choice1Result) == false) {
	    		choice1.setTextColor(color.disabled_text);
	    		choice1.setClickable(false);
	    	}
	    	
	    	choice1.setText(onPage.choice1);
			
	    	if (CheckRequirements(onPage.choice2Result) == false) {
    			choice2.setTextColor(color.disabled_text);
    			choice2.setClickable(false);
	    	}

            if (onPage.choice2 == null || onPage.choice2 == "")
            {
                choice2.setVisibility(View.GONE);
            }
            else
            {
                choice2.setVisibility(View.VISIBLE);

                if (CheckRequirements(onPage.choice2Result) == false) {
                    choice2.setTextColor(color.disabled_text);
                    choice2.setClickable(false);
                }

                choice2.setText(onPage.choice2);
            }

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
                File imageFile = new File(Launcher.directory + "/" + newImage + ".png");

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
            try
            {
            PlayVO(onPage.vo);
            }
            catch(Exception e)
            {
                UpdateErrors("ERROR: Couldn't play or find audio file: " + onPage.vo + ".ogg on page " + onPage.id);
            }

        if(onPage.music != "" && onPage.music != null)
        {
            if(onPage.music != "0")
                try
                {
                    PlayMusic(onPage.music);
                }
                catch(Exception e)
                {
                    UpdateErrors("ERROR: Couldn't play or find audio file: " + onPage.music + ".ogg on page " + onPage.id);
                }
            else if (onPage.music == "0")
                StopMusic();
        }
	}

    private boolean CheckRequirements(int choice) {

        int requiredCash = 0;

        try
        {
            Page destPage = book.pages.get(FindPage(choice));
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

    public static void UpdateErrors(String newError)    {
        errorLog = errorLog.concat("\n" + newError);
        if(DEV_MODE)
        {
            errorText.setText(errorLog);
        }

    }
// ********************


// ********************
// *** MEDIA PLAYER ***
    private void PlayVO(String vo)    {
        voPlayer = MediaPlayer.create(this, Uri.fromFile(new File(Launcher.directory + "/" + vo + ".ogg")));

        if(audioEnabled)
            voPlayer.start();
    }

    private void StopVO()    {
        voPlayer.stop();
    }

    private void PlayMusic(String music)    {
        musicPlayer = MediaPlayer.create(this, Uri.fromFile(new File(Launcher.directory + "/" + music + ".ogg")));
        musicPlayer.setLooping(true);

        final float volume = (float) (1 - (Math.log(MAX_VOLUME - 80) / Math.log(MAX_VOLUME)));
        musicPlayer.setVolume(volume, volume);
        if(audioEnabled)
            musicPlayer.start();
    }

    private void StopMusic()    {
        musicPlayer.stop();
    }
// ********************


// ********************
// *** LOGIC & FLOW ***
	public void Die()    {
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

    public int FindPage(int destination)    {

        int returner = 0;

        for(int x = 0; x < book.pages.size(); x++)
        {
            if(book.pages.get(x).id == destination)
            {
                returner = x;
                break;
            }
        }

        return returner;
    }

    public void Choose(View v)    {

        int destinationPage;

        StopVO();

    	switch(v.getId()) {
    	case (R.id.choice1Btn):
    		
    		if(alive == true)
    		{
                destinationPage = onPage.choice1Result;
                onPage = book.pages.get(FindPage(destinationPage));
    			UpdatePage(onPage);
    		}
    		else
    		{
    			alive = true;
    			hp = startingHealth;

                destinationPage = book.startPage;
                onPage = book.pages.get(FindPage(destinationPage));

    			UpdatePage(onPage);
    		}
    		break;
    	case (R.id.choice2Btn):
            destinationPage = onPage.choice2Result;
            onPage = book.pages.get(FindPage(destinationPage));
            UpdatePage(onPage);
    		break;
    	case (R.id.choice3Btn):
            destinationPage = onPage.choice3Result;
            onPage = book.pages.get(FindPage(destinationPage));
            UpdatePage(onPage);
    		break;
    	}
    }
// ********************



}
