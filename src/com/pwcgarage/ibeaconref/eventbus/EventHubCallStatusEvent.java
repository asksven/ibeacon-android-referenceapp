package com.pwcgarage.ibeaconref.eventbus;

public class EventHubCallStatusEvent extends AbstractEvent
{
    public enum Type
    {
        COMPLETED,
        STARTED
    }
 
    private int _resultCode;
 
    public EventHubCallStatusEvent(Type type,int resultCode)
    {
        super(type);
        this._resultCode = resultCode;
    }
 
    public int getResultCode()
    {
        return _resultCode;
    }
}
