import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;

public class ReplicaDealer extends Thread {
	private Queue<ReplicaItem> replicaQueue = new LinkedList<ReplicaItem>();

	public void run() {
		while (true) {
			try {
				makeReplicas();
				Thread.sleep(Constants.REPLICA_INTERVAL_MILIS);
			} catch (InterruptedException e) {
				System.err.println("The replica dealer was interrupted!");
				e.printStackTrace();
			}
		}
	}

	private void makeReplicas() {
		while (!replicaQueue.isEmpty()) {
			ReplicaItem item = replicaQueue.poll();
			String replicaAddress = NamingServerMain.getAvailableStorageServerForWriting(item.getAddress());
			if (replicaAddress != null) {
				NamingServerUser storage = NamingServerMain.getStorageServerByAddress(item.getAddress());
				storage.send(Constants.TYPE_REPLICA + Constants.DELIMITER + item.getFilePath() + Constants.DELIMITER
						+ replicaAddress);
				try {
					String response = storage.in.readUTF();
					if (response.startsWith(Constants.TYPE_SUCCESS_REPLICA_WRITE)) {
						String[] data = response.split(Constants.DELIMITER);
						String address = data[1];
						String path = data[2];
						File file = new File(path);
						if (file.exists()) {
							String addresses = Files.readAllLines(Paths.get(path)).get(0);
							addresses += System.getProperty("line.separator") + address;
							Files.write(Paths.get(path), addresses.getBytes());
							System.out.println("Successfully created a replica of file '" + path + "'.");
							System.out.println("It can now be found on:");
							System.out.println(addresses);
						} else {
							System.out.println("Failed to index the replica of " + path + " on " + address + " as an index does not exist at all.");
						}
					} else {
						
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void addToQueue(String filePath, String address) {
		ReplicaItem item = new ReplicaItem(filePath, address);
		replicaQueue.add(item);
	}
}
