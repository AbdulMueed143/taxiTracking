package team.inventeaze.taxitracking.Interfaces;

/**
 * Created by DELL on 12/6/2014.
 */
public interface IViewPageInterface {
    public void ViewPageDone(int viewid);
    public void ViewPageStart(int viewid);
    public void EventInPage(int viewid,String eventname,Object object);

}
