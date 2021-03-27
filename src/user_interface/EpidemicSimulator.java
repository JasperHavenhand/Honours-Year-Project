package user_interface;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
		btnsPanel.setLayout(new GridBagLayout());
		
		JButton newGraphBtn = new JButton("Create new graph");
		btnsPanel.add(newGraphBtn);
		
		JButton nextStepBtn = new JButton("Next timestep");
		btnsPanel.add(nextStepBtn);
		
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(btnsPanel,gbc);
	}
	
	private void createGraphPanel() {
		graphPanel = new GraphPanel();
		
		
		
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		add(graphPanel,gbc);
	}
	
	private void createConstraintsPanel() {
		JPanel constraintsPanel = new JPanel();
		constraintsPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints labelGbc = new GridBagConstraints();
		
		JLabel mergeLabel = new JLabel("Merge edges:");
		labelGbc.gridx = 0;
		labelGbc.gridy = 1;
		constraintsPanel.add(mergeLabel, labelGbc);
		
		JLabel delayLabel = new JLabel("Delay edges:");
		labelGbc.gridx = 0;
		labelGbc.gridy = 2;
		constraintsPanel.add(delayLabel, labelGbc);
		
		JLabel limitLabel = new JLabel("Set temporality limit:");
		labelGbc.gridx = 0;
		labelGbc.gridy = 3;
		constraintsPanel.add(limitLabel, labelGbc);
		
		JLabel deleteLabel = new JLabel("Delete edge between:");
		labelGbc.gridx = 0;
		labelGbc.gridy = 4;
		constraintsPanel.add(deleteLabel, labelGbc);
		
		GridBagConstraints applyGbc = new GridBagConstraints();
		
		JButton applyMerge = new JButton("Apply");
		applyGbc.gridx = 3;
		applyGbc.gridy = 1;
		constraintsPanel.add(applyMerge,applyGbc);
		
		JButton applyDelay = new JButton("Apply");
		applyGbc.gridx = 3;
		applyGbc.gridy = 2;
		constraintsPanel.add(applyDelay,applyGbc);
		
		JButton applyLimit = new JButton("Apply");
		applyGbc.gridx = 3;
		applyGbc.gridy = 3;
		constraintsPanel.add(applyLimit,applyGbc);
		
		JButton applyDelete = new JButton("Apply");
		applyGbc.gridx = 3;
		applyGbc.gridy = 4;
		constraintsPanel.add(applyDelete,applyGbc);
		
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridx = 2;
		gbc.gridy = 0;
		add(constraintsPanel,gbc);
	}
}
