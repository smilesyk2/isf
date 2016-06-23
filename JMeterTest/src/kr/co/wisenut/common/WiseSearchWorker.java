package kr.co.wisenut.common;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import kr.co.wisenut.util.StringUtil;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import QueryAPI530.Search;

public class WiseSearchWorker {
	//private static final Logger LOGGER = LoggerFactory.getLogger(WiseSearchWorker.class);
	private static final Logger LOGGER = LoggingManager.getLoggerForClass();
	
	public static final String SPACE = " ";
    
    private static final String AND_OPERATOR = " ";
    private static final String OR_OPERATOR = "|";
    private static final String GTE_OPERATOR = ":gte:";
    private static final String LTE_OPERATOR = ":lte:";
    private static final String CONTAINS_OPERATOR = ":contains:";
	
	public static Properties prop;
	public static String searchIP;
	public static int searchPort;
	public static int searchTimeout;

	public static String sort;
	public static String ranking;
	public static String highlight;
	public static String queryAnalyzer;
 
	public static String collectionId;
	public static String searchFields;
	public static String documentFields;
	
	private int ret;
	private int totalResultCount;
	
	private ArrayList<HashMap<String,String>> resultList; // 연관 기사 리스트
	private ArrayList<String> docidList; // 연관 기사 DOCID 리스트
	
	private String errorMsg;
	
	public boolean debug = true;

	public WiseSearchWorker(String ip, int port, String collection, String searchfields, String documentfields) throws Exception{
		searchIP = ip;
		searchPort = port;
		searchTimeout = 10000;
				
		sort = "RANK/DESC";
		ranking = "basic,rpf,10000";
		highlight = "1,1";
		queryAnalyzer = "1,1,1,1";
		
		collectionId = collection;

		searchFields = searchfields;
		documentFields = documentfields;
	}
	
	public void setSearchCondition(Search search, String query, String sort, int page, int pageSize, String filterQuery, String prefixQuery, String searchField) throws Exception{ // , boolean docidSearch
		ret = 0;
		
		ret = search.w3SetCodePage("UTF-8");
		ret = search.w3SetQueryLog(1);
		ret = search.w3SetCommonQuery(query, 0);
		
		LOGGER.debug(" - collection : " + collectionId);
		ret = search.w3AddCollection(collectionId);
		
		String[] rankingArr = ranking.split(",");
		LOGGER.debug(" - ranking : "+rankingArr[0]+", "+rankingArr[1]+", " + rankingArr[2]);
		ret = search.w3SetRanking(collectionId, rankingArr[0], rankingArr[1], Integer.parseInt(rankingArr[2]));
		
		String[] hlArr = highlight.split(",");
		LOGGER.debug(" - highlight : 1,1");
		ret = search.w3SetHighlight(collectionId, Integer.parseInt(hlArr[0]), Integer.parseInt(hlArr[1]));
		
		LOGGER.debug(" - sort : " + sort);
		if( null == sort || "".equals(sort)){
			sort = "RANK/DESC,UID/ASC";
		}else{
			sort += ",UID/ASC";
		}
		ret = search.w3SetSortField(collectionId, sort);
		
		String[] qaArr = queryAnalyzer.split(",");
		LOGGER.debug(" - query analyzer : 1,1,1,1");
		ret = search.w3SetQueryAnalyzer(collectionId, Integer.parseInt(qaArr[0]), Integer.parseInt(qaArr[1]), Integer.parseInt(qaArr[2]), Integer.parseInt(qaArr[3]));
		
		if(query == null || "".equals(query)){				
			LOGGER.debug(" - date range : ");
			ret = search.w3SetDateRange(collectionId, "1970/01/01", "2030/12/31");
		}
		
		LOGGER.debug(" - prefixQuery : " + prefixQuery.toString());
		if(prefixQuery.length()>0){
			ret = search.w3SetPrefixQuery(collectionId, prefixQuery.toString(), 1);
		}
		
		if( null == searchField || "".equals(searchField) || "ALL".equals(searchField) ){
			LOGGER.debug(" - search fields : " + searchFields);				
			ret = search.w3SetSearchField(collectionId, searchFields);
		}else{
			LOGGER.debug(" - search fields : " + searchField);		
			ret = search.w3SetSearchField(collectionId, searchField);
		}
		
		LOGGER.debug(" - document fields : " + documentFields);
		String[] arrDocumentFields = documentFields.split(",");
		for(String dfield : arrDocumentFields){
			String df = dfield;
			if(dfield.contains("/")){
				int snippetlength = Integer.parseInt(dfield.split("/")[1]);
				df = dfield.split("/")[0];

				ret = search.w3AddDocumentField(collectionId, df, snippetlength);
			}else{
				ret = search.w3AddDocumentField(collectionId, df, 0);
			}
		}
		//ret = search.w3SetDocumentField(collectionId, documentFields);
		
		LOGGER.debug(" - page info : " + page + ", " + pageSize);
		ret = search.w3SetPageInfo(collectionId, page, pageSize);
		
		LOGGER.debug(" - filterQuery : " + filterQuery);
		if(filterQuery.length()>0){
			ret = search.w3SetFilterQuery(collectionId, filterQuery);
		}
			
		LOGGER.info(" - search ip : " + searchIP);
		LOGGER.info(" - search port : " + searchPort);
		LOGGER.info(" - search timeout : " + searchTimeout);
		ret = search.w3ConnectServer(searchIP, searchPort, searchTimeout);
		
		ret = search.w3ReceiveSearchQueryResult(0);
		if(ret != 0) {
            LOGGER.error(search.w3GetErrorInfo() + " (Error Code : " + search.w3GetError() + " )");
            errorMsg = search.w3GetErrorInfo() + " (Error Code : " + search.w3GetError() + " )";
            
            throw new Exception(errorMsg);
        }
	}
		
