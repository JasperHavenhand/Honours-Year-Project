package user_interface;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DateEditor;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.tuple.Triple;
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
	
	private JSpinner mergeStart;
	private JFormattedTextField mergeDuration;
	private JFormattedTextField delayTime;
	private JFormattedTextField tempLimit;
	private JComboBox<String> deleteVertex1;
	private JComboBox<String> deleteVertex2;
	
	private JDialog newGraphDialog;
	private CardLayout newGraphCards;
	private JComboBox<String> newGraphSource;
	private JComboBox<String> newGraphVirus;
	private JFormattedTextField newGraphIncrement;
	private JList<String> newGraphInfected;
	
	private JDialog newSourceDialog;
	private JTextField newSourceName;
	private JTextField newSourcePath;
	private JComboBox<String> newSourceInputType;
	
	private JDialog newVirusDialog;
	private JTextField newVirusName;
	private JTextField newVirusProb;
	
	private static String LOG_NAME = "general_log";
	
	public static void main(String[] args) {
		try {
		    SwingUtilities.invokeLater(new Runnable() {
		        @Override
		        public void run() {
		        	new EpidemicSimulator();
		        }
		    });
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	// --- Initialising the Simulator ---
	public EpidemicSimulator() {
		setLayout(new GridBagLayout());
		setTitle("Epidemic Simulator");
		
		createBtnsPanel();
		createGraphPanel();
		createConstraintsPanel();
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
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
		constraintsPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5,5,5,5);
		
		// Merge
		gbc.gridy = 0;
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.SOUTH;
		constraintsPanel.add(new JLabel("start time"),gbc);
		
		gbc.gridx = 2;
		constraintsPanel.add(new JLabel("duration (in milliseconds)"),gbc);
		gbc.anchor = GridBagConstraints.CENTER;
		
		gbc.gridy = 1;
		gbc.gridx = 0;
		constraintsPanel.add(new JLabel("Merge edges:"),gbc);
		
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mergeStart = new JSpinner(new SpinnerDateModel());
		mergeStart.setEditor(new DateEditor(mergeStart, "dd/MM/yyyy HH:mm:ss.SSS"));
		constraintsPanel.add(mergeStart,gbc);
		
		gbc.gridx = 2;
		mergeDuration = new JFormattedTextField(NumberFormat.getNumberInstance());
		constraintsPanel.add(mergeDuration,gbc);
		gbc.fill = GridBagConstraints.NONE;
		
		gbc.gridx = 3;
		JButton applyMergeBtn = new JButton("Apply");
		applyMergeBtn.addActionListener(new ButtonListener("applyMergeBtn"));
		constraintsPanel.add(applyMergeBtn,gbc);
		
		// Delay
		gbc.gridy = 2;
		gbc.gridx = 0;
		JLabel delayLabel = new JLabel("Delay edges by:");
		constraintsPanel.add(delayLabel,gbc);
		
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		delayTime = new JFormattedTextField(NumberFormat.getNumberInstance());
		constraintsPanel.add(delayTime,gbc);
		gbc.fill = GridBagConstraints.NONE;
		
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.WEST;
		constraintsPanel.add(new JLabel("milliseconds"),gbc);
		gbc.anchor = GridBagConstraints.CENTER;
		
		gbc.gridx = 3;
		JButton applyDelayBtn = new JButton("Apply");
		applyDelayBtn.addActionListener(new ButtonListener("applyDelayBtn"));
		constraintsPanel.add(applyDelayBtn,gbc);
		
		// Temporality Limit
		gbc.gridy = 3;
		gbc.gridx = 0;
		JLabel limitLabel = new JLabel("Set temporality limit to:");
		constraintsPanel.add(limitLabel,gbc);
		
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		tempLimit = new JFormattedTextField(NumberFormat.getNumberInstance());
		constraintsPanel.add(tempLimit,gbc);
		gbc.fill = GridBagConstraints.NONE;
		
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.WEST;
		constraintsPanel.add(new JLabel("labels per edge"),gbc);
		gbc.anchor = GridBagConstraints.CENTER;
		
		gbc.gridx = 3;
		JButton applyTempLimitBtn = new JButton("Apply");
		applyTempLimitBtn.addActionListener(new ButtonListener("applyTempLimitBtn"));
		constraintsPanel.add(applyTempLimitBtn,gbc);
		
		// Delete Edge
		gbc.gridy = 4;
		gbc.gridx = 0;
		JLabel deleteLabel = new JLabel("Delete edge between:");
		constraintsPanel.add(deleteLabel,gbc);
		
		gbc.gridx = 1;
		deleteVertex1 = new JComboBox<String>();
		deleteVertex1.setModel(new DefaultComboBoxModel<String>());
		constraintsPanel.add(deleteVertex1,gbc);
		
		gbc.gridx = 2;
		deleteVertex2 = new JComboBox<String>();
		deleteVertex1.setModel(new DefaultComboBoxModel<String>());
		constraintsPanel.add(deleteVertex2,gbc);
		
		gbc.gridx = 3;
		JButton applyDeleteBtn = new JButton("Apply");
		applyDeleteBtn.addActionListener(new ButtonListener("applyDeleteBtn"));
		constraintsPanel.add(applyDeleteBtn,gbc);
		
		// Calculate Temporalities
		gbc.gridy = 5;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(30,5,5,5);
		JButton calcTempsBtn = new JButton("Calculate Temporalities");
		calcTempsBtn.addActionListener(new ButtonListener("calcTempsBtn"));
		constraintsPanel.add(calcTempsBtn,gbc);
		
		// Calculate Reachability Sets
		gbc.gridx = 2;
		JButton calcReachBtn = new JButton("Calculate Reachability Sets");
		calcReachBtn.addActionListener(new ButtonListener("calcReachBtn"));
		constraintsPanel.add(calcReachBtn,gbc);
		gbc.gridwidth = 2;
		
		// Adding the constraints panel to the JFrame.
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
			
			// First Panel
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
			newGraphIncrement = new JFormattedTextField(NumberFormat.getNumberInstance());
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
			
			// Second Panel
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
			Boolean error = false;
			List<String> errorMsgs = new ArrayList<String>(3);
			if (newGraphSource.getSelectedItem() == null) {
				error = true;
				errorMsgs.add("A data source is required.");
			}
			if (newGraphVirus.getSelectedItem() == null) {
				error = true;
				errorMsgs.add("A virus is required.");
			}
			if (newGraphIncrement.getValue() == null) {
				error = true;
				errorMsgs.add("A time increment is required.");
			} else if ((Long) newGraphIncrement.getValue() <= 0) {
				error = true;
				errorMsgs.add("The time increment must be positive and non-zero.");
			}
			
			if (error) {
				showErrorDialog(errorMsgs);
			} else {
				String dataSourceName = (String) newGraphSource.getSelectedItem();
				String dataSourcePath = DataSources.getInstance().get(dataSourceName);
				String virusName = (String) newGraphVirus.getSelectedItem();
				Double virusProb = Tokens.getInstance().get(virusName);
				Long timeIncrement = (Long) newGraphIncrement.getValue();
				
				/* Temporarily holding the new TemporalGraphHandler in case the 
				 * user decides to cancel creating the new graph. */
				tghNew = new TemporalGraphHandler(
						TemporalDataFactory.loadCSVDataSource(dataSourcePath).getTemporalGraph(),
						virusProb,timeIncrement);
				
				// Loading the vertices so that the user can select which will be initially infected.
				Set<String> vertices = tghNew.getVertexNamesToIDs().keySet();
				DefaultListModel<String> model = new DefaultListModel<String>();
				model.addAll(vertices);
				newGraphInfected.setModel(model);
				
				newGraphCards.next(newGraphDialog.getContentPane());
				newGraphDialog.pack();
			}
		} catch (ClassCastException e) {
			showErrorDialog("The time increment must be a whole number.");
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
		
		// Updating the graph panel.
		List<String> ids = new ArrayList<String>();
		for (String name: newGraphInfected.getSelectedValuesList()) {
			ids.add(tgh.getVertexNamesToIDs().get(name));
		}
		tgh.giveTokenTo(ids);
		graphPanel.updateVirus((String) newGraphVirus.getSelectedItem());
		timestep = 0;
		graphPanel.updateTimestep(timestep);
		updateVerticesTable();
		graphPanel.updateCurrentTimestamp(tgh.getCurrentTimestamp());
		graphPanel.updateFinalTimestamp(tgh.getFinalTimestamp());
		
		// Updating the constraints panel.
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(tgh.getCurrentTimestamp());
		Date startDate = calendar.getTime();
		calendar.setTimeInMillis(tgh.getFinalTimestamp());
		Date endDate = calendar.getTime();
		mergeStart.setModel(new SpinnerDateModel(startDate,startDate,endDate,Calendar.MILLISECOND));
		mergeStart.setEditor(new JSpinner.DateEditor(mergeStart, "dd/MM/yyyy HH:mm:ss.SSS"));
		((DefaultEditor) mergeStart.getEditor()).getTextField().setColumns(0);
		
		Set<String> vertices = tgh.getVertexNamesToIDs().keySet();
		DefaultComboBoxModel<String> model1 = new DefaultComboBoxModel<String>();
		DefaultComboBoxModel<String> model2 = new DefaultComboBoxModel<String>();
		model1.addAll(vertices);
		model2.addAll(vertices);
		deleteVertex1.setModel(model1);
		deleteVertex2.setModel(model2);
		
		newGraphDialog.setVisible(false);
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
			String sourceName = newSourceName.getText();
			String inputPath = newSourcePath.getText();
			inputType inputType = null;
			
			Boolean error = false;
			List<String> errorMsgs = new ArrayList<String>(3);
			if (sourceName.length() == 0) {
				error = true;
				errorMsgs.add("A name is required.");
			} else if (DataSources.getInstance().get(sourceName) != null) {
				error = true;
				errorMsgs.add("A data source already exists with that name.");
			}
			if (inputPath.length() == 0) {
				error = true;
				errorMsgs.add("An input data location is required.");
			}
			try {
				inputType = TemporalDataFactory.inputType.valueOf((String) newSourceInputType.getSelectedItem());
			} catch (Exception e) {
				error = true;
				errorMsgs.add("Invalid input type.");
			}
			
			if (error) {
				showErrorDialog(errorMsgs);
			} else {
				if (TemporalDataFactory.createCSVDataSource(inputPath, inputType, sourceName) == null) {
					showErrorDialog("The input data location is invalid.");
				} else {
					newSourceDialog.setVisible(false);
					newSourceName.setText("");
					newSourcePath.setText("");
					refreshNewGraphSource();
					newGraphSource.setSelectedItem(sourceName);
				}
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
			List<String> errorMsgs = new ArrayList<String>(2);
			Boolean error = false;
			if (newVirusProb.getText().length() == 0) {
				error = true;
				errorMsgs.add("A probability is required.");
			}
			if (newVirusName.getText().length() == 0) {
				error = true;
				errorMsgs.add("A name is required.");
			}

			if (error) {
				showErrorDialog(errorMsgs);
			} else {
				Double prob = Double.valueOf(newVirusProb.getText());
				if (prob < 0.0 || prob > 1.0) {
					showErrorDialog("The probability must in the range 0 to 1.");
				} else {
					String name = newVirusName.getText();
					int i = showConfirmDialog("A virus already exists with this name,"
							+ " do you want to overwrite it?");
					if (i == JOptionPane.YES_OPTION) {
						Tokens.getInstance().set(name, prob);
						newVirusDialog.setVisible(false);
						newVirusName.setText("");
						newVirusProb.setText("");
						refreshNewGraphVirus();
						newGraphVirus.setSelectedItem(name);
					}
				}
			}
		} catch (NumberFormatException nfe) {
			showErrorDialog("The probability must a number in the range 0 to 1.");
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	// --- Constraint Methods ---
	private void applyMerge() {
		try {
			if (tgh != null) {
				List<String> errorMsgs = new ArrayList<String>(2);
				Boolean error = false;
				if (mergeStart.getValue() == null) {
					error = true;
					errorMsgs.add("A start time is required.");
				}
				if (mergeDuration.getValue() == null) {
					error = true;
					errorMsgs.add("A duration is required.");
				}
				
				if (error) {
					showErrorDialog(errorMsgs);
				} else {
					long startTime = ((Date) mergeStart.getValue()).toInstant().toEpochMilli();
					long duration = (Long) mergeDuration.getValue();
					tgh.mergeEdges(startTime, duration);
				}
			}
		}  catch (NumberFormatException nfe) {
			showErrorDialog("The duration must be in milliseconds.");
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	private void applyDelay() {
		try {
			if (tgh != null) {
				if (delayTime.getValue() != null) {
					long time = (Long) delayTime.getValue();
					tgh.delayEdges(time);
				} else {
					showErrorDialog("A time amount is required.");
				}
			}
		}  catch (NumberFormatException nfe) {
			showErrorDialog("The time amount must be in milliseconds.");
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	private void applyTempLimit() {
		try {
			if (tgh != null) {
				if (tempLimit.getValue() != null) {
					int limit = (int) tempLimit.getValue();
					tgh.limitTemporality(limit);
				} else {
					showErrorDialog("A limit is required.");
				}
			}
		}  catch (NumberFormatException nfe) {
			showErrorDialog("The limit must be an integer.");
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	private void applyDelete() {
		try {
			if (deleteVertex1.getSelectedItem() != null && deleteVertex2.getSelectedItem() != null) {
				String vertex1 = tgh.getVertexNamesToIDs().get((String) deleteVertex1.getSelectedItem());
				String vertex2 = tgh.getVertexNamesToIDs().get((String) deleteVertex2.getSelectedItem());
				tgh.deleteEdgeBetween(vertex1, vertex2);
			}
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	private void displayTemporalities() {
		if (tgh != null) {
			List<Triple<String, String, Long>> temps = tgh.getTemporalities();
			Map<String, String> vertexNms = tgh.getVertexIDsToNames();
			int size = temps.size();
			Object[][] data = new Object[size][];
			for (int i = 0; i < size; i++) {
				Triple<String, String, Long> edge = temps.get(i);
				data[i] = new Object[]{vertexNms.get(edge.getLeft()),vertexNms.get(edge.getMiddle()),edge.getRight()};
			}
			
			String[] columns = new String[]{"first vertex","second vertex","temporality"};
			JTable table = new JTable(data,columns);
			table.getTableHeader().setReorderingAllowed(false);
			table.setModel(new DefaultTableModel(data,columns){
				private static final long serialVersionUID = -6939266935480600847L;
				@Override
			    public boolean isCellEditable(int row, int column) {
			       return false;
			    }
			});
			
			JDialog dialog = new JDialog();
//			dialog.add(new JLabel("The edges are specified by the vertices they exist between."));
			dialog.add(new JScrollPane(table));
			dialog.setTitle("Temporalities of Edges");
			dialog.pack();
			dialog.setVisible(true);
		}
	}
	
	private void displayReachabilitySets() {
		if (tgh != null) {
			tgh.getReachabilitySets();
			List<Tuple2<String, List<String>>> reachSets = tgh.getReachabilitySets();
			Map<String, String> vertexNms = tgh.getVertexIDsToNames();
			int size = reachSets.size();
			Object[][] data = new Object[size][];
			
			for (int i = 0; i < size; i++) {
				Tuple2<String, List<String>> set = reachSets.get(i);
				String[] line = new String[2];
				line[0] = vertexNms.get(set.f0);
				String reachableVertices = "";
				for (String id: set.f1) {
					reachableVertices += ", "+vertexNms.get(id);
				}
				
				line[1] = reachableVertices.substring(2);
				data[i] = line;
			}
			
			String[] columns = new String[]{"origin vertex","reachable vertices"};
			JTable table = new JTable(data,columns);
			table.getTableHeader().setReorderingAllowed(false);
			table.setModel(new DefaultTableModel(data,columns){
				private static final long serialVersionUID = -6939266935480600847L;
				@Override
			    public boolean isCellEditable(int row, int column) {
			       return false;
			    }
			});
			
			JDialog dialog = new JDialog();
			dialog.add(new JScrollPane(table));
			dialog.setTitle("Reachability Sets of Vertices");
			dialog.pack();
			dialog.setVisible(true);
		}
	}
	
	// --- GUI Update Methods ---
	private void nextTimeStep() {
		if (tgh != null && tgh.nextTimeStep()) {
			timestep ++;
			graphPanel.updateTimestep(timestep);
			graphPanel.updateCurrentTimestamp(tgh.getCurrentTimestamp());
			updateVerticesTable();
		}
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
	
	// --- Dialog Methods ---
	private void showErrorDialog(String message) {
		List<String> list = new ArrayList<String>();
		list.add(message);
		showErrorDialog(list);
	}
	
	private void showErrorDialog(List<String> messages) {
		String text = "";
		for (String m: messages) {
			text += "\n" + m; 
		}
		JOptionPane.showMessageDialog(this, text.trim(),
				"", JOptionPane.ERROR_MESSAGE);
	}
	
	private int showConfirmDialog(String message) {
		return JOptionPane.showConfirmDialog(this,
			    message,
			    "",JOptionPane.YES_NO_OPTION);
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
					nextTimeStep();
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
				case "calcTempsBtn":
					displayTemporalities();
					break;
				case "calcReachBtn":
					displayReachabilitySets();
					break;
			}
		}
		
	}
}
