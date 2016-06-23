package kr.co.wisenut.perftest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import kr.co.wisenut.common.WiseSearchWorker;
import kr.co.wisenut.common.WiseTeaWorker;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.wisenut.tea20.types.Pair;

/**
 * 샘플 클래스
 * @author dosuser
 *
 */
public class RecommendTest extends AbstractJavaSamplerClient {
	private static final Logger LOG = LoggingManager.getLoggerForClass();

    /**
     * A mask to be applied to the current time in order to add a semi-random
     * component to the sleep time.
     */
    /** The label to store in the sample result. */
    private String label;
	
	/** The default value of the Label parameter. */
    // private static final String LABEL_DEFAULT = "JavaTest";
    /** The name used to store the Label parameter. */
    private static final String LABEL_NAME = "Label";

    /** The response message to store in the sample result. */
    private String responseMessage;

    /** The default value of the ResponseMessage parameter. */
    private static final String RESPONSE_MESSAGE_DEFAULT = "";

    /** The name used to store the ResponseMessage parameter. */
    private static final String RESPONSE_MESSAGE_NAME = "ResponseMessage";

    /** The response code to be stored in the sample result. */
    private String responseCode;

    /** The default value of the ResponseCode parameter. */
    private static final String RESPONSE_CODE_DEFAULT = "";

    /** The name used to store the ResponseCode parameter. */
    private static final String RESPONSE_CODE_NAME = "ResponseCode";

    /** The sampler data (shown as Request Data in the Tree display). */
    private String samplerData;

    /** The default value of the SamplerData parameter. */
    private static final String SAMPLER_DATA_DEFAULT = "";

    /** The name used to store the SamplerData parameter. */
    private static final String SAMPLER_DATA_NAME = "SamplerData";

    /** Holds the result data (shown as Response Data in the Tree display). */
    private String resultData;

    /** The default value of the ResultData parameter. */
    private static final String RESULT_DATA_DEFAULT = "";

    /** The name used to store the ResultData parameter. */
    private static final String RESULT_DATA_NAME = "ResultData";
	
    /* 추천엔진 관련 파라미터 변수 */
	private String TEA_IP;
	private int TEA_PORT;
	private String SF1_IP;
	private int SF1_PORT;
	
	private String COLLECTION;
	private String SEARCH_FIELDS;
	private String DOCUMENT_FIELDS;
	
	private String CONTENTS;
	private int PAGE_NO;
	
	/*
     * Utility method to set up all the values
     */
    private void setupValues(JavaSamplerContext context) {
    	
    	responseMessage = context.getParameter(RESPONSE_MESSAGE_NAME, RESPONSE_MESSAGE_DEFAULT);

        responseCode = context.getParameter(RESPONSE_CODE_NAME, RESPONSE_CODE_DEFAULT);

        label = context.getParameter(LABEL_NAME, "");
        if (label.length() == 0) {
            label = context.getParameter(TestElement.NAME); // default to name of element
        }

        samplerData = context.getParameter(SAMPLER_DATA_NAME, SAMPLER_DATA_DEFAULT);

        resultData = context.getParameter(RESULT_DATA_NAME, RESULT_DATA_DEFAULT);

    	TEA_IP = context.getParameter("teaip", "10.113.38.22");
    	TEA_PORT = context.getIntParameter("teaport", 11000);
    	
    	SF1_IP = context.getParameter("sf1ip", "10.113.38.22");
    	SF1_PORT = context.getIntParameter("sf1port", 7000);
    	
    	COLLECTION = context.getParameter("collection", "article");
    	
    	SEARCH_FIELDS = context.getParameter("searchfields");
    	DOCUMENT_FIELDS = context.getParameter("documentfields");
    	
    	CONTENTS = context.getParameter("contents", "");
    	PAGE_NO = context.getIntParameter("pagesize", 10);
    }
    
