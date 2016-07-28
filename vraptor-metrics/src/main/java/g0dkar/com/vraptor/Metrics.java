package g0dkar.com.vraptor;

import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * Providers a central {@link MetricRegistry metrics} point for the whole application
 * 
 * @author Rafael Lins
 *
 */
@ApplicationScoped
public class Metrics {
	private final MetricRegistry metrics = new MetricRegistry();
	private final Timer requests = metrics.timer("request.frequency");
	private final Histogram requestSize = metrics.histogram("request.size");
	private final Histogram responseSize = metrics.histogram("response.size");
	private final Meter exceptions = metrics.meter("exceptions");
	
	public MetricRegistry metrics() {
		return metrics;
	}
	
	public Timer requests() {
		return requests;
	}
	
	public Histogram requestSize() {
		return requestSize;
	}
	
	public Histogram responseSize() {
		return responseSize;
	}
	
	public Meter exceptions() {
		return exceptions;
	}
	
	public Meter exceptions(final Class<? extends Throwable> type) {
		return metrics.meter(MetricRegistry.name(type, "meter"));
	}
	
	public Metrics markRequest(final long duration, final TimeUnit unit) {
		requests.update(duration, unit);
		return this;
	}
	
	public Timer.Context markRequestStart() {
		return requests.time();
	}
	
	public Metrics markRequestSize(final long size) {
		requestSize.update(size);
		return this;
	}
	
	public Metrics markResponseSize(final long size) {
		responseSize.update(size);
		return this;
	}
	
	public Metrics markException(final Throwable exception) {
		exceptions.mark();
		metrics.meter(MetricRegistry.name(exception.getClass(), "meter")).mark();
		
		return this;
	}
}
