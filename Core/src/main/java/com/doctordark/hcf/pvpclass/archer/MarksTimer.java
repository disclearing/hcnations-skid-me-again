/*
 * Copyright (C) 2016 SystemUpdate (https://systemupdate.io) All Rights Reserved
 */

package com.doctordark.hcf.pvpclass.archer;

import com.doctordark.hcf.timer.PlayerTimer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MarksTimer extends PlayerTimer{

    private static long ARCHER_TAG_DURATION = TimeUnit.SECONDS.toMillis(15L);

    public MarksTimer(String name){
        super(name, ARCHER_TAG_DURATION, false);
    }

    @Override
    public String getScoreboardPrefix(){
        return "";
    }

    public boolean setCooldown(UUID taggedUUID){
        return super.setCooldown(null, taggedUUID, ARCHER_TAG_DURATION, true, null);
    }
}
