

public class MachineStats {
String topic;
public String getTopic() {
	return topic;
}
public void setTopic(String topic) {
	this.topic = topic;
}
public int getUnique_number() {
	return unique_number;
}
public void setUnique_number(int unique_number) {
	this.unique_number = unique_number;
}
public MachineStats(String topic, int unique_number) {
	super();
	this.topic = topic;
	this.unique_number = unique_number;
}
int unique_number;
}
