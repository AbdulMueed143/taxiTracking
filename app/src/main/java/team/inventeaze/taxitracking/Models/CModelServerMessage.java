package team.inventeaze.taxitracking.Models;

/**
 * Created by AbdulMueed on 1/5/2015.
 */
public class CModelServerMessage {

    public String message;
    public int id;

    public CModelServerMessage(String pmessage,int pid) {
        message = pmessage;
        id = pid;
    }
}
