package base;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.lang3.exception.ExceptionUtils;
import javax.swing.SwingConstants;

/*
 * This is the scuffed gui I created using windowbuilder. The overall code is garbage but was made as a quick and easy solution to the large amount of time having
 * to spam the same --encrypt, --decrypt, etc, commands over and over again for each of the thousand plus files. Depending on the command, there is multi-file selection
 * and also folder selection. ex: --create-patch <folder path> will recursively go through all the encrypted bins to add them to the patch. Prior to my change, 
 * you had to manually type or paste every directory for the patch. Another thing to note, when selecting files its best to either shift+click or ctrl+click.
 * 
 * All the console outputs have been rerouted to be printed in the gui console via the @base.Window.writeToConsole(String) method, as well as the terminal console depending on if you want to use the gui or not.
 * Which reminds me, this version of the tool can still be used in the command prompt without the gui.
 * 
 * A lot of the gui is hardcoded so any resizing is currently not possible. When testing the console I noticed a lot of stuff was cut off so I added line wrapping as well as 
 * a popout console option for easier use.
 */

public class Window {

	private JFrame frame;
	private List<String> parameters = new ArrayList<>();
	private static List<ImageWindow> imageWindows = new ArrayList<>();
	private static List<PopoutConsole> consoleWindows = new ArrayList<>();

	private JFileChooser fileChooser = new JFileChooser();
	private static JTextArea txtConsole;
	
	private String selectedPath = "";
	private String currentCommand = "--encrypt_";
	
