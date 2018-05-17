import java.io.Serializable;
public class SimpleishRead extends SimpleRead implements Serializable {
	Integer uniqueKey;
	SimpleishRead(Object key, Integer responsePort, Integer uniqueKey)
	{
		super(key, responsePort);
		this.uniqueKey = uniqueKey;
	}
}