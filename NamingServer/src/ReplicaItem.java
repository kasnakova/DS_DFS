
public class ReplicaItem {
	private String filePath;
	private String address;
	
	public ReplicaItem(String filePath, String address){
		this.filePath = filePath;
		this.address = address;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
}
