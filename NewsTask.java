import java.net.*;

public class NewsTask implements Runnable {
  private String address;
  private int mcport;
  private String news;
  private int timer;

  public NewsTask(String address, int mcport, int timer) {
    this.address = address;
    this.mcport = mcport;
    this.news = "";
    this.timer = timer;
  }

  public void run() {
    InetAddress group = null;
    MulticastSocket ms = null;
    try {
      ms = new MulticastSocket(mcport);
      ms.setSoTimeout(timer);
      group = InetAddress.getByName(address);
      ms.joinGroup(group);
      while (!Thread.currentThread().isInterrupted()) {
        DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);
        try{
        ms.receive(dp);
        news = new String(dp.getData(), 0, dp.getLength());
        if(!news.equals("")){
          synchronized(System.out){
          System.out.println("\n=News: " + news+"=\n");
          }
          news = "";
        }
        }catch(SocketTimeoutException e){
         // System.out.println("Timeout scaduto");
        }        
      }
    } catch (Exception e) {
      System.out.println("errore nel receive_news()");
      e.printStackTrace();
    }
    if (ms != null)
      try {
        ms.leaveGroup(group);
        ms.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
  }
}
