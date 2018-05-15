import java.io.Serializable;
public class SimpleWrite extends Message implements Serializable {
	public Object key;
	public ValueClock value;
	//this will only be used if we use W > 1
	public Integer responsePort;
	SimpleWrite(Object key, ValueClock value, Integer responsePort)
	{
		this.key = key;
		this.value = value;
		this.responsePort = responsePort;
	}
}