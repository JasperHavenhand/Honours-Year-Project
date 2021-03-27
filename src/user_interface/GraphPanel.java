package user_interface;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

final class GraphPanel extends JPanel {
	private static final long serialVersionUID = -7655972769282670993L;
	
	private JLabel timestepLabel;
	private JLabel virusLabel;
	private JTable verticesTable;
	
	GraphPanel() {
		setLayout(new GridLayout(3,1,10,10));
		timestepLabel = new JLabel("Timestep ");
		add(timestepLabel);
		
		virusLabel = new JLabel("Virus: ");
		add(virusLabel);
		
		String data[][] = {};
		String column[] = {"vertex","Infected?"};
		verticesTable = new JTable(data,column);
		JScrollPane scrollPane = new JScrollPane(verticesTable);
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		add(scrollPane);
	}
	
	void updateTime(int timestep) {
		timestepLabel.setText("Timestep " + timestep);
	}
	
	void updateVirus(String virusName) {
		virusLabel.setText(virusName);
	}
	
	void updateVertices(String[][] vertices) {
		DefaultTableModel model = (DefaultTableModel) verticesTable.getModel();
		model.setRowCount(0);
		for (String[] vertex: vertices) {
			model.addRow(vertex);
		}
	}
	
}
