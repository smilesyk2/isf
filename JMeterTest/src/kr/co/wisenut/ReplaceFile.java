package kr.co.wisenut;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ReplaceFile {
	public static final String NEW_LINE = "\n";
	
	public static void main(String[] args) throws IOException{
		InputStreamReader isr = null;
		OutputStreamWriter osw = null;
		
		try{
			isr = new InputStreamReader(new FileInputStream(new File("article.SCD")));
			osw = new OutputStreamWriter(new FileOutputStream(new File("article_renew.SCD")));
			
			BufferedReader br = new BufferedReader(isr);
			String line;
			while( (line = br.readLine()) != null ){
				if(line.startsWith("<DOCID>article_")){
					int idx = line.indexOf("<DOCID>article_")+"<DOCID>article_".length();
					String docid = line.substring(idx);
					line = "<DOCID>A" + docid;
				}
				
				osw.write(line);
				osw.write(NEW_LINE);
				osw.flush();
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(isr != null){
				isr.close();
			}
			
			if(osw != null){
				osw.close();
			}
		}
		
	}
}
