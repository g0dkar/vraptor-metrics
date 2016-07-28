package g0dkar.com.vraptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.view.Results;

/**
 * A Controller to share the collected metrics
 * @author Rafael Lins
 *
 */
@Controller
public class MetricsController {
	@Inject
	public Result result;
	
	@Inject
	public Metrics metrics;
	
	/**
	 * <p>{@code GET /vraptor-metrics}
	 * <p>Returns all the {@link Metrics} collected so far, along with a timestamp
	 */
	@Get({ "/vraptor-metrics", "/vaptor-metrics/{metric:.+}" })
	public void getMetrics(final String metric) {
		if (metric == null || metric.trim().length() == 0) {
			final Map<String, Object> result = new HashMap<>(6);
			result.put("timestamp", System.currentTimeMillis());
			result.put("counter", new HashMap<>());
			result.put("gauge", new HashMap<>());
			result.put("histogram", new HashMap<>());
			result.put("meter", new HashMap<>());
			result.put("timer", new HashMap<>());
			
			for (final Entry<String, Metric> entry : metrics.metrics().getMetrics().entrySet()) {
				final Metric m = entry.getValue();
				
				if (m instanceof Histogram) {
					final Map<String, Object> histograms = (Map<String, Object>) result.get("histogram");
					histograms.put(entry.getKey(), snapshot(((Histogram) m).getSnapshot()));
				}
				else if (m instanceof Timer) {
					final Map<String, Object> timers = (Map<String, Object>) result.get("timer");
					timers.put(entry.getKey(), timer((Timer) m));
				}
				else {
					final Map<String, Object> map = (Map<String, Object>) result.get(m.getClass().getSimpleName().toLowerCase());
					if (map != null) {
						map.put(entry.getKey(), entry.getValue());
					}
				}
			}
				
			this.result.use(Results.json()).withoutRoot().from(result).serialize();
		}
		else {
			final Metric foundMetric = metrics.metrics().getMetrics().get(metric);
			if (foundMetric instanceof Histogram) {
				result.use(Results.json()).withoutRoot().from(snapshot(((Histogram) foundMetric).getSnapshot())).serialize();
			}
			else if (foundMetric instanceof Timer) {
				final Timer timerMetric = (Timer) foundMetric;
				final Map<String, Object> timer = new HashMap<>(6);
				timer.put("count", timerMetric.getCount());
				timer.put("meanRate", timerMetric.getMeanRate());
				timer.put("oneMinuteRate", timerMetric.getOneMinuteRate());
				timer.put("fiveMinuteRate", timerMetric.getFiveMinuteRate());
				timer.put("fifteenMinuteRate", timerMetric.getFifteenMinuteRate());
				timer.put("snapshot", snapshot(timerMetric.getSnapshot()));
				
				result.use(Results.json()).withoutRoot().from(foundMetric).serialize();
			}
			else {
				result.use(Results.json()).withoutRoot().from(foundMetric).serialize();
			}
		}
	}
	
	/**
	 * Builds a {@link Timer} representation. This is to prevent Gson from serializing the whole {@link Snapshot#getValues() values set}.
	 * @param snapshot The Snapshot to serialize
	 * @return A Map with the relevant Snapshot data
	 * @see Snapshot
	 */
	private Map<String, Object> timer(final Timer timer) {
		final Map<String, Object> timerMap = new HashMap<>(6);
		timerMap.put("count", timer.getCount());
		timerMap.put("meanRate", timer.getMeanRate());
		timerMap.put("oneMinuteRate", timer.getOneMinuteRate());
		timerMap.put("fiveMinuteRate", timer.getFiveMinuteRate());
		timerMap.put("fifteenMinuteRate", timer.getFifteenMinuteRate());
		timerMap.put("snapshot", snapshot(timer.getSnapshot()));
		
		return timerMap;
	}
	
	/**
	 * Builds a {@link Snapshot} representation. This is to prevent Gson from serializing the whole {@link Snapshot#getValues() values set}.
	 * @param snapshot The Snapshot to serialize
	 * @return A Map with the relevant Snapshot data
	 * @see Snapshot
	 */
	private Map<String, Object> snapshot(final Snapshot snapshot) {
		final Map<String, Object> data = new HashMap<>(10);
		data.put("pct75", snapshot.get75thPercentile());
		data.put("pct95", snapshot.get95thPercentile());
		data.put("pct98", snapshot.get98thPercentile());
		data.put("pct99", snapshot.get99thPercentile());
		data.put("pct999", snapshot.get999thPercentile());
		data.put("max", snapshot.getMax());
		data.put("min", snapshot.getMin());
		data.put("mean", snapshot.getMean());
		data.put("median", snapshot.getMedian());
		data.put("stddev", snapshot.getStdDev());
		
		return data;
	}
}
