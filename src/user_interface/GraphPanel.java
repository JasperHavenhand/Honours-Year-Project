package user_interface;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

final class GraphPanel extends JPanel {
	private static final long serialVersionUID = -7655972769282670993L;
	
	private JLabel timestepLabel, currTimestampLabel, fnlTimestampLabel, virusNameLabel, virusProbLabel;
	private JTable verticesTable;
	private JScrollPane verticesSPane;
	private static DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
	
	GraphPanel() {
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		
		timestepLabel = new JLabel("Timestep ");
		gbc.gridy = 0;
		add(timestepLabel,gbc);
		
		currTimestampLabel = new JLabel("Current Timestamp: ");
		gbc.gridy = 1;
		add(currTimestampLabel,gbc);
		
		fnlTimestampLabel = new JLabel("Final Timestamp: ");
		gbc.gridy = 2;
		add(fnlTimestampLabel,gbc);
		
		virusNameLabel = new JLabel("Virus Name: ");
		gbc.gridy = 3;
		add(virusNameLabel,gbc);
		virusProbLabel = new JLabel("Probability of Transmission: ");
		gbc.gridy = 4;
		add(virusProbLabel,gbc);
		
		String data[][] = {};
		String column[] = {"vertex","Infected"};
		verticesTable = new JTable(data,column);
		verticesTable.getTableHeader().setReorderingAllowed(false);
		verticesTable.setModel(new DefaultTableModel(data,column){
			private static final long serialVersionUID = 9110253115726752997L;
			@Override
		    public boolean isCellEditable(int row, int column) {
		       return false;
		    }
		});
		verticesSPane = new JScrollPane(verticesTable);
		verticesSPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		gbc.gridy = 5;
		gbc.fill = GridBagConstraints.BOTH;
		add(verticesSPane,gbc);
	}
	
	void updateTimestep(int timestep) {
		timestepLabel.setText("Timestep " + timestep);
	}
	
	void updateCurrentTimestamp(Long timestamp) {

		currTimestampLabel.setText("Current Timestamp: " + epochMilliToDate(timestamp));
	}
	
	void updateFinalTimestamp(Long timestamp) {
		fnlTimestampLabel.setText("Final Timestamp: " + epochMilliToDate(timestamp));
	}
	
	private String epochMilliToDate(Long timestamp) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		return df.format(calendar.getTime());
	}
	
	void updateVirus(String virusName) {
		virusNameLabel.setText("Virus Name: " + virusName);
		virusProbLabel.setText("Probability of Transmission: " + Tokens.getInstance().get(virusName));
	}
	
	void updateVertices(String[][] vertices) {
		DefaultTableModel model = (DefaultTableModel) verticesTable.getModel();
		model.setRowCount(0);
		for (String[] vertex: vertices) {
			model.addRow(vertex);
		}
	}
	
}
