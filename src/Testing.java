import org.gradoop.flink.io.api.DataSource;

import temporalData.TemporalDataFactory;
import temporalData.TemporalDataFactory.inputType;

public class Testing {
	
	public static void main (String[] args) {
		DataSource data = TemporalDataFactory.createCSVDataSource(
				"C:\\Users\\Student\\Documents\\Fourth Year\\COMP390 - Honours Year Project\\NSense_Traces_Set2_CRAWDAD",
				inputType.NSENSE,"NSense_test");
	}
}
