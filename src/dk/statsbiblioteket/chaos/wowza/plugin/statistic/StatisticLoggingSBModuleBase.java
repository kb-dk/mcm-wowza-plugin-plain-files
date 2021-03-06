package dk.statsbiblioteket.chaos.wowza.plugin.statistic;

import java.io.IOException;
import java.text.SimpleDateFormat;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.module.IModuleOnApp;
import com.wowza.wms.module.IModuleOnStream;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify;

import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.MCMPortalInterfaceStatisticsImpl;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.StreamingEventLogger;
import dk.statsbiblioteket.chaos.wowza.plugin.util.PropertiesUtil;
import dk.statsbiblioteket.chaos.wowza.plugin.util.StringAndTextUtil;

public class StatisticLoggingSBModuleBase extends ModuleBase implements IModuleOnApp, IModuleOnStream {

	private static String pluginName = "CHAOS Wowza plugin - Statistics SB";
	private static String pluginVersion = "1.2.0 Optional statistics"; 

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
	
	public StatisticLoggingSBModuleBase() {
		super();
	}

	@Override
	public void onAppStart(IApplicationInstance appInstance) {
		getLogger().info("onAppStart: " + pluginName + " version " + pluginVersion);
		getLogger().info("onAppStart: VHost home path: " + appInstance.getVHost().getHomePath());
		appInstance.shutdown(true, true);
		PropertiesUtil.loadPropertiesMap(getLogger(), appInstance.getVHost().getHomePath());
		if (StreamingEventLogger.getInstance()==null) {
			try {
				StreamingEventLogger.createInstance(getLogger(), appInstance.getVHost().getHomePath());
			} catch (IOException e) {
				throw new RuntimeException("Could not initialize StreamingEventLogger.", e); 
			}
		}
		if (MCMPortalInterfaceStatisticsImpl.getInstance()==null) {
			try {
				MCMPortalInterfaceStatisticsImpl.createInstance(getLogger(), appInstance.getVHost().getHomePath());
			} catch (IOException e) {
				throw new RuntimeException("Could not initialize MCMPortalInterfaceStatistics.", e); 
			}
		}
	}

	@Override
	public void onAppStop(IApplicationInstance appInstance) {
		getLogger().info("onAppStop: " + pluginName + " version " + pluginVersion);
	}

	@Override
	public void onStreamCreate(IMediaStream stream) {
		getLogger().info("onStreamCreate by: " + stream.getClientId());
		String queryString = String.valueOf(stream.getClient().getQueryStr());
		String statisticsParameter = StringAndTextUtil.extractValueFromQueryStringAndKey("statistics", queryString);
		if ((statisticsParameter == null) || (!statisticsParameter.equalsIgnoreCase("off"))) {
			IMediaStreamActionNotify streamActionNotify = 
				new StatisticLoggingStreamListener(getLogger(), stream, StreamingEventLogger.getInstance());
			WMSProperties props = stream.getProperties();
			synchronized(props) {
				props.put("streamActionNotifierForStatistics", streamActionNotify);
			}
			stream.addClientListener(streamActionNotify);
		}
	}

	@Override
	public void onStreamDestroy(IMediaStream stream) {
		getLogger().info("onStreamDestroy by: " + stream.getClientId());
		IMediaStreamActionNotify actionNotify = null;
		WMSProperties props = stream.getProperties();
		synchronized(props) {
			actionNotify = (IMediaStreamActionNotify)stream.getProperties().get("streamActionNotifierForStatistics");
		}
		if (actionNotify != null) {
			stream.removeClientListener(actionNotify);
			getLogger().info("removeClientListener: "+stream.getSrc());
		}
	}
}