	public void search(String query, String sort, int page, int pageSize, String filterQuery, String prefixQuery, String searchField) throws Exception{
		Search search = new Search();
		
		resultList = new ArrayList<HashMap<String,String>>();
		docidList = new ArrayList<String>();
		
		// property에 지정한 모든 필드 검색
		setSearchCondition(search, query, sort, page, pageSize, filterQuery, prefixQuery, searchField);
		
		totalResultCount = search.w3GetResultTotalCount(collectionId);
		
		int count = search.w3GetResultCount(collectionId);
		for(int i=0; i<count; i++){
			HashMap<String,String> map = new HashMap<String, String>();
			
			map.put("UID", String.valueOf(search.w3GetUid(collectionId,  i)));
			for(String dfield : documentFields.split(",")){
				String df = dfield;
				if(dfield.contains("/")){
					df = dfield.split("/")[0];
				}
				// DOCID 결과값은 별도로 따로 저장.
				if(df.equals("DOCID")){
					docidList.add(search.w3GetField(collectionId, df, i));
				}
				map.put(df, search.w3GetField(collectionId, df, i));
			}
						
			resultList.add(map);
		}
		
		search.w3CloseServer();
	}
	
	public void docidSearch(String sort, int page, int pageSize, String filterQuery, String prefixQuery, String searchField) throws Exception{
		Search search = new Search();
		
		resultList = new ArrayList<HashMap<String,String>>();
		docidList = new ArrayList<String>();
		
		// docid만 검색.
		setSearchCondition(search, "", sort, page, pageSize, filterQuery, prefixQuery, searchField);
		
		totalResultCount = search.w3GetResultTotalCount(collectionId);
		
		int count = search.w3GetResultCount(collectionId);

		String[] documentFieldsArr = documentFields.split(",");
		for(int i=0; i<count; i++){
			HashMap<String,String> map = new HashMap<String, String>();
			
			for(String dfield : documentFieldsArr){
				String df = dfield;
				if(dfield.contains("/")){
					df = dfield.split("/")[0];
				}
				// DOCID 결과값은 별도로 따로 저장.
				if(df.equals("DOCID")){
					docidList.add(search.w3GetField(collectionId, df, i));
				}
				map.put(df, search.w3GetField(collectionId, df, i));
			}
						
			resultList.add(map);
		}
				
		search.w3CloseServer();
	}
	
