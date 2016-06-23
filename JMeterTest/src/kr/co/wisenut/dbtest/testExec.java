package kr.co.wisenut.dbtest;

import java.io.IOException;

public class testExec {

 /** 
  * @param args
  */
 public static void main(String[] args) {
  // TODO Auto-generated method stub
   System.out.println("start.");
   
   //String cmd="C:\\referee21_bld01\\batch\\bridge.cmd C:\\referee21_bld01\\config\\config_kbi.xml db report static";
   String[] cmd = {"cmd.exe", "/c", "C:\\referee21_bld01\\batch\\stc_kbi.cmd"};
  
   
   Process ps=null;
   try {
   ps = Runtime.getRuntime().exec(cmd);
  } catch (IOException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
   
  try
  {
   ps.waitFor();
  }
  catch(InterruptedException e)
  {
   Thread.currentThread().interrupt();
  }
   
   
   System.out.println("terminated.");

   
 }

}
