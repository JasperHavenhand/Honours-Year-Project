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
import java.util.Set;

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
import javax.swing.JScrollPane;
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
	private TemporalGraphHandler tghNew;
	private int timestep;
	private GraphPanel graphPanel;
	
	private JTextField mergeStart;
	private JTextField mergeDuration;
	private JTextField delayTime;
	private JTextField tempLimit;
	private JComboBox<String> deleteVertex1;
	private JComboBox<String> deleteVertex2;
	
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
		
		mergeStart = new JTextField();
		constraintsPanel.add(mergeStart);
		
		mergeDuration = new JTextField();
		constraintsPanel.add(mergeDuration);
		
		JButton applyMergeBtn = new JButton("Apply");
		applyMergeBtn.addActionListener(new ButtonListener("applyMergeBtn"));
		constraintsPanel.add(applyMergeBtn);
		
		// Delay
		JLabel delayLabel = new JLabel("Delay edges:");
		constraintsPanel.add(delayLabel);
		
		delayTime = new JTextField();
		constraintsPanel.add(delayTime);
		
		constraintsPanel.add(new JPanel());
		
		JButton applyDelayBtn = new JButton("Apply");
		applyDelayBtn.addActionListener(new ButtonListener("applyDelayBtn"));
		constraintsPanel.add(applyDelayBtn);
		
		// Temporality Limit
		JLabel limitLabel = new JLabel("Set temporality limit:");
		constraintsPanel.add(limitLabel);
		
		tempLimit = new JTextField();
		constraintsPanel.add(tempLimit);
		
		constraintsPanel.add(new JPanel());
		
		JButton applyTempLimitBtn = new JButton("Apply");
		applyTempLimitBtn.addActionListener(new ButtonListener("applyTempLimitBtn"));
		constraintsPanel.add(applyTempLimitBtn);
		
		// Delete Edge
		JLabel deleteLabel = new JLabel("Delete edge between:");
		constraintsPanel.add(deleteLabel);
		
		deleteVertex1 = new JComboBox<String>();
		deleteVertex1.setModel(new DefaultComboBoxModel<String>());
		constraintsPanel.add(deleteVertex1);
		
		deleteVertex2 = new JComboBox<String>();
		deleteVertex1.setModel(new DefaultComboBoxModel<String>());
		constraintsPanel.add(deleteVertex2);
		
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
			panel1.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(5,5,5,5);
			
			gbc.gridy = 0;
			gbc.gridx = 0;
			panel1.add(new JLabel("Select Data Source:"),gbc);
			refreshNewGraphSource();
			gbc.gridx = 1;
			panel1.add(newGraphSource,gbc);
			JButton newSourceBtn = new JButton("Create New");
			newSourceBtn.addActionListener(new ButtonListener("newSourceBtn"));
			gbc.gridx = 2;
			panel1.add(newSourceBtn,gbc);
			
			gbc.gridy = 1;
			gbc.gridx = 0;
			panel1.add(new JLabel("Select Virus:"),gbc);
			refreshNewGraphVirus();
			gbc.gridx = 1;
			panel1.add(newGraphVirus,gbc);
			JButton newVirusBtn = new JButton("Create New");
			newVirusBtn.addActionListener(new ButtonListener("newVirusBtn"));
			gbc.gridx = 2;
			panel1.add(newVirusBtn,gbc);
			
			gbc.gridy = 2;
			gbc.gridx = 0;
			panel1.add(new JLabel("Set increment between timesteps (in milliseconds):"),gbc);
			newGraphIncrement = new JTextField();
			gbc.gridx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			panel1.add(newGraphIncrement,gbc);
			gbc.fill = GridBagConstraints.NONE;
			
			JButton createGraphBtn = new JButton("Create Graph");
			createGraphBtn.addActionListener(new ButtonListener("createGraphBtn"));
			gbc.gridy = 3;
			gbc.gridx = 2;
			panel1.add(createGraphBtn,gbc);
			
			newGraphDialog.add(panel1);
			
			JPanel panel2 = new JPanel();
			panel2.setLayout(new GridBagLayout());
			
			gbc.gridy = 0;
			gbc.gridx = 0;
			panel2.add(new JLabel("Selected initially infected vertices:"),gbc);
			newGraphInfected = new JList<String>();
			newGraphInfected.setModel(new DefaultListModel<String>());
			gbc.gridx = 1;
			gbc.fill = GridBagConstraints.BOTH;
			panel2.add(new JScrollPane(newGraphInfected),gbc);
			gbc.fill = GridBagConstraints.NONE;
			
			JButton infectVerticesBtn = new JButton("Confirm");
			infectVerticesBtn.addActionListener(new ButtonListener("infectVerticesBtn"));
			gbc.gridy = 1;
			gbc.gridx = 1;
			panel2.add(infectVerticesBtn,gbc);
			
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
	
	/** Creates the new TemporalGraphHandler and fetches the vertices for the user
	 * to select the initially infected ones. */
	private void createNewGraph() {
		try {
			String dataSourceName = (String) newGraphSource.getSelectedItem();
			String dataSourcePath = DataSources.getInstance().get(dataSourceName);
			String virusName = (String) newGraphVirus.getSelectedItem();
			Double virusProb = Tokens.getInstance().get(virusName);
			Long timeIncrement = Long.parseLong(newGraphIncrement.getText());
			
			/* Temporarily holding the new TemporalGraphHandler in case the 
			 * user decides to cancel creating the new graph. */
			tghNew = new TemporalGraphHandler(
					TemporalDataFactory.loadCSVDataSource(dataSourcePath).getTemporalGraph(),
					virusProb,timeIncrement);
			
			// Loading the vertices so that the user can select which will be initially infected.
			Set<String> vertices = tghNew.getVertices().keySet();
			DefaultListModel<String> model = new DefaultListModel<String>();
			model.addAll(vertices);
			newGraphInfected.setModel(model);
			
			newGraphCards.next(newGraphDialog.getContentPane());
			newGraphDialog.validate();
			
		} catch (NumberFormatException nfe) {
			// Highlight that the time increment value is invalid.
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	/** Let's the use choose the initially infected vertices and then updates the 
	 * graph constraints panels if the user commits to this new graph. */
	private void displayNewGraph() {
		tgh = tghNew;
		tghNew = null;
		
		List<String> ids = new ArrayList<String>();
		for (String name: newGraphInfected.getSelectedValuesList()) {
			ids.add(tgh.getVertices().get(name));
		}
		tgh.giveTokenTo(ids);
		graphPanel.updateVirus((String) newGraphVirus.getSelectedItem());
		timestep = 0;
		graphPanel.updateTime(timestep);
		updateVerticesTable();
		
		Set<String> vertices = tgh.getVertices().keySet();
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
		model.addAll(vertices);
		deleteVertex1.setModel(model);
		deleteVertex2.setModel(model);
		
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
	
	// --- Constraint Methods ---
	private void applyMerge() {
		try {
		long startTime = Long.parseLong(mergeStart.getText());
		long duration = Long.parseLong(mergeDuration.getText());
		tgh.mergeEdges(startTime, duration);
		}  catch (NumberFormatException nfe) {
			// highlight that the times must be in milliseconds.
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	private void applyDelay() {
		try {
			long time = Long.parseLong(delayTime.getText());
			tgh.delayEdges(time);
		}  catch (NumberFormatException nfe) {
			// highlight that the time must be in milliseconds.
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	private void applyTempLimit() {
		try {
			int limit = Integer.parseInt(tempLimit.getText());
			tgh.limitTemporality(limit);
		}  catch (NumberFormatException nfe) {
			// highlight that the limit must be an integer.
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	private void applyDelete() {
		try {
			if (deleteVertex1.getSelectedItem() != null && deleteVertex2.getSelectedItem() != null) {
				String vertex1 = tgh.getVertices().get((String) deleteVertex1.getSelectedItem());
				String vertex2 = tgh.getVertices().get((String) deleteVertex2.getSelectedItem());
				tgh.deleteEdgeBetween(vertex1, vertex2);
			}
		}  catch (IllegalArgumentException iae) {
			// highlight that the limit must be a hexadecimal.
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
					applyMerge();
					break;
				case "applyDelayBtn":
					applyDelay();
					break;
				case "applyTempLimitBtn":
					applyTempLimit();
					break;
				case "applyDeleteBtn":
					applyDelete();
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
