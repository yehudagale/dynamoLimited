import java.io.Serializable;
public class SimpleReadResponse extends SimpleishRead implements Serializable {
	ValueClock value;
	SimpleReadResponse(Object key, ValueClock value, Integer responsePort, Integer uniqueKey)
	{
		super(key, responsePort, uniqueKey);
		this.value = value;
	}	
}