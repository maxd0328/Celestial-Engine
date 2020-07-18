package celestial.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import celestial.core.EngineRuntime;

public final class PerformanceLogger {
	
	private final LinkedHashMap<String, Long> inProgress;
	private final LinkedHashMap<String, ArrayList<Long>> finished;
	
	private int framesToCapture = -1;
	private int initialFrameCount = 0;
	
	public PerformanceLogger() {
		this.inProgress = new LinkedHashMap<String, Long>();
		this.finished = new LinkedHashMap<String, ArrayList<Long>>();
	}
	
	public void captureFrame() {
		captureFrames(1);
	}
	
	public void captureFrames(int frameCount) {
		inProgress.clear();
		finished.clear();
		this.framesToCapture = frameCount;
		this.initialFrameCount = frameCount;
	}
	
	public boolean isResultAvailable() {
		return framesToCapture == 0;
	}
	
	public Map<String, Long> getResults() {
		if(!isResultAvailable())
			throw new IllegalStateException("No results available");
		
		Map<String, Long> results = new LinkedHashMap<String, Long>();
		for(String identifier : finished.keySet())
			results.put(identifier, average(finished.get(identifier)));
		
		inProgress.clear();
		finished.clear();
		framesToCapture = -1;
		
		return results;
	}
	
	public int getInitialFrameCount() {
		return initialFrameCount;
	}
	
	protected void startTask(String identifier) {
		if(framesToCapture > 0)
			inProgress.put(identifier, EngineRuntime.timeNsec());
	}
	
	protected void finishTask(String identifier) {
		if(framesToCapture <= 0) return;
		if(!inProgress.containsKey(identifier))
			throw new IllegalArgumentException("No such task");
		
		long start = inProgress.get(identifier);
		long now = EngineRuntime.timeNsec();
		
		if(finished.containsKey(identifier))
			finished.get(identifier).add(now - start);
		else {
			ArrayList<Long> times = new ArrayList<Long>();
			times.add(now - start);
			finished.put(identifier, times);
		}
	}
	
	protected void nextFrame() {
		if(framesToCapture > 0) --framesToCapture;
	}
	
	private long average(Collection<Long> longs) {
		long avg = 0;
		for(Long value : longs)
			avg += value;
		avg /= longs.size();
		return avg;
	}
	
	public static void displayResults(Map<String, Long> results, int frameCount) {
		System.out.println("FRAME DUMP --- " + frameCount + " FRAME(S)");
		System.out.println("Stats:");
		System.out.println("TASK ID               LENGTH (NSEC)       TIMESTAMP (NSEC)    ");
		System.out.println("--------------------------------------------------------------");
		
		int timestamp = 0;
		for(String id : results.keySet()) {
			System.out.printf("%-22s%-20d%-20d\n", id, results.get(id), timestamp);
			timestamp += results.get(id);
		}
	}
	
}
