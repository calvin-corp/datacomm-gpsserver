package lib;

public interface Client
{
    public void onClose(boolean remote);
    public void onMessage(String msg);
}
