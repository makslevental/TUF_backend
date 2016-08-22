package database_maks_pkg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


import javax.swing.tree.DefaultTreeModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;

import static database_maks_pkg.DB.shrds;
import static database_maks_pkg.DB.shrds_confb;
import static database_maks_pkg.DB.cols;
import static database_maks_pkg.DB.regs;
import static database_maks_pkg.DB.samps;
import static database_maks_pkg.DB.truthfs;
import static database_maks_pkg.DB.sdfs;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.UIManager;


public class Confabulator {
	JFrame frame;
	Map<Integer, String> selected_collections = new HashMap<Integer, String>();
	Map<Integer, String> confabbed_sample_uid_sid_mp = new HashMap<Integer, String>();
	Map<Integer, ListPairMap> confabbed_sample_uid_lpmp = new HashMap<Integer, ListPairMap>();
	Map<String, Integer> confabbed_sample_sid_uid_mp = new HashMap<String, Integer>();
	Map<String, Integer> confabbed_coll_sampsid_sampuid_mp = new HashMap<String, Integer>();
	Map<Integer, String> shards_map = new HashMap<Integer, String>();
	Map<Integer, String> collections_map = new HashMap<Integer, String>();
	Map<Integer, String> samples_map = new HashMap<Integer, String>();
	Map<Integer, Integer> collections_samples_bridge = new HashMap<Integer, Integer>();
	Map<String, Integer> regions_map = new HashMap<String, Integer>();
	HashMap<Integer,Integer> used_samps_in_confab_col = new HashMap<Integer,Integer>();
	HashMap<Integer,ArrayList<Integer>> col_samps_mp = new HashMap<Integer,ArrayList<Integer>>();
	
	JList list_regions_in_common = new JList();
	JList list_confabbed_collection = new JList();
	JList list_confabbed_sample = new JList();
	JScrollPane sp_shards_and_collections = new JScrollPane();
	JScrollPane sp_regions_in_common = new JScrollPane();
	JScrollPane sp_samples_in_region = new JScrollPane();
	JScrollPane sp_confabbed_samples = new JScrollPane();
	JScrollPane sp_confabbed_collection = new JScrollPane();
	final JTree tree_shards_and_collections = new JTree();
	final JTree tree_samples_in_region = new JTree();
	private final JButton btnFinnishCollection = new JButton("Finish Collection");
	private final JButton btnRemoveSamples = new JButton("Remove Sample(s)");
	private final JButton btnFinnishSample = new JButton("Finish Sample");
	
	
	private enum finnishSampleChoice {
		NEWSHARDNEWCOLLECTION, NEWSHARDNOCOLLECTION, 
		EXISTINGSHARDNEWCOLLECTION, EXISTINGSHARDEXISTINGCOLLECTION, EXISTINGSHARDNOCOLLECTION
	}
	
	private enum SampleCustomAuto {
		CUSTOM, AUTO
	}
	
	private class Shard_list {

		private JFrame frame;
		/**
		 * Create the application.
		 */
		public Shard_list() {
			initialize();
		}

		/**
		 * Initialize the contents of the frame.
		 */
		private void initialize() {
			frame = new JFrame();
			frame.setBounds(100, 100, 450, 300);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			JList list = new JList();
			GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
			groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
					.addGroup(groupLayout.createSequentialGroup()
						.addGap(126)
						.addComponent(list, GroupLayout.PREFERRED_SIZE, 187, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(133, Short.MAX_VALUE))
			);
			groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
					.addGroup(groupLayout.createSequentialGroup()
						.addGap(56)
						.addComponent(list, GroupLayout.PREFERRED_SIZE, 153, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(66, Short.MAX_VALUE))
			);
			frame.getContentPane().setLayout(groupLayout);
		}
	}
	
	
	private class ListPairMap<V> extends HashMap<String,V> {
		public ListPairMap(String name, V uid) {
			super();
			this.put(name, uid);
		}
		public ListPairMap() {
			super();
		}
		@Override
		public String toString() {
			return (String) this.keySet().toArray()[0];
		}
	}
	
	private class SamplesTreeExpansionListener implements TreeExpansionListener {

		@Override
		public void treeExpanded(TreeExpansionEvent event) {		
			for(Enumeration<UidDefaultMutableTreeNode> samples = ((UidDefaultMutableTreeNode) event.getPath().getLastPathComponent()).children();
					samples.hasMoreElements();) {
				UidDefaultMutableTreeNode sample = samples.nextElement();
				if(used_samps_in_confab_col.keySet().contains(sample.uid))
						sample.used = true;
			}
		}

		@Override
		public void treeCollapsed(TreeExpansionEvent event) {
			// do nothing			
		}		
	}
	

	private class ColletionsTreeExpansionListener implements TreeExpansionListener {

		@Override
		public void treeExpanded(TreeExpansionEvent event) {		
			//do nothing
		}

