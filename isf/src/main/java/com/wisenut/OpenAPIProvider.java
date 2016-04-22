package com.wisenut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wisenut.model.WNResultData;
import com.wisenut.tea20.types.Pair;
import com.wisenut.util.StringUtil;
import com.wisenut.worker.DaumWorker;
import com.wisenut.worker.FacebookWorker;
import com.wisenut.worker.NaverWorker;
import com.wisenut.worker.TwitterWorker;
import com.wisenut.worker.WiseSearchWorker;
import com.wisenut.worker.WiseTeaWorker;
import com.wisenut.worker.YoutubeWorker;

public class OpenAPIProvider {
	
	final static Logger logger = LogManager.getLogger(OpenAPIProvider.class);
	
	public String getMainKeywordsInfo(String article, int start, int pageno) throws Exception{
		WiseTeaWorker teaWorker = new WiseTeaWorker();
		
		return teaWorker.getMainKeywordsInfo(article, start, pageno);
	}
	
	// 1. Model + SF-1 결과 조합. 날짜 조건 없음.
	public String getRecommendedContentsInfoWithSF1Result(String article, int start, int pageno) throws Exception{
		return getRecommendedContentsInfoWithSF1Result(article, start, pageno, "", "");
	}
	
	// 1. Model + SF-1 결과 조합. 날짜 조건 있음.
	public String getRecommendedContentsInfoWithSF1Result(String article, int start, int pageno, String startDate, String endDate) throws Exception{
		ArrayList<HashMap<String,String>> resultList = new ArrayList<HashMap<String,String>>();
		
		WiseTeaWorker teaWorker = new WiseTeaWorker();
		WiseSearchWorker searchWorker = new WiseSearchWorker();
		
		List<Pair<Integer>> keywordList = teaWorker.getMainKeywordsPair(article, start, pageno);
		
		StringBuffer query = new StringBuffer();
        
        for (int i = 0; i < keywordList.size(); i++) {
         	Pair<Integer> item = keywordList.get(i);
 			if (null == item) {
 				continue;
 			}
 			
 			if( query.length() != 0 )
 				query.append("|");
 			
 			query.append(item.key());
 		}
        
        logger.debug("query : " + query.toString());
        
        searchWorker.search(query.toString(), pageno, startDate, endDate);
        
        // 검색한 결과와 tea의 similarDoc 결과를 조합
        List<Pair<Double>> docidList = teaWorker.getRecommendedContentsPair(article, searchWorker.getDOCIDList(), pageno);
        
		for(Pair<Double> p : docidList){
			searchWorker.docidSearch(p.key(), pageno, startDate, endDate);
			
			// DOCID Search에 대한 결과는 한 개이므로 첫번째 결과만 가져와서 add.
			if(searchWorker.getResultList().size()>0){				
				resultList.add(searchWorker.getResultList().get(0));
			}
		}
		
		return StringUtil.objectToString(resultList);
	}
	
	public String getRecommendedContentsInfo(String article, int start, int pageno) throws Exception{
		return getRecommendedContentsInfo(article, start, pageno, "", "");
	}
	
	// 2. Model
	public String getRecommendedContentsInfo(String article, int start, int pageno, String startDate, String endDate) throws Exception{
		ArrayList<HashMap<String,String>> resultList = new ArrayList<HashMap<String,String>>();
		
		WiseTeaWorker teaWorker = new WiseTeaWorker();
		WiseSearchWorker searchWorker = new WiseSearchWorker();
        
        // similarDoc만 사용
        List<Pair<Double>> docidList = teaWorker.getRecommendedContentsPair(article, pageno);
        
        logger.debug("docidList size : " + docidList.size());
        
		for(Pair<Double> p : docidList){
			searchWorker.docidSearch(p.key(), pageno, startDate, endDate);
			
			// DOCID Search에 대한 결과는 한 개이므로 첫번째 결과만 가져와서 add.
			if(searchWorker.getResultList().size()>0){				
				resultList.add(searchWorker.getResultList().get(0));
			}
		}
		
		return StringUtil.objectToString(resultList);
	}
	
	
	public String getOpenAPIResult(String provider, String query, int startPos, int pageNo, String sort){
		WNResultData data = new WNResultData();
		data.setProvider(provider);
		
		if(provider.equals("twitter")){
			TwitterWorker tWorker = new TwitterWorker();
			tWorker.search(query, startPos, pageNo, sort, data);
		}else if(provider.equals("naver")){
			NaverWorker nWorker = new NaverWorker();
			nWorker.search(query, startPos, pageNo, sort, data);
		}else if(provider.equals("daum")){
			DaumWorker dWorker = new DaumWorker();
			dWorker.search(query, startPos, pageNo, sort, data);
		}else if(provider.equals("facebook")){
			FacebookWorker fWorker = new FacebookWorker("group");
			fWorker.search(query, data);
		}else if(provider.equals("youtube")){
			YoutubeWorker yWorker = new YoutubeWorker();
			yWorker.search(query, startPos, pageNo, sort, data);
		}
		return data.toString();
	}
	
