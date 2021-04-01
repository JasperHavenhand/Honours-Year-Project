package user_interface;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.flink.api.java.tuple.Tuple2;
import org.gradoop.temporal.model.impl.pojo.TemporalVertex;

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
	private CardLayout newGraphCards;
	private JComboBox<String> newGraphSource;
	private JComboBox<String> newGraphVirus;
	private JTextField newGraphIncrement;
	private JList<String> newGraphInfected;
	
	private JDialog newSourceDialog;
	private JTextField newSourceName;
	private JTextField newSourcePath;
	private JComboBox<String> newSourceInputType;
	
	private JDialog newVirusDialog;
	private JTextField newVirusName;
	private JTextField newVirusProb;
	
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
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0,5,0,0);
		add(btnsPanel,gbc);
	}
	
	private void createGraphPanel() {
		graphPanel = new GraphPanel();
		
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.HORIZONTAL;
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
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.insets = new Insets(0,0,0,5);
		add(constraintsPanel,gbc);
	}
	
	// --- The New Graph Dialog ---
	private void showNewGraphDialog() {
		if (newGraphDialog == null) {
			newGraphDialog = new JDialog(this, "Create a New Graph", true);
			newGraphCards = new CardLayout();
			newGraphDialog.getContentPane().setLayout(newGraphCards);
			
			JPanel panel1 = new JPanel();
			panel1.setLayout(new GridLayout(4,3,5,5));
			
			panel1.add(new JLabel("Select Data Source:"));
			refreshNewGraphSource();
			panel1.add(newGraphSource);
			JButton newSourceBtn = new JButton("Create New");
			newSourceBtn.addActionListener(new ButtonListener("newSourceBtn"));
			panel1.add(newSourceBtn);
			
			panel1.add(new JLabel("Select Virus:"));
			refreshNewGraphVirus();
			panel1.add(newGraphVirus);
			JButton newVirusBtn = new JButton("Create New");
			newVirusBtn.addActionListener(new ButtonListener("newVirusBtn"));
			panel1.add(newVirusBtn);
			
			panel1.add(new JLabel("Set increment between timesteps (in milliseconds):"));
			newGraphIncrement = new JTextField();
			panel1.add(newGraphIncrement);
			
			panel1.add(new JPanel());
			panel1.add(new JPanel());
			panel1.add(new JPanel());
			
			JButton createGraphBtn = new JButton("Create Graph");
			createGraphBtn.addActionListener(new ButtonListener("createGraphBtn"));
			panel1.add(createGraphBtn);
			
			newGraphDialog.add(panel1);
			
			JPanel panel2 = new JPanel();
			panel2.setLayout(new GridLayout(2,2,5,5));
			
			panel2.add(new JLabel("Selected initially infected vertices:"));
			newGraphInfected = new JList<String>();
			newGraphInfected.setModel(new DefaultListModel<String>());
			panel2.add(newGraphInfected);
			
			panel2.add(new JPanel());
			
			JButton infectVerticesBtn = new JButton("Confirm");
			infectVerticesBtn.addActionListener(new ButtonListener("infectVerticesBtn"));
			panel2.add(infectVerticesBtn);
			
			newGraphDialog.add(panel2);
			
			newGraphDialog.pack();
		}
		newGraphCards.first(newGraphDialog.getContentPane());
		newGraphDialog.setVisible(true);
	}
	
	private void refreshNewGraphSource() {
		if (newGraphSource == null) {
			newGraphSource = new JComboBox<String>();
		}
		List<Tuple2<String, String>> sources = DataSources.getInstance().getAll();
		List<String> sourceNames = new ArrayList<String>();
		DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) newGraphSource.getModel();
		model.removeAllElements();
		for (Tuple2<String, String> s: sources) {
			sourceNames.add(s.f0); 
		}
		model.addAll(sourceNames);
		newGraphSource.setModel(model);
	}
	
	private void refreshNewGraphVirus() {
		if (newGraphVirus == null) {
			newGraphVirus = new JComboBox<String>();
		}
		List<Tuple2<String, Double>> viruses = Tokens.getInstance().getAll();
		DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) newGraphVirus.getModel();
		model.removeAllElements();
		List<String> virusNames = new ArrayList<String>();
		for (int i = 0; i < viruses.size(); i++) {
			virusNames.add(viruses.get(i).f0);
		}
		model.addAll(virusNames);
		newGraphVirus.setModel(model);
	}
	
	private void createNewGraph() {
		try {
			String dataSourceName = (String) newGraphSource.getSelectedItem();
			String dataSourcePath = DataSources.getInstance().get(dataSourceName);
			String virusName = (String) newGraphVirus.getSelectedItem();
			Double virusProb = Tokens.getInstance().get(virusName);
			Long timeIncrement = Long.parseLong(newGraphIncrement.getText());
			
			tgh = new TemporalGraphHandler(
					TemporalDataFactory.loadCSVDataSource(dataSourcePath).getTemporalGraph(),
					virusProb,timeIncrement);
			
			timestep = 0;
			
			// Loading the vertices so that the user can select which will be initially infected.
			List<TemporalVertex> vertices = tgh.getCompleteGraph().getVertices().collect();
			List<String> vertexIDs = new ArrayList<String>();
			DefaultListModel<String> model = (DefaultListModel<String>) newGraphInfected.getModel();
			model.removeAllElements();
			for (TemporalVertex v: vertices) {
				vertexIDs.add(v.getId().toString()); 
			}
			model.addAll(vertexIDs);
			newGraphInfected.setModel(model);
			
			newGraphCards.next(newGraphDialog.getContentPane());
			
		} catch (NumberFormatException nfe) {
			// Highlight that the time increment value is invalid.
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	private void displayNewGraph() {
		tgh.giveTokenTo(newGraphInfected.getSelectedValuesList());
		graphPanel.updateVirus((String) newGraphVirus.getSelectedItem());
		graphPanel.updateTime(timestep);
		updateVerticesTable();
		newGraphDialog.setVisible(false);
	}
	
	private void updateVerticesTable() {
		try {
			List<TemporalVertex> vertices = tgh.getCompleteGraph().getVertices().collect();
			Collections.sort(vertices,Comparator.comparing((TemporalVertex vertex) -> vertex.getId()));
			String[][] filteredVertices = new String[vertices.size()][];
			for (int i = 0; i < vertices.size(); i++) {
				String[] vertex = {vertices.get(i).getPropertyValue("name").getString(),
				vertices.get(i).getPropertyValue("infected").toString()};
				filteredVertices[i] = vertex;
			}
			graphPanel.updateVertices(filteredVertices);
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
			newSourceDialog.add(newSourceInputType);
			
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
			if (TemporalDataFactory.createCSVDataSource(inputPath, inputType, sourceName) == null) {
				//highlight that inputPath is invalid.
			} else {
				newSourceDialog.setVisible(false);
				newSourceName.setText("");
				newSourcePath.setText("");
				refreshNewGraphSource();
				newGraphSource.setSelectedItem(sourceName);
			}
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	// --- The New Virus Dialog ---
	private void showNewVirusDialog() {
		if (newVirusDialog == null) {
			newVirusDialog = new JDialog(this, "Create a New Virus", true);
			newVirusDialog.setLayout(new GridLayout(3,2,5,5));
			
			newVirusDialog.add(new JLabel("Name:"));
			newVirusName = new JTextField();
			newVirusDialog.add(newVirusName);
			
			newVirusDialog.add(new JLabel("Transfer Probability:"));
			newVirusProb = new JTextField();
			newVirusDialog.add(newVirusProb);
			
			newVirusDialog.add(new JPanel());
			
			JButton createVirusBtn = new JButton("Create Virus");
			createVirusBtn.addActionListener(new ButtonListener("createVirusBtn"));
			newVirusDialog.add(createVirusBtn);
			
			newVirusDialog.pack();
		}
		newVirusDialog.setVisible(true);
	}
	
	private void createNewVirus() {
		try {
			Double prob = Double.valueOf(newVirusProb.getText());
			if (prob < 0.0 || prob > 1.0) {
				//must be in the range 0.0 to 1.0
			}
			String name = newVirusName.getText();
			Tokens.getInstance().set(name, prob);
			newVirusDialog.setVisible(false);
			newVirusName.setText("");
			newVirusProb.setText("");
			refreshNewGraphVirus();
			newGraphVirus.setSelectedItem(name);
		} catch (NumberFormatException nfe) {
			// highlight that the probability must be a number
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
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
						updateVerticesTable();
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
					createNewGraph();
					break;
				case "infectVerticesBtn":
					displayNewGraph();
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
				case "createVirusBtn":
					createNewVirus();
					break;
			}
		}
		
	}
}
