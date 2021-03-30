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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.flink.api.java.tuple.Tuple2;

import data.temporal.TemporalDataFactory;
import data.temporal.TemporalDataFactory.inputType;
import graph.temporal.TemporalGraphHandler;
import utilities.Log;

public final class EpidemicSimulator extends JFrame {

	private static final long serialVersionUID = -8833078011625169517L;
	private TemporalGraphHandler tgh;
	private int timestep;
	private GraphPanel graphPanel;
	
	private JDialog newGraphDialog;
	private JComboBox<String> newGraphSource;
	private JComboBox<String> newGraphVirus;
	private JTextField newGraphIncrement;
	
	private JDialog newSourceDialog;
	private JTextField newSourceName;
	private JTextField newSourcePath;
	private JComboBox<String> newSourceInputType;
	
	private static String LOG_NAME = "general_log";
	
	// --- Initialising the Simulator ---
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
		
		JButton applyMergeBtn = new JButton("Apply");
		applyMergeBtn.addActionListener(new ButtonListener("applyMergeBtn"));
		constraintsPanel.add(applyMergeBtn);
		
		// Delay
		JLabel delayLabel = new JLabel("Delay edges:");
		constraintsPanel.add(delayLabel);
		
		JTextField delayTime = new JTextField();
		constraintsPanel.add(delayTime);
		
		constraintsPanel.add(new JPanel());
		
		JButton applyDelayBtn = new JButton("Apply");
		applyDelayBtn.addActionListener(new ButtonListener("applyDelayBtn"));
		constraintsPanel.add(applyDelayBtn);
		
		// Temporality Limit
		JLabel limitLabel = new JLabel("Set temporality limit:");
		constraintsPanel.add(limitLabel);
		
		JTextField limit = new JTextField();
		constraintsPanel.add(limit);
		
		constraintsPanel.add(new JPanel());
		
		JButton applyLimitBtn = new JButton("Apply");
		applyLimitBtn.addActionListener(new ButtonListener("applyLimitBtn"));
		constraintsPanel.add(applyLimitBtn);
		
		// Delete Edge
		JLabel deleteLabel = new JLabel("Delete edge between:");
		constraintsPanel.add(deleteLabel);
		
		JTextField vertex1 = new JTextField();
		constraintsPanel.add(vertex1);
		
		JTextField vertex2 = new JTextField();
		constraintsPanel.add(vertex2);
		