	public static void main(String[] args){
		System.out.println("##########################################################");
		System.out.println("               WISENUT OPEN API PROVIDER");
		System.out.println("##########################################################");
		OpenAPIProvider provider = new OpenAPIProvider();
		
		String article = "겨울채비 하세요... 이번 주 맑고 포근 윤우현 기자 whyoon@jbnews.com 11월4째주 충북지방은 대체로 고기압의 영향을 받아 맑겠으며, 기온도 평년보다 조금 높아 포근한 한 주가 될 것으로 전망된다.청주기상대는 20일 충북지방은 고기압의 영향을 받아 맑은 날씨를 보이겠으며, 아침최저 청주2도, 충주 영하1도 등 영하2~영상2도의 분포를 보이겠고, 낮 최고기온은 청주 16도, 충주 14도 등 14~16도로 포근할 것이라고 예보했다. 한편 11월 셋째 휴일인 19일 충북지역은 구름이 낀 흐린 날씨를 보인 가운데 유명산 등에는 다소 한산한 모습을 보였다. 가을 단풍이 모두 떨어진 월악산국립공원 입장객은 2천명으로 지난 주의 절반에 그쳤고 속리산에는 4천명의 등산객이 산행을 즐겼으나 평소보다 적은 수준이었다. 대통령 옛 별장인 청원 청남대에도 휴일 평균 관람객의 50%를 밑도는 2천명만 입장해 초겨울 대청호의 풍광을 감상했다. 청주 상당산성, 청원 문의문화재 단지 등 도내 주요 유원지에서는 두툼한 옷을 입은 가족단위 행락객이 등산, 산책, 외식 등을 하며 휴일의 여유를 즐겼다. 청주 도심 극장가는 대입 수능을 마친 고3학생 등으로 북적거렸고 도시와 농촌가정에서는 김장김치를 담그는 손길이 분주했다."; 
		System.out.println("###### Input Article : "+ article);
		//String strResultData = provider.getOpenAPIResult("youtube", "teamcoco", 1, 10, WNConstants.METAINFO_BY_PROVIDER[WNConstants.TWITTER_ID][WNConstants.SORT_BY_RANK]);
		
		String strResultData;
		try {
			// 키워드(해쉬태그) 추출.
			strResultData = provider.getMainKeywordsInfo(article, 0, 10);
			System.out.println("#1. 키워드(해쉬태그) 추출 : " + strResultData);
			
			// 날짜 조건이 없는 경우.
			strResultData = provider.getRecommendedContentsInfo(article, 0, 10);
			System.out.println("#2-1. 연관기사 추천(날짜 조건 X) : " + strResultData);
			
			// 날짜 조건이 있는 경우
			strResultData = provider.getRecommendedContentsInfo(article, 0, 10, "20150101", "20151231");
			System.out.println("#2-2. 연관기사 추천(날짜 조건 O) : " + strResultData);
			
			// SF-1의 결과를 사용한 연관 기사 추천. 날짜 조건 없는 경우.
			strResultData = provider.getRecommendedContentsInfoWithSF1Result(article, 0, 10);
			System.out.println("#3-1. SF-1 검색 결과를 조합한 연관기사 추천(날짜 조건 X) : " + strResultData);
			
			// SF-1의 결과를 사용한 연관 기사 추천. 날짜 조건 있는 경우.
			strResultData = provider.getRecommendedContentsInfoWithSF1Result(article, 0, 10, "20140101", "20141231");
			System.out.println("#3-2. SF-1 검색 결과를 조합한 연관기사 추천(날짜 조건 O) : " + strResultData);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