		@Override
		public void treeCollapsed(TreeExpansionEvent event) {
			for(Enumeration<UidDefaultMutableTreeNode> collections = ((UidDefaultMutableTreeNode) event.getPath().getLastPathComponent()).children();
					collections.hasMoreElements();) {
				UidDefaultMutableTreeNode collection = collections.nextElement();
				if(selected_collections.containsKey(collection.uid))
					selected_collections.remove(collection.uid);
			}
		}		
	}
	
	private class SampleStrikeoutCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override 
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
        	super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        	Map attributes = tree.getFont().getAttributes();  
        	Map newAttributes = new HashMap<TextAttribute, Object>();
        	for(Object entry: attributes.entrySet()) {
        		Map.Entry etry = (Map.Entry) entry;
        		newAttributes.put(etry.getKey(),etry.getValue());
        	}
        	if(leaf){
        		UidDefaultMutableTreeNode node = (UidDefaultMutableTreeNode) value;
            	if(used_samps_in_confab_col.keySet().contains(node.uid) || confabbed_sample_uid_lpmp.containsKey(node.uid)){
            		newAttributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            	}
            	else {
            		newAttributes.remove(TextAttribute.STRIKETHROUGH);
            	}
            	setFont(new Font(newAttributes));
        		return this;
        	}
        	else {
        		newAttributes.remove(TextAttribute.STRIKETHROUGH);
        	}
        	setFont(new Font(newAttributes));
            return this;
        }
    }
	
	public class SampleLeafOnlyTreeSelectionModel extends DefaultTreeSelectionModel {
		private static final long serialVersionUID = 1L;
	
		private TreePath[] augmentPaths(TreePath[] pPaths) {
		    ArrayList<TreePath> paths = new ArrayList<TreePath>();
		    
		    for (int i = 0; i < pPaths.length; i++) {
		        if (pPaths != null && pPaths[i] != null && ((DefaultMutableTreeNode) pPaths[i].getLastPathComponent()).isLeaf()) {
		            paths.add(pPaths[i]);
		        }
		    }
	
		    return paths.toArray(pPaths);
		}
	
		@Override
		public void setSelectionPaths(TreePath[] pPaths) {
		    super.setSelectionPaths(augmentPaths(pPaths));
		}
	
		@Override
		public void addSelectionPaths(TreePath[] pPaths) {
		    super.addSelectionPaths(augmentPaths(pPaths));
		}
		
		public void clearSelection() {
			// do nothing
		}
	}

	
	public class CollectionLeafOnlyTreeSelectionModel extends DefaultTreeSelectionModel {
		private static final long serialVersionUID = 1L;
		JTree tree;
		public CollectionLeafOnlyTreeSelectionModel(JTree tree) {
			super();
			this.tree = tree;
		}
		private TreePath[] augmentPaths(TreePath[] pPaths) {
			
		    TreePath[] tree_paths = this.tree.getSelectionPaths();
		    ArrayList<TreePath> paths;
		    if(tree_paths != null)
		    	paths = new ArrayList<TreePath>(Arrays.asList(tree_paths));
		    else
		    	paths = new ArrayList<TreePath>();
		    
		    for (int i = 0; i < pPaths.length; i++) {
		        if (pPaths != null && pPaths[i] != null && ((DefaultMutableTreeNode) pPaths[i].getLastPathComponent()).isLeaf()) {
		            paths.add(pPaths[i]);
		        }
		    }
		    return paths.toArray(pPaths);
		}
	
		@Override
		public void setSelectionPaths(TreePath[] pPaths) {
		    super.setSelectionPaths(augmentPaths(pPaths));
		}
	
		@Override
		public void addSelectionPaths(TreePath[] pPaths) {
		    super.addSelectionPaths(augmentPaths(pPaths));
		}
	}
	public class UidDefaultMutableTreeNode extends DefaultMutableTreeNode {
		int uid;
		String name;
		int[] uids;
		boolean used;
		public UidDefaultMutableTreeNode(Object data, int uid, String name) {
			super(data);
			this.uid = uid;
			this.name = name;
			this.used = false;
		}
		public UidDefaultMutableTreeNode() {
			super();
		}
		
	}
	
	public class TUFListModel extends DefaultListModel<ListPairMap> {
		private static final long serialVersionUID = 1L;
		public TUFListModel(final String[] values, final int[] uids) {
			super();
			for(int i = 0; i < values.length; i++){
				this.addElement(new ListPairMap(values[i], uids[i]));
			}
		}
		public TUFListModel() {
			super();
		}
		
	}
	
	public class SampleSelection implements TreeSelectionListener  {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath selected_samp_path = e.getPath();
			UidDefaultMutableTreeNode node = (UidDefaultMutableTreeNode) selected_samp_path.getLastPathComponent();
			int samp_uid = node.uid;
			
			if(!used_samps_in_confab_col.keySet().contains(samp_uid)){
				Object[] selected_samp_node_oath = selected_samp_path.getPath();
				String sid = "<html>" + node.name + "@<br>&emsp;" + ((UidDefaultMutableTreeNode) selected_samp_node_oath[1]).name +": " + node.uid + "</html>";
				if (e.isAddedPath(selected_samp_path) && !confabbed_sample_uid_lpmp.containsKey(samp_uid)) {
					ListPairMap list_entry = new ListPairMap(sid, samp_uid);
					confabbed_sample_uid_lpmp.put(samp_uid, list_entry);
					((TUFListModel) list_confabbed_sample.getModel()).addElement(list_entry);						
				}
				else {
					((TUFListModel) list_confabbed_sample.getModel()).removeElement(confabbed_sample_uid_lpmp.get(node.uid));
					confabbed_sample_uid_lpmp.remove(samp_uid);
					
				}
				// crosses out nodes that have the same sample uid
				((DefaultTreeModel)tree_samples_in_region.getModel()).reload(node);
			}

	    }
		
	}
	

	
	public class CollectionSelection implements TreeSelectionListener {

		@Override
		public void valueChanged(TreeSelectionEvent e)  {
			TreePath[] selected_paths = tree_shards_and_collections.getSelectionPaths();
			tree_samples_in_region.setModel(new DefaultTreeModel(new UidDefaultMutableTreeNode()));
			selected_collections.clear();
			if(selected_paths != null)
				for(TreePath path: selected_paths) {
					UidDefaultMutableTreeNode node = (UidDefaultMutableTreeNode) path.getLastPathComponent();
					if(tree_shards_and_collections.getModel().isLeaf(node))
						selected_collections.put(node.uid, node.name);
				}
			try {
				populateRegionsList();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
				

	    }
		
	}
	
	
	public class RegionSelection implements ListSelectionListener  {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getValueIsAdjusting()) {
				JList j = (JList) e.getSource();
				if(!j.isSelectionEmpty()) {
					confabbed_sample_uid_lpmp.clear();
					((TUFListModel) list_confabbed_sample.getModel()).clear();
					int region_uid = (int) ((HashMap<String,Integer>) j.getModel().getElementAt(j.getSelectedIndex())).values().toArray()[0];
					
					ArrayList<Integer> temp = new ArrayList<Integer>(selected_collections.keySet());
					int[] selected_collections_uids = new int[temp.size()];
					for(int i = 0; i < temp.size(); i++) {
						selected_collections_uids[i] = temp.get(i).intValue();
					}
					try {
						col_samps_mp = cols.getIntersectionSamplesUidsMap(selected_collections_uids,region_uid);
						populateSamplesInRegionTree(col_samps_mp);
					} catch (SQLException e1) {
						// can't throw exception so just bail
						e1.printStackTrace();
						System.exit(-1);
					}
				}
			}
		}
		
	}

	public class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			TUFListModel listColModel = (TUFListModel) list_confabbed_collection.getModel();
			int[] col_uids;
			switch(e.getActionCommand()) {
				case "finsamp": // make new confabbed sample and clear things
					if(confabbed_sample_uid_lpmp.isEmpty())
						break;
					confabbed_sample_uid_lpmp.clear();
					TUFListModel listSampModel = (TUFListModel) list_confabbed_sample.getModel();
					StringBuilder newSampleName = new StringBuilder("<html>sample:<br>&emsp;&emsp;");
					ArrayList<Integer> newSampleUids = new ArrayList<Integer>();
					for (Enumeration<ListPairMap> e1 = listSampModel.elements(); e1.hasMoreElements();) {
						ListPairMap temp = e1.nextElement();
						newSampleName.append(temp.toString().replace("</html>", "<br>&emsp;&emsp;"));
						Integer samp_uid = (Integer) temp.get(temp.toString()); 
						newSampleUids.add(samp_uid); 
						int region_uid = (int) ((HashMap<String,Integer>) list_regions_in_common.getSelectedValue()).values().toArray()[0];
						used_samps_in_confab_col.put(samp_uid, region_uid);
						
					}
					newSampleName.append("</html>");
					ListPairMap<ArrayList<Integer>> confabbed_samp = new ListPairMap<ArrayList<Integer>>(newSampleName.toString(), newSampleUids);
					listColModel.addElement(confabbed_samp);
					listSampModel.clear();
					break;
				case "rmvsamps": // clear things
					int[] samps_to_rmv = list_confabbed_collection.getSelectedIndices();
					ListPairMap samp = new ListPairMap();
					for(int i: samps_to_rmv) {
						samp = listColModel.get(i);
						for(Integer subsamp: (ArrayList<Integer>) samp.get(samp.toString())) 
							used_samps_in_confab_col.remove(subsamp);
						listColModel.remove(i);
					}
					try {
						populateSamplesInRegionTree(col_samps_mp);
					} catch (SQLException e1) { // can't throw exception so just bail
						e1.printStackTrace();
						System.exit(-1);
					}
					break;
				case "fincol": 
					// TODO: check for collision with shard names, collection names, sample names, etc
					finnishSampleChoice chs; // ask about shard name/desc and collection name/desc
					JFrame frame = new JFrame(); // for the dialog boxes
				    String[] options = {"Adding", "New Shard", "Cancel"};

				    String shard_name = null, collection_name = null, shard_desc = null, collection_desc = null;
					String[] col_names = null, nameanddesc = null, shard_names = null;
					int[] shard_uids = null, coll_uids = null;
					try {
						shard_uids = shrds_confb.getAll();
						shard_names = new String[shard_uids.length];
						for(int i = 0; i < shard_uids.length; i++)
							shard_names[i] = shrds_confb.getName(shard_uids[i]);
					} catch (SQLException e1) {// can't throw exception so just bail
						e1.printStackTrace();
						System.exit(-1);
					}
			
					int choice = JOptionPane.showOptionDialog(frame,"Are you adding to an existing Shard or would you like to create a new one?","Shard",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
					if(choice == 0) { // use existing shard
						shard_name = ListDialog.showDialog(frame,frame,"Existing Shards:","Shards",shard_names,null,null);
						if(shard_name == null) // cancel was hit on list of shard choices
							return;
						else { // shard was picked from list
							try {
								
								coll_uids = shrds_confb.getCollectionUids(shrds_confb.getUID(shard_name));
								col_names = new String[coll_uids.length];
								for(int i = 0; i < coll_uids.length; i++)
									col_names[i] = cols.getName(coll_uids[i]);
							} catch (SQLException e1) {// can't throw exception so just bail								
								e1.printStackTrace();
								System.exit(-1);
							}
							
							options[1] =  "New Collection"; options[2] =  "No Collection";
							choice = JOptionPane.showOptionDialog(frame,"Are you adding to an existing Collection, would you like to create a new one, or no collection?","Collection",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

							if(choice == 0) { // show list of collections
								collection_name = ListDialog.showDialog(frame,frame,"Existing Collections:","Collections",col_names,null,null);
								chs = finnishSampleChoice.EXISTINGSHARDEXISTINGCOLLECTION;
								if(collection_name == null) // cancel was hit on list of collections
									return;									
							}
							else if (choice == 1) { // new collection in existing shard
								chs = finnishSampleChoice.EXISTINGSHARDNEWCOLLECTION;
								nameanddesc = getNameAndDescription("Collection", col_names);
								if(nameanddesc[0] !=null)
									collection_name = nameanddesc[0];
								else
									return;
								collection_desc = nameanddesc[1];		
							}
							else // no collection
								chs = finnishSampleChoice.EXISTINGSHARDNOCOLLECTION;
						}
					}
					else if (choice == 1){ // new shard name
						nameanddesc = getNameAndDescription("Shard", shard_names);
						if(nameanddesc[0] != null)
							shard_name = nameanddesc[0];
						else
							return;
						shard_desc = nameanddesc[1];
						choice = JOptionPane.showOptionDialog(frame,"New collection or no collection?","Collection",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						options[0] = "New Collection"; options[1] =  "No Collection"; options[2] =  "Cancel";
						if(choice == 0) { // new collection
							chs = finnishSampleChoice.NEWSHARDNEWCOLLECTION;
							nameanddesc = getNameAndDescription("Collection", col_names);
							if(nameanddesc[0] !=null)
								collection_name = nameanddesc[0];
							else
								return;
							collection_desc = nameanddesc[1];								
						}
						else if (choice == 1) { // no collection
							chs = finnishSampleChoice.NEWSHARDNOCOLLECTION;
						}
						else // cancel
							return;
					}
					else // cancel has hit on choose shard 
						return;
					
					ArrayList<String> confabshard_lines = new ArrayList<String>();				
					ArrayList<String> newsample_names = addSamplesToShardCsvLines(shard_name, confabshard_lines);
					if(newsample_names == null)
						return;
					
					if(chs == finnishSampleChoice.NEWSHARDNEWCOLLECTION || chs == finnishSampleChoice.NEWSHARDNOCOLLECTION) {
						Calendar cSchedStartCal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
						// preserve daylight savings time : https://www.mathworks.com/matlabcentral/answers/101443-how-can-i-convert-java-dates-to-matlab-dates-and-retain-daylight-saving-time-details-when-using-matl
						long datenum = cSchedStartCal.getTimeInMillis() - cSchedStartCal.get(cSchedStartCal.ZONE_OFFSET) - cSchedStartCal.get(cSchedStartCal.DST_OFFSET);
						confabshard_lines.add(MessageFormat.format("$shds$|${0}$|${1}$|${2}$|${3}$",shard_desc,shard_name, datenum));
					}
					if(chs == finnishSampleChoice.EXISTINGSHARDNEWCOLLECTION || chs == finnishSampleChoice.NEWSHARDNEWCOLLECTION)
						confabshard_lines.add(MessageFormat.format("$cols$|${0}$|${1}$|${1}$|${2}$",collection_desc,collection_name, shard_name));
					
					if(chs == finnishSampleChoice.EXISTINGSHARDEXISTINGCOLLECTION)
						for(String newsmp_name: newsample_names) {
							try {
								confabshard_lines.add(MessageFormat.format("$clsb$|${0}$|${1}$|${2}$|${3}", cols.getName(cols.getUID(collection_name, shard_name)), shard_name, newsmp_name, shard_name));
							} catch (SQLException e1) {// can't throw exception so just bail
								e1.printStackTrace();
								System.exit(-1);
							}	
						}
						
										
					JFileChooser c = new JFileChooser();
					JTextField filename = new JTextField();
					JTextField dir = new JTextField();
					int rVal = c.showSaveDialog(frame);	
					if (rVal == JFileChooser.APPROVE_OPTION) {
				        filename.setText(c.getSelectedFile().getAbsolutePath());
				      }
					if (rVal == JFileChooser.CANCEL_OPTION) {
				        return;
					}
					try {
						PrintWriter writer = new PrintWriter(filename.getText(), "UTF-8");
						for(String line: confabshard_lines)
							writer.println(line);
						writer.close();
					} catch (FileNotFoundException e1) { // can't throw exception so just bail
						e1.printStackTrace();
						System.exit(-1);
					} catch (UnsupportedEncodingException e1) {	// can't throw exception so just bail
						e1.printStackTrace();
						System.exit(-1);
					}					
			}
		}
		
	}
	
	private ArrayList<String> addSamplesToShardCsvLines(String shard_name, ArrayList<String> confabshard_lines) {

		String[] options = {"Auto", "Custom", "Cancel"};
		ArrayList<String> newsamp_names = new ArrayList<String>();
		int choice = JOptionPane.showOptionDialog(frame,"Would you like to automatically name/describe or custom name/describe each confabulated sample?","Samples",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
		SampleCustomAuto schs;
		int numExistingSamps = 0;
		ArrayList<String> both_samp_names = null;
		// this is stupid i know
		if(choice == 0) {
			schs = SampleCustomAuto.AUTO;
			try {
				numExistingSamps = shrds_confb.getSampleUids(shrds_confb.getUID(shard_name)).length;
			} catch (SQLException e1) {// can't throw exception so just bail								
				e1.printStackTrace();
				System.exit(-1);
			}
		}
		else if(choice == 1) {
			int[] sample_uids = null;
			String[] samp_names = null;
			try {
				sample_uids = shrds_confb.getSampleUids(shrds_confb.getUID(shard_name));
				samp_names = new String[sample_uids.length];
				for(int i = 0; i < sample_uids.length; i++)
					samp_names[i] = samps.getName(sample_uids[i]);
			} catch (SQLException e1) {// can't throw exception so just bail
				e1.printStackTrace();
				System.exit(-1);
			}
			both_samp_names = new ArrayList<String>(Arrays.asList(samp_names));
			schs = SampleCustomAuto.CUSTOM;
		}
		else // cancel
			return null;

		
		// $samp$|$description$|$name$|$uid_shard$|$uid_region$|$truthfn$|$truthfp$
		String sampsform = "$samps$|${0}$|${1}$|${2}$|${3}$|${4}$|${5}$";
		// $sdfsb$|$sdfn$|$sdfp$|$sampname$|$sampshardname$
		String sdfsbform = "$sdfsb$|${0}$|${1}$|${2}$|${3}";
		for(Enumeration<ListPairMap> samples = ((TUFListModel) list_confabbed_collection.getModel()).elements(); samples.hasMoreElements();) {

			ListPairMap<ArrayList<Integer>> sample = samples.nextElement();
			String smp = (String) sample.keySet().toArray()[0]; // this is stupid since there's only one key

			ArrayList<Integer> subsampleuids = sample.get(smp);
			String regname = null, truthfn = null, truthfp = null;
			String sample_name = null, sample_desc = null;
			try {
				// check if only one region per sample
				int regtest = used_samps_in_confab_col.get(subsampleuids.get(0));
				for(int i = 1; i < subsampleuids.size(); i++) {
					if(regtest != used_samps_in_confab_col.get(subsampleuids.get(0))) {
						JOptionPane.showMessageDialog(frame,
							    "Regions don't overlap: sample "+subsampleuids.get(0),
							    "Error",
							    JOptionPane.ERROR_MESSAGE);
						return null;
					}
						
				}
				regname = regs.getName(used_samps_in_confab_col.get(subsampleuids.get(0)));
				truthfn = truthfs.getFilename(samps.getTruthFileUids(subsampleuids.get(0))[0]);
				truthfp = truthfs.getPath(samps.getTruthFileUids(subsampleuids.get(0))[0]);
			} catch (SQLException e1) {// can't throw exception so just bail								
				e1.printStackTrace();
				System.exit(-1);
			}

			if(schs == SampleCustomAuto.AUTO){
				numExistingSamps += 1;
				sample_name = "sample"+numExistingSamps;
				
				Matcher match = Pattern.compile(":(.*)").matcher(smp.replace("<html>", "").replace("</html>", "").replace("<br>&emsp;&emsp;", ",").replace("<br>&emsp;", ""));
				match.find();
				StringBuilder description = null;
				description = new StringBuilder(match.group(0).replaceAll(":\\s\\d*", ""));
				description.delete(0, 2);
				description.delete(description.length()-1, description.length());
				sample_desc = description.toString();
			}
			else if (schs == SampleCustomAuto.CUSTOM) {
				String[] nameanddesc = getNameAndDescription("Sample", both_samp_names.toArray(new String[both_samp_names.size()]));
				if(nameanddesc[0] !=null) {
					sample_name = nameanddesc[0];
					both_samp_names.add(sample_name);
				}
				else // cancel
					return null;
				sample_desc = nameanddesc[1];	
			}
			newsamp_names.add(sample_name);
			confabshard_lines.add(MessageFormat.format(sampsform, sample_desc,sample_name,shard_name,regname,truthfn,truthfp));
			for(Integer sampuid: subsampleuids) {
				try {
					int[] sdfss = samps.getSDFileUids(sampuid);
					for(int sdfuid: sdfss) 
						confabshard_lines.add(MessageFormat.format(sdfsbform, sdfs.getPath(sdfuid), sdfs.getFilename(sdfuid), sample_name, shard_name));	
				} catch (SQLException e1) {// can't throw exception so just bail								
					e1.printStackTrace();
					System.exit(-1);
				}
			}
			
		}			
		return newsamp_names;
	}
	
	public String[] getNameAndDescription(String type, String[] names) {
	    JTextField name = new JTextField();
	    JTextField description = new JTextField();
	    Object[] message = {"Name:", name, "Description", description};
		String nme;
		String desc;
		HashSet<String> hash_names = new HashSet<String>(Arrays.asList(names));
		while(true) {
			int choice = JOptionPane.showConfirmDialog(frame, message, type, JOptionPane.OK_CANCEL_OPTION);
			if(choice == JOptionPane.OK_OPTION) {
				nme = name.getText();
				if(hash_names.contains(name)){
					JOptionPane.showMessageDialog(frame,
						    "Collection name already exists",
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				else { // new, unique collection name was chosen
					desc = description.getText();
					break;
				}
			}
			else // cancel was hit on entering new name/desc
				return null;
		}
		return new String[] {nme,desc};
	}

	
	private void populateSamplesInRegionTree(final HashMap<Integer,ArrayList<Integer>> col_samps_mp) throws SQLException {
		tree_samples_in_region.setModel(new DefaultTreeModel(
				new DefaultMutableTreeNode("Samples") {
					private static final long serialVersionUID = 2L;
					{
						for(Integer coluid: col_samps_mp.keySet()){
							String colname = cols.getName(coluid.intValue());
							add( 
									// collection branch
									new UidDefaultMutableTreeNode(colname,coluid,colname) {
										{
											ArrayList<Integer> uids_array = col_samps_mp.get(this.uid); 
											int[] temp = new int[uids_array.size()];
											for(int i = 0; i < temp.length; i++) {
												temp[i] = uids_array.get(i);
											}
											uids = temp;
											for(int sampuid : uids){
												// sample leaf
												String sample_name = samps.getName(sampuid);
												add(new UidDefaultMutableTreeNode(sample_name,sampuid,sample_name));
											}
										}
										
									});
						}

					}
				}
			));
		// necessary so that switching collections doesn't remove last selected
		// sample
		tree_samples_in_region.setSelectionModel(new SampleLeafOnlyTreeSelectionModel());
		for (int i = 0; i < tree_samples_in_region.getRowCount(); i++) {
			tree_samples_in_region.expandRow(i);
		}
	}


	private void populateRegionsList() throws SQLException {
		if(selected_collections.isEmpty()){
			list_regions_in_common.setModel(new TUFListModel());
		}
		else {
			ArrayList<Integer> temp = new ArrayList<Integer>(selected_collections.keySet());
			int[] selected_collections_uids = new int[temp.size()];
			for(int i = 0; i < temp.size(); i++) {
				selected_collections_uids[i] = temp.get(i).intValue();
			}
			int[] intersection = cols.getIntersectionRegionUID(selected_collections_uids);
			String[] list_values = new String[intersection.length];
			for(int i = 0; i < intersection.length; i++) {
				String reg_name = regs.getName(intersection[i]);
				regions_map.put(reg_name, intersection[i]);
				list_values[i] = reg_name;
			}
			list_regions_in_common.setModel(new TUFListModel(list_values, intersection));
		}
	}
	/**
	 * Create the application.
	 * @throws SQLException 
	 */
	public Confabulator() throws SQLException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws SQLException 
	 */
	private void initialize() throws SQLException {
		frame = new JFrame();
		frame.setBounds(100, 100, 1066, 725);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel panel = new JPanel();
		
		JLabel lblNewLabel = new JLabel("Collections");
		
		JLabel lblConfabulatedRegions = new JLabel("Region Intersection");
		
		JLabel lblConfabulableSamples = new JLabel("Samples in Region");
		
		JLabel lblConfabulatedSample = new JLabel("Working Sample");
		
		

		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addContainerGap()
							.addComponent(sp_shards_and_collections, GroupLayout.PREFERRED_SIZE, 262, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(94)
							.addComponent(lblNewLabel)))
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(18)
							.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
								.addGroup(gl_panel.createSequentialGroup()
									.addComponent(sp_regions_in_common, GroupLayout.PREFERRED_SIZE, 197, GroupLayout.PREFERRED_SIZE)
									.addGap(18)
									.addComponent(sp_samples_in_region, GroupLayout.PREFERRED_SIZE, 205, GroupLayout.PREFERRED_SIZE))
								.addComponent(btnRemoveSamples)
								.addComponent(btnFinnishCollection))
							.addPreferredGap(ComponentPlacement.RELATED, 130, Short.MAX_VALUE)
							.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
								.addGroup(gl_panel.createSequentialGroup()
									.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false)
										.addComponent(sp_confabbed_samples, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(sp_confabbed_collection, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
									.addGap(19))
								.addGroup(gl_panel.createSequentialGroup()
									.addComponent(btnFinnishSample)
									.addGap(78))))
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(54)
							.addComponent(lblConfabulatedRegions)
							.addGap(77)
							.addComponent(lblConfabulableSamples)
							.addPreferredGap(ComponentPlacement.RELATED, 163, Short.MAX_VALUE)
							.addComponent(lblConfabulatedSample)
							.addGap(86))))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblConfabulatedSample)
						.addComponent(lblNewLabel)
						.addComponent(lblConfabulableSamples)
						.addComponent(lblConfabulatedRegions))
					.addGap(7)
					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
						.addComponent(sp_shards_and_collections, GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
						.addGroup(gl_panel.createSequentialGroup()
							.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
								.addComponent(sp_samples_in_region, GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
								.addComponent(sp_regions_in_common, GroupLayout.PREFERRED_SIZE, 402, GroupLayout.PREFERRED_SIZE))
							.addGap(88)
							.addComponent(btnRemoveSamples)
							.addGap(28)
							.addComponent(btnFinnishCollection)
							.addGap(19))
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(sp_confabbed_samples, GroupLayout.PREFERRED_SIZE, 222, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(btnFinnishSample)
							.addGap(12)
							.addComponent(sp_confabbed_collection, GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)))
					.addGap(66))
		);
		
		panel.setLayout(gl_panel);
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 1038, Short.MAX_VALUE)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addGap(24)
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 676, Short.MAX_VALUE))
		);
		frame.getContentPane().setLayout(groupLayout);
		
		tree_shards_and_collections.setSelectionModel(new CollectionLeafOnlyTreeSelectionModel(tree_shards_and_collections));
		tree_shards_and_collections.setShowsRootHandles(true);
		tree_shards_and_collections.setRootVisible(false);
		tree_shards_and_collections.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("Shards") {
				private static final long serialVersionUID = 2L;
				{
					final int[] shard_uids = shrds.getAll();
					for(int shrduid: shard_uids){
						if (new HashSet<String>(Arrays.asList("Base", "APHillBase", "YumaBase", "Objects")).contains(shrds.getName(shrduid))) 
							continue;
						
						String shrd_name = shrds.getName(shrduid);
						add( 
								// shard branch
								new UidDefaultMutableTreeNode(shrd_name,shrduid,shrd_name) {
									{
										uids = shrds.getCollectionUids(this.uid);
										for(int colluid : uids){
											// collection leaf
											String col_name = cols.getName(colluid);
											add(new UidDefaultMutableTreeNode(col_name,colluid,col_name));
										}
									}
									
								});
					}

				}
			}
		));
		tree_shards_and_collections.addTreeSelectionListener(new CollectionSelection());
		sp_shards_and_collections.setViewportView(tree_shards_and_collections);
		
		list_regions_in_common.setVisibleRowCount(20);
		list_regions_in_common.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list_regions_in_common.addListSelectionListener(new RegionSelection());
		sp_regions_in_common.setViewportView(list_regions_in_common);
		
		tree_samples_in_region.setShowsRootHandles(true);
		tree_samples_in_region.setRootVisible(false);
		tree_samples_in_region.setSelectionModel(new SampleLeafOnlyTreeSelectionModel());
		tree_samples_in_region.addTreeExpansionListener(new SamplesTreeExpansionListener());
		tree_samples_in_region.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Samples")));
		tree_samples_in_region.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree_samples_in_region.addTreeSelectionListener(new SampleSelection());
		tree_samples_in_region.setCellRenderer(new SampleStrikeoutCellRenderer());

		sp_samples_in_region.setViewportView(tree_samples_in_region);
		
		list_confabbed_collection.setModel(new TUFListModel());
		sp_confabbed_collection.setViewportView(list_confabbed_collection);
		list_confabbed_sample.setSelectionBackground(Color.WHITE);
		list_confabbed_sample.setModel(new TUFListModel());
		sp_confabbed_samples.setViewportView(list_confabbed_sample);
		
		
		btnFinnishSample.setActionCommand("finsamp");
		btnRemoveSamples.setActionCommand("rmvsamps");
		btnFinnishCollection.setActionCommand("fincol");
		btnFinnishSample.addActionListener(new ButtonListener());
		btnRemoveSamples.addActionListener(new ButtonListener());
		btnFinnishCollection.addActionListener(new ButtonListener());
		
		
		
		
		
		
	}

	/**
	 * Use this modal dialog to let the user choose one string from a long
	 * list.  See ListDialogRunner.java for an example of using ListDialog.
	 * The basics:
	 * <pre>
	    String[] choices = {"A", "long", "array", "of", "strings"};
	    String selectedName = ListDialog.showDialog(
	                                componentInControllingFrame,
	                                locatorComponent,
	                                "A description of the list:",
	                                "Dialog Title",
	                                choices,
	                                choices[0]);
	 * </pre>
	 */
	public static class ListDialog extends JDialog
	                        implements ActionListener {
	    private static ListDialog dialog;
	    private static String value = "";
	    private JList list;

	    /**
	     * Set up and show the dialog.  The first Component argument
	     * determines which frame the dialog depends on; it should be
	     * a component in the dialog's controlling frame. The second
	     * Component argument should be null if you want the dialog
	     * to come up with its left corner in the center of the screen;
	     * otherwise, it should be the component on top of which the
	     * dialog should appear.
	     */
	    public static String showDialog(Component frameComp,
	                                    Component locationComp,
	                                    String labelText,
	                                    String title,
	                                    String[] possibleValues,
	                                    String initialValue,
	                                    String longValue) {
	        Frame frame = JOptionPane.getFrameForComponent(frameComp);
	        dialog = new ListDialog(frame,
	                                locationComp,
	                                labelText,
	                                title,
	                                possibleValues,
	                                initialValue,
	                                longValue);
	        dialog.setVisible(true);
	        return value;
	    }

	    private void setValue(String newValue) {
	        value = newValue;
	        list.setSelectedValue(value, true);
	    }

	    private ListDialog(Frame frame,
	                       Component locationComp,
	                       String labelText,
	                       String title,
	                       Object[] data,
	                       String initialValue,
	                       String longValue) {
	        super(frame, title, true);

	        //Create and initialize the buttons.
	        JButton cancelButton = new JButton("Cancel");
	        cancelButton.addActionListener(this);
	        //
	        final JButton setButton = new JButton("Select");
	        setButton.setActionCommand("Select");
	        setButton.addActionListener(this);
	        getRootPane().setDefaultButton(setButton);

	        //main part of the dialog
	        list = new JList(data) {
	            //Subclass JList to workaround bug 4832765, which can cause the
	            //scroll pane to not let the user easily scroll up to the beginning
	            //of the list.  An alternative would be to set the unitIncrement
	            //of the JScrollBar to a fixed value. You wouldn't get the nice
	            //aligned scrolling, but it should work.
	            public int getScrollableUnitIncrement(Rectangle visibleRect,
	                                                  int orientation,
	                                                  int direction) {
	                int row;
	                if (orientation == SwingConstants.VERTICAL &&
	                      direction < 0 && (row = getFirstVisibleIndex()) != -1) {
	                    Rectangle r = getCellBounds(row, row);
	                    if ((r.y == visibleRect.y) && (row != 0))  {
	                        Point loc = r.getLocation();
	                        loc.y--;
	                        int prevIndex = locationToIndex(loc);
	                        Rectangle prevR = getCellBounds(prevIndex, prevIndex);

	                        if (prevR == null || prevR.y >= r.y) {
	                            return 0;
	                        }
	                        return prevR.height;
	                    }
	                }
	                return super.getScrollableUnitIncrement(
	                                visibleRect, orientation, direction);
	            }
	        };

	        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        if (longValue != null) {
	            list.setPrototypeCellValue(longValue); //get extra space
	        }
	        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
	        list.setVisibleRowCount(100);
	        list.addMouseListener(new MouseAdapter() {
	            public void mouseClicked(MouseEvent e) {
	                if (e.getClickCount() == 2) {
	                    setButton.doClick(); //emulate button click
	                }
	            }
	        });
	        JScrollPane listScroller = new JScrollPane(list);
	        listScroller.setPreferredSize(new Dimension(250, 250));
	        listScroller.setAlignmentX(LEFT_ALIGNMENT);

	        //Create a container so that we can add a title around
	        //the scroll pane.  Can't add a title directly to the
	        //scroll pane because its background would be white.
	        //Lay out the label and scroll pane from top to bottom.
	        JPanel listPane = new JPanel();
	        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
	        JLabel label = new JLabel(labelText);
	        label.setLabelFor(list);
	        listPane.add(label);
	        listPane.add(Box.createRigidArea(new Dimension(0,5)));
	        listPane.add(listScroller);
	        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

	        //Lay out the buttons from left to right.
	        JPanel buttonPane = new JPanel();
	        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
	        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
	        buttonPane.add(Box.createHorizontalGlue());
	        buttonPane.add(cancelButton);
	        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
	        buttonPane.add(setButton);

	        //Put everything together, using the content pane's BorderLayout.
	        Container contentPane = getContentPane();
	        contentPane.add(listPane, BorderLayout.CENTER);
	        contentPane.add(buttonPane, BorderLayout.PAGE_END);

	        //Initialize values.
	        setValue(initialValue);
	        pack();
	        setLocationRelativeTo(locationComp);
	    }

	    //Handle clicks on the Set and Cancel buttons.
	    public void actionPerformed(ActionEvent e) {
	        if ("Select".equals(e.getActionCommand())) {
	            ListDialog.value = (String)(list.getSelectedValue());
	        }
	        ListDialog.dialog.setVisible(false);
	    }
	}

	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Confabulator window = new Confabulator();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
