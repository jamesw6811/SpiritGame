package com.spiritgame;

/**
 * Created with IntelliJ IDEA.
 * User: jamesw
 * Date: 11/24/12
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class GameTimer {
    private long starttime;

    public GameTimer() {
        starttime = System.currentTimeMillis();
    }

    public long getTimeMillis() {
        return System.currentTimeMillis() - starttime;
    }
}
