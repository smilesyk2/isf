package kr.co.wisenut.common;

import java.util.ArrayList;
import java.util.HashMap;

public class WNCollection {
	
	public static String CHARSET = "UTF-8";
	public static int PAGE_SIZE = 10; //view page list count
	public static float COMPOSITE_THRESHOLD = (float)0.8;

    public static String SEARCH_IP="127.0.0.1";
    public static int SEARCH_PORT=9000;
    
    public static int COLLECTION_NAME = 0;
    public static int SORT_FIELD = 1;
    public static int RESULT_FIELD = 2;
    public static int SESSION_INFO = 3;
    public static int DATE_RANGE = 4;
    public static int SCORE_RANGE = 5;
    public static int PREFIX_QUERY = 6;
    public static int FILTER_OPERATION = 7;    
    
    public static HashMap<String,HashMap> collectionMap = new HashMap<String,HashMap>();

	public static String[] COLLECTIONS = new String[]{"plagcol"};
	public static String[] COLLECTIONS_NAME = new String[]{"표절검색대상컬렉션"};
	
	public String[][] COLLECTION_INFO = null;
	
	public WNCollection() {
		
		for(int i=0; i<COLLECTIONS.length; i++)
		{
			HashMap collInfoMap = new HashMap();
			
			collInfoMap.put(COLLECTION_NAME, "plagcol");
			collInfoMap.put(SORT_FIELD, "SCORE DESC");
			collInfoMap.put(RESULT_FIELD, "DOCID,Date,REPORT_SUBMIT_NUMBER,LECTURE_NUMBER,TASK_CODE,REPORT_NAME,KIND_CODE,SUBMIT_START_DATE,SUBMIT_END_DATE,SUBMIT_FORM_CODE,REPORT_ATTACH,REPORT_CONTENT,APPRAISAL_RATE,SUPPLEMENT_APPRAISAL_RATE,GRADE_OPEN_YN,USER_ID,USER_NAME,DELETE_YN,REG_USER_ID,REG_DATE,REG_IP,UPDATE_USER_ID,UPDATE_DATE,UPDATE_IP,READ_COUNT,LMS_UPLOAD_KEY,LCMS_UPLOAD_KEY");
			collInfoMap.put(SESSION_INFO, "");
			collInfoMap.put(DATE_RANGE, "");
			collInfoMap.put(SCORE_RANGE, "");
			collInfoMap.put(PREFIX_QUERY, "");
			collInfoMap.put(FILTER_OPERATION, "");
			
			collectionMap.put(COLLECTIONS[i], collInfoMap);
		}
	}
}
