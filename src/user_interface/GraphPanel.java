package user_interface;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

final class GraphPanel extends JPanel {
	private static final long serialVersionUID = -7655972769282670993L;
	
	private JLabel timestepLabel, virusNameLabel, virusProbLabel;
	private JTable verticesTable;
	private JScrollPane verticesSPane;
	
	GraphPanel() {
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		
		timestepLabel = new JLabel("Timestep ");
		gbc.gridy = 0;
		add(timestepLabel,gbc);
		
		virusNameLabel = new JLabel("Virus Name: ");
		gbc.gridy = 1;
		add(virusNameLabel,gbc);
		virusProbLabel = new JLabel("Probability of Transmission: ");
		gbc.gridy = 2;
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
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.BOTH;
		add(verticesSPane,gbc);
	}
	
	void updateTime(int timestep) {
		timestepLabel.setText("Timestep " + timestep);
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