		JButton applyDeleteBtn = new JButton("Apply");
		applyDeleteBtn.addActionListener(new ButtonListener("applyDeleteBtn"));
		constraintsPanel.add(applyDeleteBtn);
		
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.insets = new Insets(0,0,0,5);
		add(constraintsPanel,gbc);
	}
	
	// --- The New Graph Dialog ---
	private void showNewGraphDialog() {
		if (newGraphDialog == null) {
			newGraphDialog = new JDialog(this, "Create a New Graph", true);
			newGraphDialog.setLayout(new GridLayout(4,3,5,5));
			
			newGraphDialog.add(new JLabel("Select Data Source:"));
			refreshNewGraphSource();
			newGraphDialog.add(newGraphSource);
			JButton newSourceBtn = new JButton("Create New");
			newSourceBtn.addActionListener(new ButtonListener("newSourceBtn"));
			newGraphDialog.add(newSourceBtn);
			
			newGraphDialog.add(new JLabel("Select Virus:"));
			refreshNewGraphVirus();
			newGraphDialog.add(newGraphVirus);
			JButton newVirusBtn = new JButton("Create New");
			newVirusBtn.addActionListener(new ButtonListener("newVirusBtn"));
			newGraphDialog.add(newVirusBtn);
			
			newGraphDialog.add(new JLabel("Set increment between timesteps (in milliseconds):"));
			newGraphIncrement = new JTextField();
			newGraphDialog.add(newGraphIncrement);
			
			newGraphDialog.add(new JPanel());
			newGraphDialog.add(new JPanel());
			newGraphDialog.add(new JPanel());
			
			JButton createGraphBtn = new JButton("Create Graph");
			createGraphBtn.addActionListener(new ButtonListener("createGraphBtn"));
			newGraphDialog.add(createGraphBtn);
			newGraphDialog.pack();
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
	
	private void loadNewGraph() {
		try {
			String dataSourceName = (String) newGraphSource.getSelectedItem();
			String dataSourcePath = DataSources.getInstance().get(dataSourceName);
			String virusName = (String) newGraphVirus.getSelectedItem();
			Double virusProb = Tokens.getInstance().get(virusName);
			Long timeIncrement = Long.parseLong(newGraphIncrement.getText());
			
			tgh = new TemporalGraphHandler(
					TemporalDataFactory.loadCSVDataSource(dataSourcePath).getTemporalGraph(),
					virusName,virusProb,timeIncrement);
			newGraphDialog.setVisible(false);
		} catch (NumberFormatException nfe) {
			// Highlight that the time increment value is invalid.
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}

	// --- The New Data Source Dialog ---
	private void showNewSourceDialog() {
		if (newSourceDialog == null) {
			newSourceDialog = new JDialog(this, "Create a New Data Source", true);
			newSourceDialog.setLayout(new GridLayout(4,3,5,5));
			
			newSourceDialog.add(new JLabel("Name:"));
			newSourceName = new JTextField();
			newSourceDialog.add(newSourceName);
			
			newSourceDialog.add(new JPanel());
			
			newSourceDialog.add(new JLabel("Input Data Location"));
			newSourcePath = new JTextField();
			newSourceDialog.add(newSourcePath);
			JButton newSourcePathBtn = new JButton("Search");
			newSourcePathBtn.addActionListener(new ButtonListener("newSourcePathBtn"));
			newSourceDialog.add(newSourcePathBtn);
			
			newSourceDialog.add(new JLabel("Input Type:"));
			inputType[] inputTypes = TemporalDataFactory.inputType.values();
			String[] inputTypeNames = new String[inputTypes.length];
			for (int i = 0; i < inputTypes.length; i++) {
				inputTypeNames[i] = inputTypes[i].toString();
			}
			newSourceInputType = new JComboBox<String>(inputTypeNames);
			
			newSourceDialog.add(new JPanel());
			newSourceDialog.add(new JPanel());
			newSourceDialog.add(new JPanel());
			
			JButton createSourceBtn = new JButton("Create Data Source");
			createSourceBtn.addActionListener(new ButtonListener("createSourceBtn"));
			newSourceDialog.add(createSourceBtn);
			
			newSourceDialog.pack();
		}
		newSourceDialog.setVisible(true);
	}
	
	private void sourcePathChooser() {
	    JFileChooser fc = new JFileChooser();
	    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    fc.setAcceptAllFileFilterUsed(false);
	    if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	newSourcePath.setText(fc.getSelectedFile().getPath());
	    }
	}
	
	private void createNewSource() {
		try {
			String inputPath = newSourcePath.getText();
			inputType inputType = data.temporal.TemporalDataFactory.inputType.valueOf((String) newSourceInputType.getSelectedItem());
			String sourceName = newSourceName.getText();
			TemporalDataFactory.createCSVDataSource(inputPath, inputType, sourceName);
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	// -- The New Virus Dialog ---
	private void showNewVirusDialog() {
		
	}
	
	private void createNewVirus() {
		
	}
	
	private class ButtonListener implements ActionListener {
		private String buttonName;
		
		ButtonListener(String buttonName) {
			this.buttonName = buttonName;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (buttonName) {
				case "nextStepBtn":
					if (tgh.nextTimeStep()) {
						timestep ++;
						graphPanel.updateTime(timestep);
					}
					break;
				case "applyMergeBtn":
//					tgh.mergeEdges(startTime, duration);
					// Update graph panel?
					break;
				case "applyDelayBtn":
//					tgh.delayEdges(time);
					// Update graph panel?
					break;
				case "applyLimitBtn":
//					tgh.limitTemporality(limit);
					// Update graph panel?
					break;
				case "applyDeleteBtn":
//					tgh.delayEdges(time);
					// Update graph panel?
					break;
				case "newGraphBtn":
					showNewGraphDialog();
					break;
				case "createGraphBtn":
					loadNewGraph();
					break;
				case "newSourceBtn":
					showNewSourceDialog();
					break;
				case "newSourcePathBtn":
					sourcePathChooser();
					break;
				case "createSourceBtn":
					createNewSource();
					break;
				case "newVirusBtn":
					showNewVirusDialog();
					break;
			}
		}
		
	}
}