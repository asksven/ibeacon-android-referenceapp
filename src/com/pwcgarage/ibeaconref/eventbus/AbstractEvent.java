package com.pwcgarage.ibeaconref.eventbus;

public abstract class AbstractEvent
{
    private Enum _type;
 
    protected AbstractEvent(Enum type)
    {
        this._type = type;
    }
 
    public Enum getType()
    {
        return this._type;
    }
}