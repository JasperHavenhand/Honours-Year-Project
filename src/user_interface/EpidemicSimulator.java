package user_interface;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.flink.api.java.tuple.Tuple2;

import graph.temporal.TemporalGraphHandler;

public final class EpidemicSimulator extends JFrame {

	private static final long serialVersionUID = -8833078011625169517L;
	private TemporalGraphHandler tgh;
	private int timestep;
	private GraphPanel graphPanel;
	
	private JDialog newGraphDialog;
	private JComboBox<String> newGraphSource;
	private JComboBox<String> newGraphVirus;
	private JTextField newGraphIncrement;
	
	// Initialising the simulator.
	public EpidemicSimulator() {
		setLayout(new GridBagLayout());
		setTitle("Epidemic Simulator");
		
		createBtnsPanel();
		createGraphPanel();
		createConstraintsPanel();
		
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);
	}
	
	private void createBtnsPanel() {
		JPanel btnsPanel = new JPanel();
		btnsPanel.setLayout(new GridLayout(2,1,10,10));
		
		JButton newGraphBtn = new JButton("Create new graph");
		newGraphBtn.addActionListener(new ButtonListener("newGraphBtn"));
		btnsPanel.add(newGraphBtn);
		
		JButton nextStepBtn = new JButton("Next timestep");
		nextStepBtn.addActionListener(new ButtonListener("nextStepBtn"));
		btnsPanel.add(nextStepBtn);
		
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0,5,0,0);
		add(btnsPanel,gbc);
	}
	
	private void createGraphPanel() {
		graphPanel = new GraphPanel();
		
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 0;
		add(graphPanel,gbc);
	}
	
	private void createConstraintsPanel() {
		JPanel constraintsPanel = new JPanel();
		constraintsPanel.setLayout(new GridLayout(4,4,10,10));
		
		// Merge
		JLabel mergeLabel = new JLabel("Merge edges:");
		constraintsPanel.add(mergeLabel);
		
		JTextField mergeStart = new JTextField();
		constraintsPanel.add(mergeStart);
		
		JTextField mergeDuration = new JTextField();
		constraintsPanel.add(mergeDuration);
		
		JButton applyMerge = new JButton("Apply");
		applyMerge.addActionListener(new ButtonListener("applyMerge"));
		constraintsPanel.add(applyMerge);
		
		// Delay
		JLabel delayLabel = new JLabel("Delay edges:");
		constraintsPanel.add(delayLabel);
		
		JTextField delayTime = new JTextField();
		constraintsPanel.add(delayTime);
		
		constraintsPanel.add(new JPanel());
		
		JButton applyDelay = new JButton("Apply");
		applyDelay.addActionListener(new ButtonListener("applyDelay"));
		constraintsPanel.add(applyDelay);
		
		// Temporality Limit
		JLabel limitLabel = new JLabel("Set temporality limit:");
		constraintsPanel.add(limitLabel);
		
		JTextField limit = new JTextField();
		constraintsPanel.add(limit);
		
		constraintsPanel.add(new JPanel());
		
		JButton applyLimit = new JButton("Apply");
		applyLimit.addActionListener(new ButtonListener("applyLimit"));
		constraintsPanel.add(applyLimit);
		
		// Delete Edge
		JLabel deleteLabel = new JLabel("Delete edge between:");
		constraintsPanel.add(deleteLabel);
		
		JTextField vertex1 = new JTextField();
		constraintsPanel.add(vertex1);
		
		JTextField vertex2 = new JTextField();
		constraintsPanel.add(vertex2);
		
		JButton applyDelete = new JButton("Apply");
		applyDelete.addActionListener(new ButtonListener("applyDelete"));
		constraintsPanel.add(applyDelete);
		
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.insets = new Insets(0,0,0,5);
		add(constraintsPanel,gbc);
	}
	
	private void showNewGraphDialog() {
		if (newGraphDialog == null) {
			newGraphDialog = new JDialog(this, "Create a New Graph", true);
			newGraphDialog.setLayout(new GridLayout(4,3));
			
			newGraphDialog.add(new JLabel("Select Data Source:"));
			refreshNewGraphSource();
			newGraphDialog.add(newGraphSource);
			
			newGraphDialog.add(new JLabel("Select Virus:"));
			refreshNewGraphVirus();
			newGraphDialog.add(newGraphVirus);
			
			newGraphDialog.add(new JLabel("Set increment between timesteps (in milliseconds):"));
			newGraphIncrement = new JTextField();
			newGraphDialog.add(newGraphIncrement);
			
			
		}
		newGraphDialog.setVisible(true);
	}
	
	private void refreshNewGraphSource() {
		List<Tuple2<String, String>> sources = DataSources.getInstance().getAll();
		String[] sourceNames = new String[sources.size()];
		for (int i = 0; i < sources.size(); i++) {
			sourceNames[i] = sources.get(i).f0; 
		}
		newGraphSource = new JComboBox<String>(sourceNames);
	}
	
	private void refreshNewGraphVirus() {
		List<Tuple2<String, Double>> viruses = Tokens.getInstance().getAll();
		String[] virusNames = new String[viruses.size()];
		for (int i = 0; i < viruses.size(); i++) {
			virusNames[i] = viruses.get(i).f0;
		}
		newGraphVirus = new JComboBox<String>(virusNames);
	}

	private class ButtonListener implements ActionListener {
		private String buttonName;
		
		ButtonListener(String buttonName) {
			this.buttonName = buttonName;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (buttonName) {
				case "newGraphBtn":
					// Open new graph dialogue.
					showNewGraphDialog();
					break;
				case "nextStepBtn":
					tgh.nextTimeStep();
					timestep ++;
					graphPanel.updateTime(timestep);
					// Update graph panel.
					break;
				case "applyMerge":
//					tgh.mergeEdges(startTime, duration);
					// Update graph panel?
					break;
				case "applyDelay":
//					tgh.delayEdges(time);
					// Update graph panel?
					break;
				case "applyLimit":
//					tgh.limitTemporality(limit);
					// Update graph panel?
					break;
				case "applyDelete":
//					tgh.delayEdges(time);
					// Update graph panel?
					break;
			}
		}
		
	}
}
