package kr.co.wisenut.common;

import java.util.List;

import com.wisenut.tea20.api.ClassifierClient;
import com.wisenut.tea20.types.Pair;

public class WiseClassifierWorker {

	public static String cfIP;
	public static int cfPort;
	public static String collectionId;
	
	public WiseClassifierWorker(String ip, int port, String collection) throws Exception{
		cfIP = ip;
		cfPort = port;
		
		collectionId = collection;
	}
	
	public List<Pair<Double>> getRecommendedCategoryPair(String keywords){
		ClassifierClient cfClient = new ClassifierClient(cfIP, cfPort);
					
		List<Pair<Double>> resultList = cfClient.predictDocument( collectionId, keywords, "^", ":");
		
		return resultList;
	}
}