    @Override
    public void setupTest(JavaSamplerContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(whoAmI() + "\tsetupTest()");
            listParameters(context);
        }
    }
    
    @Override
    public Arguments getDefaultParameters() {
        Arguments params = new Arguments();
        params.addArgument("teaip", "10.113.38.22");
        params.addArgument("teaport", String.valueOf(11000));
        params.addArgument("sf1ip", "10.113.38.22");
        params.addArgument("sf1port", String.valueOf(7000));
        params.addArgument("collection", "article");
        params.addArgument("searchfields", "CONTENT_PLAIN");
        params.addArgument("documentfields", "TITLE,CONTENT_PLAIN");
        params.addArgument("contents", "");
        params.addArgument("pagesize", "");
        return params;
    }

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		setupValues(context);
		
		SampleResult results = new SampleResult();
		
		results.setResponseCode(responseCode);
        results.setResponseMessage(responseMessage);
        results.setSampleLabel(label);

        if (samplerData != null && samplerData.length() > 0) {
            results.setSamplerData(samplerData);
        }

        if (resultData != null && resultData.length() > 0) {
            results.setResponseData(resultData, null);
            results.setDataType(SampleResult.TEXT);
        }
		
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> runTest start");
		try {
			// Record sample start time.
			results.sampleStart();
			
			/******************************** TEST START *************************************/ 
			
			WiseTeaWorker teaWorker = new WiseTeaWorker(TEA_IP, TEA_PORT, COLLECTION);
			WiseSearchWorker searchWorker = new WiseSearchWorker(SF1_IP, SF1_PORT, COLLECTION, SEARCH_FIELDS, DOCUMENT_FIELDS);
			
			List<Pair<Double>> docidList = new ArrayList<Pair<Double>>();
			int totalResultCount = 0;
			
			// 기사 길이에 따라 모델, 모델+sf1을 구분해서 가져옴.
	    	if(CONTENTS.length()>200){ // 모델만 사용.
	    		// similarDoc만 사용
	        	docidList = teaWorker.getRecommendedContentsPair(COLLECTION, CONTENTS, PAGE_NO);
	        	totalResultCount = teaWorker.getTotalRecommendedMediaCount();
	    	}else{ // 모델 + SF1 사용
	    		// 입력 받은 기사에서 주제어를 추출.
	    		List<Pair<Integer>> keywordList = teaWorker.getMainKeywordsPair(CONTENTS);
	        	StringBuffer query = new StringBuffer();
	           
	        	// 추출한 주제어를 OR 연산자(|)로 연결.
	            for (int i = 0; i < keywordList.size(); i++) {
	             	Pair<Integer> item = keywordList.get(i);
	     			if (null == item) continue;
	     			
	     			if( query.length() != 0 )
	     				query.append("|");
	     			
	     			query.append(item.key());
	     		}
	            
	            HashMap<String,String> prefixMap = new HashMap<String,String>();
	            prefixMap.put("alias", COLLECTION);
	            
	            // OR 연산자로 연결한 쿼리로 SF-1에 검색.
	            searchWorker.search(query.toString(), "", 0, PAGE_NO, "", "", "ALL");
	            
	            // 검색 결과와 tea의 similarDoc 결과를 조합. 검색 결과 중 기사(article)의 결과만 리스트로 제공
	            docidList = teaWorker.getRecommendedContentsPair(COLLECTION, CONTENTS, searchWorker.getDocidList());
	            totalResultCount = teaWorker.getTotalRecommendedMediaCount();
	    	}
			
	    	StringBuffer keyBuffer = new StringBuffer();
    		HashMap<String,String> map = new HashMap<String, String>();
    		
	    	for(Pair<Double> p : docidList){
	    		keyBuffer.append(p.key()).append("|");
	    	}
	    	
	    	if(docidList.size()>0){	    		
	    		map.put("DOCID", keyBuffer.toString().replaceAll("\\|$", ""));
	    	}
    		
    		searchWorker.search("", "", 0, PAGE_NO, "", searchWorker.makePrefixQuery(map), "ALL");
    		
    		totalResultCount = searchWorker.getTotalResultCount();
    		
			// DOCID Search에 대한 결과는 한 개이므로 첫번째 결과만 가져와서 add.
    		StringBuffer resultSb = new StringBuffer();
    		resultSb.append("####################################################################").append("\n");
	 		resultSb.append(CONTENTS).append("\n");
	 		resultSb.append("####################################################################").append("\n");
			if(searchWorker.getResultList().size()>0){
				//for(HashMap<String,String> resultMap : searchWorker.getResultList()){
				for(int cnt=1; cnt<=searchWorker.getResultList().size(); cnt++){
					HashMap<String,String> resultMap = searchWorker.getResultList().get(cnt-1);
					
					resultSb.append("- NO : " + cnt + "\n");
					resultSb.append("- UID : " + resultMap.get("UID") + "\n");
					resultSb.append("- DOCID : " + resultMap.get("DOCID") + "\n");
					resultSb.append("- TITLE : " + resultMap.get("TITLE") + "\n");
					resultSb.append("- CONTENTS : " + resultMap.get("CONTENT_PLAIN") + "\n");
					resultSb.append("\n\n");
				}
				
				results.setSamplerData(resultSb.toString());
				results.setBodySize(totalResultCount);
				results.setResponseData(resultSb.toString().getBytes());
			}
			
			/******************************** TEST END *************************************/
			
			results.setSuccessful(true);
		}catch (Exception e) {
			getLogger().error("SleepTest_bak: error during sample", e);
			results.setSuccessful(false);
		} finally {
			results.sampleEnd();
		}
		
		if (LOG.isDebugEnabled()) {
            LOG.debug(whoAmI() + "\trunTest()" + "\tTime:\t" + results.getTime());
            listParameters(context);
        }

		System.out.println("runTest finish >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		return results;
	}
	

    /**
     * Dump a list of the parameters in this context to the debug log.
     * Should only be called if debug is enabled.
     *
     * @param context
     *            the context which contains the initialization parameters.
     */
    private void listParameters(JavaSamplerContext context) {
        Iterator<String> argsIt = context.getParameterNamesIterator();
        while (argsIt.hasNext()) {
            String name = argsIt.next();
            LOG.debug(name + "=" + context.getParameter(name));
        }
    }


    /**
     * Generate a String identifier of this test for debugging purposes.
     *
     * @return a String identifier for this test instance
     */
    private String whoAmI() {
        StringBuilder sb = new StringBuilder();
        sb.append(Thread.currentThread().toString());
        sb.append("@");
        sb.append(Integer.toHexString(hashCode()));
        return sb.toString();
    }
}