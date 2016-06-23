package kr.co.wisenut.perftest;

import java.util.Iterator;
import java.util.List;

import kr.co.wisenut.common.WiseTeaWorker;
import kr.co.wisenut.common.WiseClassifierWorker;

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
public class ClassificationTest extends AbstractJavaSamplerClient {
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
	private String TEA_COLLECTION;
	
	private String CF_IP;
	private int CF_PORT;
	private String CF_COLLECTION;
	
	private String CONTENTS;
	
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
    	TEA_COLLECTION = context.getParameter("teacollection", "article");
    	
    	CF_IP = context.getParameter("cfip", "10.113.38.22");
    	CF_PORT = context.getIntParameter("cfport", 9000);
    	CF_COLLECTION = context.getParameter("cfcollection", "KBS");
    	
    	CONTENTS = context.getParameter("contents", "");
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
        params.addArgument("teacollection", "article");
        params.addArgument("cfip", "10.113.38.22");
        params.addArgument("cfport", String.valueOf(9000));
        params.addArgument("cfcollection", "KBS");
        params.addArgument("contents", "");
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
			
			WiseTeaWorker teaWorker = new WiseTeaWorker(TEA_IP, TEA_PORT, TEA_COLLECTION);
	    	WiseClassifierWorker cfWorker = new WiseClassifierWorker(CF_IP, CF_PORT, CF_COLLECTION);
			
			// 먼저 해당 기사의 키워드 리스트를 얻어옴.
			List<Pair<Integer>> keywordsList = teaWorker.getMainKeywordsPair(CONTENTS);
			StringBuffer keywordsBuffer = new StringBuffer();
			for(Pair<Integer> keywords : keywordsList){
				keywordsBuffer.append(keywords.key()).append(":").append(keywords.value());
				keywordsBuffer.append("^");
			}
			
			System.out.println("keywords list :" + keywordsBuffer.toString().replaceAll("\\^$", ""));
			
			// 키워드 리스트를 parameter로 넣음.
			List<Pair<Double>> categoriesPair = cfWorker.getRecommendedCategoryPair(keywordsBuffer.toString().replaceAll("\\^$", ""));
			//List<Pair<Double>> categoriesPair = getDummyData("category");// cf 안정화 전까지 dummy data로 대체
			
			// 카테고리 추천 결과를 세팅.
			StringBuffer resultSb = new StringBuffer();
			resultSb.append("####################################################################").append("\n");
	 		resultSb.append(CONTENTS).append("\n");
	 		resultSb.append("####################################################################").append("\n");
			for(int cnt=1; cnt<=categoriesPair.size(); cnt++){
				Pair<Double> category = categoriesPair.get(cnt-1);
				
				if( null != category){					
					resultSb.append("- NO : " + cnt + "\n");
					resultSb.append("- Category : " + category.key() + "\n");
					resultSb.append("- Score : " + category.value() + "\n");
					resultSb.append("\n\n");
				}
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