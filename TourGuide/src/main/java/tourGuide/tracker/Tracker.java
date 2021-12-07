package tourGuide.tracker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tourGuide.service.TourGuideService;
import tourGuide.user.User;

public class Tracker extends Thread {
	private Logger logger = LoggerFactory.getLogger(Tracker.class);
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
	private final ExecutorService executorService;
	private final TourGuideService tourGuideService;
	private boolean stop = false;
	private int partitionSize;
	private List<User> users;

	public Tracker(TourGuideService tourGuideService) {
		this.tourGuideService = tourGuideService;
		users = tourGuideService.getAllUsers();
		partitionSize = getPartitionSize(users.size());
		// Setting the total amount of Threads.
		executorService = Executors.newFixedThreadPool(getNumberOfThreads(users.size()));
		executorService.submit(this);
	}

	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		stop = true;
		executorService.shutdownNow();
	}

	@Override
	public void run() {
		StopWatch stopWatch = new StopWatch();
		while (true) {
			if (Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}

			logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
			stopWatch.start();

			// Splitting the users' list into smaller chunks (partitions).
			// https://stackoverflow.com/a/41898691
			List<List<User>> partitions = new ArrayList<>();
			for (int i = 0; i < users.size(); i += partitionSize) {
				partitions.add(users.subList(i, Math.min(i + partitionSize, users.size())));
			}

			// Creating Callable tasks. Each task will process a chunck of users' list
			// (partition).
			// The list of tasks will be put into executorService.invokeAll method (to start
			// the processing)
			// Each task will be processed by a separate Thread.
			// E.g. if we have 10 000 users we will have the list of 10 tasks processing
			// 1000 users each.
			// https://www.baeldung.com/java-executor-service-tutorial
			List<Callable<List<User>>> tasks = new ArrayList<>();

			partitions.forEach(partition -> {
				Callable<List<User>> task = new Callable<List<User>>() {
					@Override
					public List<User> call() throws Exception {
						partition.forEach(u -> tourGuideService.trackUserLocation(u));
						return partition;
					}
				};
				tasks.add(task);
			});

			try {
				executorService.invokeAll(tasks);
			} catch (InterruptedException ex) {
				logger.debug("executorService.invokeAll was interrupted");
				ex.printStackTrace();
			}

			stopWatch.stop();
			logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
			stopWatch.reset();
			try {
				logger.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public int getNumberOfThreads(int totalUsers) {
			// Here we always round up to next whole value.
			// If there are 2999 users we will have 3 threads.
			// If there are 2001 users we will have 3 threads too.
			Double totalUsersDouble = Double.valueOf(totalUsers);
			Double partitionDouble = Double.valueOf(partitionSize);
			BigDecimal threadsNum = BigDecimal.valueOf(totalUsersDouble / partitionDouble);
			BigDecimal setScale = threadsNum.setScale(0, RoundingMode.UP);
			int result = setScale.intValueExact();
			return result + 3; // Adding extra 3 Threads just to be sure we have enough.
	}

	public int getPartitionSize(int totalUsers) {
		if (totalUsers <= 1000) {
			return 100;
		} else {
			return 1000;
		}
	}
}
