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
public class NERTest extends AbstractJavaSamplerClient {
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
			//WiseSearchWorker searchWorker = new WiseSearchWorker(SF1_IP, SF1_PORT, COLLECTION, SEARCH_FIELDS, DOCUMENT_FIELDS);
			
			System.out.println(CONTENTS);
	    	
	        // 1. 입력한 기사와 유사 기사들을 찾아서 한 덩어리로 만듦.        
	    	/*List<Pair<Double>> similarDocumentList = teaWorker.getRecommendedContentsPair(COLLECTION, CONTENTS, PAGE_NO);
	    	StringBuffer docidSb = new StringBuffer();
	        for (int i = 0; i < similarDocumentList.size(); i++) {
	        	Pair<Double> item = similarDocumentList.get(i);
	        	if (null == item) {
	 				continue;
	 			}
	        	
	        	docidSb.append(item.key()+"|");
	        }
	        
	        if(docidSb.length() > 0){
	        	HashMap<String,String> map = new HashMap<String, String>();
				map.put("DOCID", docidSb.toString().replaceAll("\\|$", ""));
				
				System.out.println("prefix : " + searchWorker.makePrefixQuery(map));
				
				searchWorker.docidSearch("", 0, PAGE_NO, "", searchWorker.makePrefixQuery(map), "");
	        }else{
	        	searchWorker.docidSearch("", 0, PAGE_NO, "", "", "");
	        }
	        
	        String similarContent = "";
	        ArrayList<HashMap<String,String>> resultMapList = searchWorker.getResultList();
			
			if( null != resultMapList ){    			
				for(HashMap<String,String> thisMap : resultMapList){
					similarContent += thisMap.get(SEARCH_FIELDS); // 검색 결과 중 CONTENT_PLAIN의 내용만 덧붙임. 
				}
			}*/
	        
	        // 2. 유사 기사 덩어리와 입력된 기사를 개체명 조회 메소드에 넣어서 결과를 받아옴.
	 		List<Pair<Integer>> nerPairList = teaWorker.getNerPair( COLLECTION, CONTENTS, "10" );
	 		
	 		StringBuffer resultSb = new StringBuffer();
	 		resultSb.append("####################################################################").append("\n");
	 		resultSb.append(CONTENTS).append("\n");
	 		resultSb.append("####################################################################").append("\n");
	 		for (int i = 0; i < nerPairList.size(); i++) {
				resultSb.append("- NO : " + (i+1) + "\n");
				resultSb.append("- NER : " + nerPairList.get(i).key() + "\n");
				resultSb.append("- Score : " + nerPairList.get(i).value() + "\n");
				resultSb.append("\n\n");
	 		}
	 		
	 		results.setSamplerData(resultSb.toString());
			results.setResponseData(resultSb.toString().getBytes());
			
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