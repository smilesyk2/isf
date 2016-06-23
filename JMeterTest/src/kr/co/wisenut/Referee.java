package kr.co.wisenut;
import kr.co.wisenut.common.WNCollection;
import RefAPI210.*;

public class Referee {

	String collection;
	RefAPI210.Search search;
	
	Referee(){
		collection = WNCollection.COLLECTIONS[0];
	}
	
	public static void main(String[] args){
		
		Referee referee = new Referee();
		
		referee.search = new RefAPI210.Search();
		referee.setRefAPI();
		
		referee.search.wBrowsingQuery();
	}
	
	public String setRefAPI()
	{
		int ret = 0;
		String errMsg = "";

		// - charset 지정
		ret = search.wSetCharset( WNCollection.CHARSET );
		if( ret < 0 )
		{
			errMsg += search.wGetErrorMessage();
			errMsg += "\n";
		}

		// - 컬렉션 지정
		ret = search.wSetTargetCollection( collection );
		if( ret < 0 )
		{
			errMsg += search.wGetErrorMessage();
			errMsg += "\n";
		}

		// 정렬 필드 지정
		String sortField = (String) WNCollection.collectionMap.get(collection).get(WNCollection.SORT_FIELD);
		ret = search.wSetSortField( sortField );
		if( ret < 0 )
		{
			errMsg += search.wGetErrorMessage();
			errMsg += "\n";
		}

		// 결과 필드 설정
		String resultField = (String) WNCollection.collectionMap.get(collection).get(WNCollection.RESULT_FIELD);
		if( !resultField.equals("") )
		{
			ret = search.wSetResultField( resultField );
			if( ret < 0 )
			{
				errMsg += search.wGetErrorMessage();
				errMsg += "\n";
			}
		}

		return errMsg;
	}
	
}
