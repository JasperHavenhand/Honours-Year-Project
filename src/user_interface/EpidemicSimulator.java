package user_interface;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import graph.temporal.TemporalGraphHandler;

public final class EpidemicSimulator extends JFrame {

	private static final long serialVersionUID = -8833078011625169517L;
	private TemporalGraphHandler tgh;
	private int timeStep;
	private GraphPanel graphPanel;

	
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
		btnsPanel.add(newGraphBtn);
		
		JButton nextStepBtn = new JButton("Next timestep");
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
		constraintsPanel.add(applyMerge);
		
		// Delay
		JLabel delayLabel = new JLabel("Delay edges:");
		constraintsPanel.add(delayLabel);
		
		JTextField delayTime = new JTextField();
		constraintsPanel.add(delayTime);
		
		constraintsPanel.add(new JPanel());
		
		JButton applyDelay = new JButton("Apply");
		constraintsPanel.add(applyDelay);
		
		// Temporality Limit
		JLabel limitLabel = new JLabel("Set temporality limit:");
		constraintsPanel.add(limitLabel);
		
		JTextField limit = new JTextField();
		constraintsPanel.add(limit);
		
		constraintsPanel.add(new JPanel());
		
		JButton applyLimit = new JButton("Apply");
		constraintsPanel.add(applyLimit);
		
		// Delete Edge
		JLabel deleteLabel = new JLabel("Delete edge between:");
		constraintsPanel.add(deleteLabel);
		
		JTextField vertex1 = new JTextField();
		constraintsPanel.add(vertex1);
		
		JTextField vertex2 = new JTextField();
		constraintsPanel.add(vertex2);
		
		JButton applyDelete = new JButton("Apply");
		constraintsPanel.add(applyDelete);
		
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.insets = new Insets(0,0,0,5);
		add(constraintsPanel,gbc);
	}
}
