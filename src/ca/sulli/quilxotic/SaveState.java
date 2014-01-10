package ca.sulli.quilxotic;

import java.io.Serializable;

/**
 * Created by Sullivan on 1/9/14.
 */

public class SaveState implements Serializable {

    public int currentPage;
    public int hp;
    public int cash;
    public String fileName;

    SaveState(int currentPage, int hp, int cash, String fileName)
    {
        this.currentPage = currentPage;
        this.hp = hp;
        this.cash = cash;
        this.fileName = fileName;
    }

    SaveState() {}

}
