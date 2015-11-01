package com.example.xyzreader.remote;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;


public class OttoBusProvider {

    private Bus bus;
    private static OttoBusProvider provider;

    public static synchronized OttoBusProvider getInstance() {
        if(provider == null) provider = new OttoBusProvider();

        return provider;
    }

    private OttoBusProvider() {
        this.bus = new Bus(ThreadEnforcer.ANY);
    }

    public Bus getBus() {
        return bus;
    }
}
