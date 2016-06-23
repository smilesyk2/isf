package kr.co.wisenut.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.wisenut.tea20.api.TeaClient;
import com.wisenut.tea20.types.Pair;

public class WiseTeaWorker {

	private static final Logger LOGGER = LoggingManager.getLoggerForClass();
	
	public static String teaIP;
	public static int teaPort;
	public static String collectionId;
	public static String searchField;
	
	private int totalRecommendedMediaCount;
	private int totalKeywordsCount;
	
	public WiseTeaWorker(String ip, int port, String collection) throws Exception{
		teaIP = ip;
		teaPort = port;
		
		collectionId = collection;
		searchField = "CONTENT_PLAIN";
	}
	
	public List<Pair<Integer>> getMainKeywordsPair(String article) throws Exception {
		TeaClient teaClient = new TeaClient(teaIP, teaPort);
		
		article = searchField + "$!$" + article;
		List<Pair<Integer>> keywordList = teaClient.extractKeywordsForPlainText(collectionId, article, "TERMS");
		
		totalKeywordsCount = keywordList.size();
		LOGGER.info("extractKeywordsForPlainText results in " + keywordList.size() + " keywords.");
		
		return keywordList;
	}
	
	public List<Pair<Integer>> getNerPair(String article, String similarContent){
		TeaClient teaClient = new TeaClient(teaIP, teaPort);
		
		return teaClient.extractNerForPlainText("article", article, "");
	}
	
	// 모델만 이용
	public List<Pair<Double>> getRecommendedContentsPair(String type, String contents, int pageSize) throws Exception {
		TeaClient teaClient = new TeaClient(teaIP, teaPort);
		
		contents = searchField + "$!$" + contents;
		List<Pair<Double>> documentList = teaClient.getSimilarDoc( type, contents, String.valueOf(pageSize));
		
		totalRecommendedMediaCount = documentList.size();
		LOGGER.info("getSimilarDoc results in " + documentList.size() + " documents.");
		
		return documentList;
	}
	
	// 모델 + SF-1 결과를 조합
	public List<Pair<Double>> getRecommendedContentsPair(String type, String article, ArrayList<String> searchResultList) throws Exception {
		TeaClient teaClient = new TeaClient(teaIP, teaPort);
		
		article = searchField + "$!$" + article;
		List<Pair<Double>> documentList = teaClient.getSimilarDocSf1( type, article, "100", searchResultList);
		
		totalRecommendedMediaCount = documentList.size();
		LOGGER.info("getSimilarDocSf1 results in " + documentList.size() + " documents.");
		
		return documentList;
	}
	
	public int getTotalRecommendedMediaCount(){
		return totalRecommendedMediaCount;
	}
	
	public int getTotalKeywordsCount(){
		return totalKeywordsCount;
	}
}