	public String arkTrans(String arkRequestUrl, String param){
		StringBuffer receiveMsg = new StringBuffer();
		HttpURLConnection uc = null;
		try {
			URL servletUrl = new URL(arkRequestUrl);
			uc = (HttpURLConnection) servletUrl.openConnection();
			uc.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			uc.setRequestMethod("POST");
			uc.setDoOutput(true);
			uc.setDoInput(true);
			uc.setUseCaches(false);
			uc.setDefaultUseCaches(false);
			DataOutputStream dos = new DataOutputStream (uc.getOutputStream());
			dos.write(param.getBytes());
			dos.flush();
			dos.close();
			
			int errorCode = 0;
			// -- Network error check
			//System.out.println("[URLConnection Response Code] " + uc.getResponseCode());
			if (uc.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String currLine = "";
                // UTF-8. ..
                BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
                while ((currLine = in.readLine()) != null) {
                	receiveMsg.append(currLine).append("\r\n");
                }
                in.close();
			} else {
				errorCode = uc.getResponseCode();
				receiveMsg.append("error code : " + errorCode);
				
				return receiveMsg.toString();
			}
		} catch(Exception ex) {
			LOGGER.error(StringUtil.getSStackTraceElement(ex.getStackTrace()));
		} finally {
			uc.disconnect();
		}
		
		return receiveMsg.toString();
	}
	
	public ArrayList<String> getDocidList(){
		return docidList;
	}
	
	public ArrayList<HashMap<String,String>> getResultList(){
		return resultList;
	}
	
	public int getTotalResultCount() {
		return totalResultCount;
	}
	
	public int getRet(){
		return ret;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public String makeFilterQuery(String dateKind, String startDate, String endDate){
		/*
    	 * filterQuery 형식
    	 * <[필드]:gte:[범위]> <[범위]:lte:[범위]>
    	 * 
    	 * (ex) <CRT_DTIME:gte:20150101> <CRT_DTIME:lte:20151231>
    	 */
    	StringBuffer filterQuery = new StringBuffer();
    	String[] arrDateKind = (dateKind !=null && dateKind.contains(","))? dateKind.split(","):new String[]{dateKind};
    	
    	for(String dateField : arrDateKind){
    		filterQuery.append("(");
    		if( null != startDate && !"".equals(startDate) ){    			
    			filterQuery.append("<").append(dateField).append(GTE_OPERATOR).append(startDate).append(">");
    			filterQuery.append(AND_OPERATOR);
    		}
    		if( null != endDate && !"".equals(endDate) ){    			
    			filterQuery.append("<").append(dateField).append(LTE_OPERATOR).append(endDate).append(">");
    		}
    		filterQuery.append(")").append(OR_OPERATOR);
    	}
    	
    	return filterQuery.toString().replaceAll("\\w$", "").replaceAll("\\"+OR_OPERATOR+"$", "");
	}
	
	public String makePrefixQuery(HashMap<String,String> map){
		/*
    	 * prefixQuery 형식
    	 * [필드]:값1|값2|값3|....|값n^[필드]:값1|값2
    	 * 
    	 * (ex) ARTCL_KIND_CD:0001,CRTOR_NM:성유경,RPTR_NM:성유경
    	 */
    	StringBuffer prefixQuery = new StringBuffer();
    	
    	Iterator<String> iter = map.keySet().iterator();
    	while(iter.hasNext()){
    		String prefixField = iter.next();
    		String value = map.get(prefixField);
    		
    		if(prefixField.indexOf(",") != -1){
    			String[] fieldArr = prefixField.split(",");
    			
    			StringBuffer tempBuffer = new StringBuffer();
    			for(String field : fieldArr){
    				tempBuffer.append("<").append(field).append(CONTAINS_OPERATOR).append(value).append(">");
    				tempBuffer.append(OR_OPERATOR);
    			}
    			// 마지막에 붙은 | 기호를 없앰.
    			prefixQuery.append(tempBuffer.toString().replaceAll("\\|$", ""));
    		}else{
    			prefixQuery.append("<").append(prefixField).append(CONTAINS_OPERATOR).append(value).append(">");
    		}
    		
    		prefixQuery.append(AND_OPERATOR);
    	}
    	
    	
    	return prefixQuery.toString().replaceAll("\\w$", "");
	}
}