	public static void main(String[] args) {
		if (args.length > 0) {
			MHTools.process(args);
		} else {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Throwable e) {
				e.printStackTrace();
			}
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						Window window = new Window();
						window.frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}

	}

	/**
	 * Create the application.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public Window() {

		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("MHTools");
		frame.setBounds(100, 100, 953, 520);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		String[] tools = new String[] { "--encrypt_", "--decrypt_", "--create-patch_", "--dec-all_", "--extract_ .bin",
				"--extract_ .pak", "--extract_ .tmh", "--rebuild_ .tmh", "--rebuild_ table", "--reb-enc_", "--dec-ext_",
				"--gen-index_" };

		parameters.add(tools[0].substring(0, tools[0].indexOf("_")));
		currentCommand = parameters.get(0);
		fileChooser.setMultiSelectionEnabled(true);

		JLabel curCmdLbl = new JLabel(currentCommand);
		curCmdLbl.setFont(new Font("Dialog", Font.PLAIN, 12));
		curCmdLbl.setBounds(131, 16, 100, 14);
		frame.getContentPane().add(curCmdLbl);

		JComboBox<String> cmdSelect = new JComboBox<>();
		cmdSelect.setFont(new Font("Dialog", Font.PLAIN, 12));
		cmdSelect.setModel(new DefaultComboBoxModel<String>(tools));
		cmdSelect.setBounds(10, 11, 111, 25);
		frame.getContentPane().add(cmdSelect);

		JSeparator separator = new JSeparator();
		separator.setBounds(10, 41, 244, 2);
		frame.getContentPane().add(separator);

		JLabel lblSelectedFile = new JLabel("Selected File/Dir: none");
		lblSelectedFile.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblSelectedFile.setBounds(120, 51, 464, 14);
		frame.getContentPane().add(lblSelectedFile);

		JFormattedTextField paramTxtField = new JFormattedTextField();
		paramTxtField.setFont(new Font("Dialog", Font.PLAIN, 12));
		paramTxtField.setText("Parameters: ");
		paramTxtField.setBounds(84, 430, 842, 20);
		frame.getContentPane().add(paramTxtField);
		setTextAreaText(paramTxtField);

		JButton btnBrowse = new JButton("Browse");
		btnBrowse.setFont(new Font("Dialog", Font.PLAIN, 12));
		btnBrowse.setBounds(10, 47, 100, 23);
		frame.getContentPane().add(btnBrowse);

		JButton btnClear = new JButton("Clear Files");
		btnClear.setFont(new Font("Dialog", Font.PLAIN, 12));
		btnClear.setBounds(10, 81, 100, 23);
		frame.getContentPane().add(btnClear);

		JButton btnBegin = new JButton("Begin");
		btnBegin.setFont(new Font("Dialog", Font.PLAIN, 12));
		btnBegin.setBounds(10, 396, 89, 23);
		frame.getContentPane().add(btnBegin);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 115, 564, 270);
		frame.getContentPane().add(scrollPane);

		txtConsole = new JTextArea();
		txtConsole.setFont(new Font("Dialog", Font.PLAIN, 12));
		txtConsole.setLineWrap(false);
		scrollPane.setViewportView(txtConsole);
		txtConsole.setEditable(false);

		JLabel lblParameters = new JLabel("Parameters");
		lblParameters.setFont(new Font("Dialog", Font.BOLD, 12));
		lblParameters.setBounds(10, 433, 76, 14);
		frame.getContentPane().add(lblParameters);

		JLabel lblUsage = new JLabel("Usage: --encrypt <path to xxxx.bin> [ ... <xxxx.bin>]");
		lblUsage.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblUsage.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
			}
		});
		lblUsage.setBounds(112, 400, 423, 14);
		frame.getContentPane().add(lblUsage);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(584, 115, 342, 270);
		frame.getContentPane().add(scrollPane_1);

		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(System.getProperty("user.dir"), true);
		DefaultTreeModel model = new DefaultTreeModel(rootNode);
		JTree tree = new JTree(model);
		tree.setFont(new Font("Dialog", Font.PLAIN, 12));
		getList(model, rootNode, new File(System.getProperty("user.dir")));
		scrollPane_1.setViewportView(tree);
		
		JButton btnDisplayImage = new JButton("Display Image");
		btnDisplayImage.setFont(new Font("Dialog", Font.PLAIN, 12));
		
		btnDisplayImage.setBounds(815, 396, 111, 23);
		btnDisplayImage.setEnabled(false);
		frame.getContentPane().add(btnDisplayImage);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnOptions = new JMenu("Options");
		mnOptions.setHorizontalAlignment(SwingConstants.LEFT);
		mnOptions.setFont(new Font("Dialog", Font.PLAIN, 12));
		menuBar.add(mnOptions);

		JCheckBoxMenuItem lineWrapMenuItem = new JCheckBoxMenuItem("Line Wrap");
		lineWrapMenuItem.setHorizontalAlignment(SwingConstants.LEFT);
		lineWrapMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
		
		JMenuItem popoutConsoleMenuItem = new JMenuItem("Popout Console");
		popoutConsoleMenuItem.setHorizontalAlignment(SwingConstants.LEFT);
		popoutConsoleMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
		
		
		lineWrapMenuItem.addItemListener((e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				txtConsole.setLineWrap(true);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				txtConsole.setLineWrap(false);
			}
		});
		
		popoutConsoleMenuItem.addActionListener((e) -> {
			consoleWindows.add(new PopoutConsole(txtConsole));
			writeToConsole("Opened Popout Console.");
		});
		mnOptions.add(popoutConsoleMenuItem);
		mnOptions.add(lineWrapMenuItem);

		//Command selector
		cmdSelect.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {

				if (e.getStateChange() == ItemEvent.SELECTED) {
					parameters.set(0, e.getItem().toString().substring(0, e.getItem().toString().indexOf("_")));
					curCmdLbl.setText(parameters.get(0));
					setTextAreaText(paramTxtField);
					currentCommand = e.getItem().toString();
				}

				if (e.getStateChange() == ItemEvent.SELECTED && e.getItem().toString().contains("--create-patch")) {
					parameters.add("MHP3RD_DATA.BIN");
					setTextAreaText(paramTxtField);
				} else if (e.getStateChange() == ItemEvent.SELECTED) {
					for (int i = 0; i < parameters.size(); i++) {
						if (parameters.get(i).contains("MHP3RD_DATA.BIN")) {
							parameters.remove(i);
						}
					}
					setTextAreaText(paramTxtField);
				}

				if (e.getStateChange() == ItemEvent.SELECTED) {

					switch (currentCommand) {
					case "--encrypt_":
						lblUsage.setText("Usage: --encrypt <path to xxxx.bin> [ ... <xxxx.bin>]");
						fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						fileChooser.setMultiSelectionEnabled(true);
						break;

					case "--decrypt_":
						lblUsage.setText("Usage: --decrypt <path to xxxx.bin> [ ... <xxxx.bin>]");
						fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						fileChooser.setMultiSelectionEnabled(true);
						break;

					case "--create-patch_":
						lblUsage.setText("Usage: --create-patch <xxxx.bin.enc> [ ... <xxxx.bin.enc>] <output_file>");
						fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						fileChooser.setMultiSelectionEnabled(true);
						break;

					case "--dec-all_":
						lblUsage.setText("Usage: --dec-all <data.bin> <path to output folder>");
						fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						fileChooser.setMultiSelectionEnabled(false);
						break;

					case "--extract_ .bin":
						lblUsage.setText("Usage: --extract <path to xxxx.bin> [ ... <xxxx.bin>] <decoder number>");
						fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						fileChooser.setMultiSelectionEnabled(true);
						break;

					case "--extract_ .pak":
						lblUsage.setText("Usage: --extract <path to xxxx.pak> [ ... <xxxx.pak>] <decoder number>");
						fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						fileChooser.setMultiSelectionEnabled(true);
						break;

					case "--extract_ .tmh":
						lblUsage.setText("Usage: --extract <path to container.tmh>  [ ... <container.tmh>] <decoder number>");
						fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						fileChooser.setMultiSelectionEnabled(true);
						break;

					case "--rebuild_ .tmh":
						lblUsage.setText("Usage: --rebuild <path to project folder> <encoder number>");
						fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						fileChooser.setMultiSelectionEnabled(false);
						break;

					case "--rebuild_ table":
						lblUsage.setText("Usage: --rebuild <path to string table xxxx.bin> <encoder number>");
						fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						fileChooser.setMultiSelectionEnabled(false);
						break;

					case "--reb-enc_":
						lblUsage.setText("Usage: --reb-enc <path to project folder> <encoder number>");
						fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						fileChooser.setMultiSelectionEnabled(false);
						break;

					case "--dec-ext_":
						lblUsage.setText("Usage: --dec-ext <path to xxxx.bin> <decoder number>");
						fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						fileChooser.setMultiSelectionEnabled(false);
						break;

					case "--gen-index_":
						lblUsage.setText("Usage: --gen-index <data.bin>");
						fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						fileChooser.setMultiSelectionEnabled(false);
						break;

					default:
						writeToConsole("Command not found: " + currentCommand);
					}
					writeToConsole("Command Selected: " + currentCommand);
				}
			}
		});

		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
				fileChooser.setBounds(-34, 51, 618, 399);
				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Binary (*.bin)", "bin"));
				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Encrypted (*.enc) Not Working", ".enc"));
				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Package (*.pak)", "pak"));
				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TMH (*.tmh)", "tmh"));

				int result = fileChooser.showOpenDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION) {
					
					if (fileChooser.getFileSelectionMode() == JFileChooser.FILES_ONLY) {
						String files = browseFile(fileChooser.getSelectedFile());
						lblSelectedFile.setText("Selected File: " + files);
						writeToConsole("Files Selected: " + files);

					} else if (fileChooser.getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY) {
						String file = browseDirectory(fileChooser.getSelectedFile());
						lblSelectedFile.setText(file);
						writeToConsole(file);
						
					} else if (fileChooser.getFileSelectionMode() == JFileChooser.FILES_AND_DIRECTORIES) {

						
						lblSelectedFile.setText("Selected Dir/File: " + fileChooser.getSelectedFile().getAbsolutePath());
						parameters.add(1, fileChooser.getSelectedFile().getAbsolutePath());
						writeToConsole("Folder/File Selected: " + fileChooser.getSelectedFile().getAbsolutePath());
					}
					setTextAreaText(paramTxtField);

				}
			}
		});
		
		btnDisplayImage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Image selectedImage = null;
				try {
					selectedImage = ImageIO.read(new File(selectedPath));
				} catch (IOException e1) {
					writeToConsole(ExceptionUtils.getStackTrace(e1));
				}
				imageWindows.add(new ImageWindow(selectedImage, selectedPath));
			}
		});
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				selectedPath = "";
				Object[] paths = tree.getSelectionPath().getPath();
				if(paths != null) {
					for(int i = 0; i < paths.length; i++) {
						selectedPath += paths[i];
						if(i+1 < paths.length) {
							selectedPath += File.separator;
						}
					}
					
					if(selectedPath.endsWith(".png") || selectedPath.endsWith(".jpg")) {
						btnDisplayImage.setEnabled(true);
					} else {
						btnDisplayImage.setEnabled(false);
					}
				}
			}
		});
		
		btnClear.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Iterator<String> it = parameters.iterator();
				while (it.hasNext()) {
					String next = it.next();
					if (!next.contains(parameters.get(0))) {
						it.remove();
					}
				}
				if (currentCommand.contains("--create-patch")) {
					parameters.add(1, "MHP3RD_DATA.BIN");
				}
				lblSelectedFile.setText("Selected File/Dir: none");
				setTextAreaText(paramTxtField);
			}
		});

		btnBegin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String textField = paramTxtField.getText();
				String textFieldMod = textField.substring(0);
				String[] params = textFieldMod.split("\\s+");
				writeToConsole(textFieldMod);
				MHTools.process(params);
				if(params.length >= 2) {
					refreshTree(model, rootNode, new File(params[1]).getParentFile());
				}
				
			}
		});
	}
	
	private String browseFile(File file) {
		String files = "";

		if (fileChooser.isMultiSelectionEnabled()) {
			for (File s : fileChooser.getSelectedFiles()) {
				parameters.add(1, s.getAbsolutePath());
				files += s.getAbsolutePath() + ", \n";
			}
		} else {
			parameters.add(1, fileChooser.getSelectedFile().getAbsolutePath());
			files += fileChooser.getSelectedFile().getAbsolutePath();
		}
		return files;
	}
	
	private String browseDirectory(File file) {
		String dirs = "";
		
		if (fileChooser.isMultiSelectionEnabled()) {
			for (File f : fileChooser.getSelectedFiles()) {
				parameters.add(1, f.getAbsolutePath());
				dirs += f.getAbsolutePath() + ", \n";
			}
		} else {
			parameters.add(1, file.getAbsolutePath());
			dirs += file.getAbsolutePath();
		}
		return dirs;
	}
	
	private void refreshTree(DefaultTreeModel model, DefaultMutableTreeNode node, File f) {
		node.removeAllChildren();
		model.reload();
		getList(model, node, f);
		node.setUserObject(f.getParentFile());
	}

	private void getList(DefaultTreeModel model, DefaultMutableTreeNode node, File f) {
		if(!f.isDirectory()) {
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(f.getName());
			model.insertNodeInto(child, node, node.getChildCount());
		} else {
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(f.getName());
			model.insertNodeInto(child, node, node.getChildCount());
			File fList[] = f.listFiles();
			for(int i = 0; i < fList.length; i++) {
				getList(model, child, fList[i]);
			}
		}
		
	}

	private void setTextAreaText(JFormattedTextField txtField) {
		String params = "";
		for (String s : parameters) {
			params += s + " ";
		}
		txtField.setText(params);
	}

	public static void writeToConsole(String text) {
		if (txtConsole != null) {
			txtConsole.append(text + "\n");
			
			if(consoleWindows.size() > 0) {
				for(PopoutConsole cw : consoleWindows) {
					cw.append(text + "\n");
				}
			}
		}
		System.out.println(text);
	}
	
	private class ImageWindow extends JPanel {
		private static final long serialVersionUID = -1472016857763489863L;
		
		private JFrame frame;
		private Image image;
		private String path;
		private int scale = 5;
		public ImageWindow(Image image, String path) {
			this.image = image;
			this.path = path;
			frame = new JFrame(this.path);
			frame.getContentPane().add(this);
			frame.setSize(image.getWidth(null) * scale + 50, image.getHeight(null) * scale + 50);
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setLocationRelativeTo(null);		
			frame.setVisible(true);
			
			
		}
		
		public void paintComponent(Graphics g) {
			g.drawImage(image, 0, 0, image.getWidth(null) * scale, image.getHeight(null) * scale, null);
		}
	}
}
